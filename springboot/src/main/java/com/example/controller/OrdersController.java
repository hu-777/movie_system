package com.example.controller;
import com.example.common.Result;
import com.example.entity.*;
import com.example.service.OrdersService;
import com.example.security.IdentityService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.annotation.Resource;
import java.util.*;
@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Resource private OrdersService service;
    @Resource private IdentityService identities;
    public static class SeatOrder { public Integer goodsId,screeningId;public String seatInfo; }
    public static class StatusChange { public Integer id;public String status; }
    public static class ExportRequest { public List<Integer> orderIds; }
    @PostMapping("/addWithSeats") public Result add(@RequestBody SeatOrder input){
        Account account=identities.current();
        if(!"USER".equals(account.getRole()))throw new AccessDeniedException("仅用户可购票");
        service.addOrderWithSeats(input.goodsId,input.screeningId,account.getId(),null,input.seatInfo);return Result.success();
    }
    @GetMapping("/selectAll") public Result all(){Account account=identities.current();return Result.success(service.selectAll(account.getRole(),account.getId()));}
    @GetMapping("/selectPage") public Result page(@RequestParam(defaultValue="1") int pageNum,@RequestParam(defaultValue="10") int pageSize,
            @RequestParam(required=false) String orderNo,@RequestParam(required=false) String goodsName){
        Account account=identities.current();Orders filter=new Orders();filter.setOrderNo(orderNo);filter.setGoodsName(goodsName);
        return Result.success(service.selectPage(filter,Math.max(1,pageNum),Math.min(100,Math.max(1,pageSize)),account.getId(),account.getRole()));
    }
    @GetMapping("/selectById/{id}") public Result one(@PathVariable Integer id){Orders order=service.selectById(id);identities.assertOwner(order);return Result.success(order);}
    @PutMapping("/update") public Result update(@RequestBody StatusChange input){
        Orders order=service.selectById(input.id);identities.assertOwner(order);Account account=identities.current();
        if("ADMIN".equals(account.getRole())) { if(!"已退票".equals(input.status))throw new AccessDeniedException("管理员仅可确认退票"); }
        else if(!Arrays.asList("待取票","已取消","已完成","待退票").contains(input.status))throw new AccessDeniedException("不能执行该订单操作");
        Orders patch=new Orders();patch.setId(input.id);patch.setStatus(input.status);service.updateById(patch);return Result.success();
    }
    @DeleteMapping("/delete/{id}") public Result delete(@PathVariable Integer id){service.deleteById(id);return Result.success();}
    @PostMapping("/exportBatchInvoices") public Result export(@RequestBody ExportRequest input){
        if(input.orderIds==null || input.orderIds.isEmpty() || input.orderIds.size()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请选择1至100个订单");
        List<Orders> orders=new ArrayList<>();
        for(Integer id:input.orderIds){Orders order=service.selectById(id);identities.assertOwner(order);orders.add(order);}
        return Result.success(orders);
    }
}
