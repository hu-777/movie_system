package com.example.controller;

import cn.hutool.core.lang.Dict;
import com.example.common.Result;
import com.example.entity.*;
import com.example.service.*;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
public class WebController {

    @Resource
    private AdminServiceImpl adminServiceImpl;
    @Resource
    private UserServiceImpl userServiceImpl;
    @Resource
    private GoodsStockService goodsStockService;
    @Autowired
    private OrdersService ordersService;

    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("ADMIN".equals(account.getRole())) {
            ac = adminServiceImpl.login(account);
        }else{
            ac = userServiceImpl.login(account);
        }
        return Result.success(ac);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userServiceImpl.register(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("ADMIN".equals(account.getRole())) {
            adminServiceImpl.updatePassword(account);
        }else{
            userServiceImpl.updatePassword(account);
        }
        return Result.success();
    }

    /**
     * 获取统计图数据
     * @return 动态数据
     * */
    @GetMapping("/lineCharts")
    public Result lineCharts() {
        List<Orders> list = ordersService.selectAllOrders();
    
        // 创建两个统计维度 - 添加null检查过滤time为null的记录
        Map<String, Integer> amountMap = list.stream()
                .filter(orders -> orders.getTime() != null) // 过滤time为null的记录
                .collect(Collectors.groupingBy(
                        orders -> orders.getTime().substring(5, 10),
                        Collectors.summingInt(Orders::getTotalPrice)
                ));
    
        Map<String, Long> countMap = list.stream()
                .filter(orders -> orders.getTime() != null) // 过滤time为null的记录
                .collect(Collectors.groupingBy(
                        orders -> orders.getTime().substring(5, 10),
                        Collectors.counting()
                ));
    
        // 合并所有日期并排序
        Set<String> allDates = new HashSet<>();
        allDates.addAll(amountMap.keySet());
        allDates.addAll(countMap.keySet());
    
        List<Dict> lineList = allDates.stream()
                .sorted()
                .map(date -> Dict.create()
                        .set("date", date)
                        .set("amount", amountMap.getOrDefault(date, 0))  // 金额数据
                        .set("count", countMap.getOrDefault(date, 0L))   // 数量数据
                ).collect(Collectors.toList());
    
        return Result.success(lineList);
    }

    @GetMapping("/barCharts")
    public Result barCharts() {
        List<Orders> orders = ordersService.selectAllOrders();
        List<GoodsStock> goods = goodsStockService.selectAllUpcoming();

        // 将商品ID强制转换为Integer类型建立映射
        Map<Integer, String> goodsTypeMap = goods.stream()
                .collect(Collectors.toMap(
                        GoodsStock::getId,
                        GoodsStock::getType,
                        (existing, replacement) -> existing
                ));

        Map<String, Integer> amountMap = new HashMap<>();
        Map<String, Long> countMap = new HashMap<>();

        for (Orders order : orders) {
            Integer goodsId = order.getGoodsId(); // 保持原始Integer类型
            String type = goodsTypeMap.getOrDefault(goodsId, "其他");

            amountMap.put(type, amountMap.getOrDefault(type, 0) + order.getTotalPrice());
            countMap.put(type, countMap.getOrDefault(type, 0L) + 1);
        }

        // 合并排序逻辑保持不变
        Set<String> allTypes = new TreeSet<>();
        allTypes.addAll(amountMap.keySet());
        allTypes.addAll(countMap.keySet());

        List<Dict> barList = allTypes.stream()
                .map(type -> Dict.create()
                        .set("type", type)
                        .set("amount", amountMap.getOrDefault(type, 0))
                        .set("count", countMap.getOrDefault(type, 0L))
                ).collect(Collectors.toList());

        return Result.success(barList);
    }

    @GetMapping("/pieCharts")
    public Result pieCharts() {
        List<Orders> orders = ordersService.selectAllOrders();
    
        // 添加filter过滤掉userId为null的订单记录
        Map<Integer, Dict> userStats = orders.stream()
                .filter(order -> order.getUserId() != null) // 过滤userId为null的记录
                .collect(Collectors.groupingBy(
                        Orders::getUserId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> Dict.create()
                                        .set("name", Optional.ofNullable(list.get(0).getUserName())
                                                .orElse("用户#" + list.get(0).getUserId()))
                                        .set("count", list.size()) // 新增数量统计
                                        .set("amount", list.stream()  // 新增金额统计
                                                .mapToInt(Orders::getTotalPrice).sum())
                        )
                ));
    
        int totalCount = orders.stream()
                .filter(order -> order.getUserId() != null) // 同样过滤userId为null的记录
                .mapToInt(order -> 1).sum();
        
        int totalAmount = orders.stream()
                .filter(order -> order.getUserId() != null) // 同样过滤userId为null的记录
                .mapToInt(Orders::getTotalPrice).sum();
    
        List<Dict> pieData = userStats.values().stream()
                .map(user -> Dict.create()
                        .set("name", user.getStr("name"))
                        .set("countRatio", user.getInt("count") * 1.0 / totalCount) // 使用正确字段
                        .set("amountRatio", user.getInt("amount") * 1.0 / totalAmount) // 使用正确字段
                ).collect(Collectors.toList());
    
        return Result.success(Dict.create()
                .set("totalCount", totalCount)
                .set("totalAmount", totalAmount)
                .set("data", pieData));
    }

    @GetMapping("/scatterCharts")
    public Result scatterCharts() {
        List<Orders> orders = ordersService.selectAllOrders();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd");
    
        Map<String, Map<Integer, Integer>> dateHourMap = new TreeMap<>();
    
        for (Orders order : orders) {
            try {
                // 添加null检查
                if (order.getTime() == null) continue;
                
                LocalDateTime dateTime = LocalDateTime.parse(order.getTime(), formatter);
                String date = dateTime.format(dateFormatter);
                int hour = dateTime.getHour();
    
                dateHourMap.computeIfAbsent(date, k -> new HashMap<>())
                        .merge(hour, 1, Integer::sum);
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        // 添加参考线数据（12点、18点）
        List<Dict> referenceLines = new ArrayList<>();
        referenceLines.add(Dict.create().set("x", 12).set("name", "午间高峰"));
        referenceLines.add(Dict.create().set("x", 18).set("name", "晚间高峰"));

        List<Dict> scatterData = new ArrayList<>();
        dateHourMap.forEach((date, hourCounts) -> {
            hourCounts.forEach((hour, count) -> {
                scatterData.add(Dict.create()
                        .set("x", hour)
                        .set("y", date)
                        .set("value", count));
            });
        });

        return Result.success(Dict.create()
                .set("data", scatterData)
                .set("references", referenceLines)); // 返回参考线数据
    }

    @GetMapping("/check-username")
    public Result checkUsername(@RequestParam String username) {
        User user = userServiceImpl.find(username);
        if (user != null) {
            // 用户名已存在
            return Result.success(true);
        } else {
            // 用户名可用
            return Result.success(false);
        }
    }
    
    @PostMapping("/getSecurityQuestion")
    public Result getSecurityQuestion(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String securityQuestion = userServiceImpl.getSecurityQuestion(username);
        return Result.success(securityQuestion);
    }
    
    @PostMapping("/verifySecurityAnswer")
    public Result verifySecurityAnswer(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String securityAnswer = request.get("securityAnswer");
        boolean isCorrect = userServiceImpl.verifySecurityAnswer(username, securityAnswer);
        if (isCorrect) {
            return Result.success();
        } else {
            return Result.error("密保答案错误");
        }
    }
    
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String securityAnswer = request.get("securityAnswer");
        String newPassword = request.get("newPassword");
        userServiceImpl.resetPassword(username, securityAnswer, newPassword);
        return Result.success();
    }
    
}