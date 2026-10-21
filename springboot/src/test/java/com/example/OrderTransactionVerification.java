package com.example;

import com.example.entity.Orders;
import com.example.service.OrdersService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.concurrent.*;

/** Standalone integration regression: isolated H2 database, real Spring transactions. */
public class OrderTransactionVerification {
    static JdbcTemplate db;
    static OrdersService service;
    static int passed;
    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
        passed++;
    }
    static void rejects(Runnable action, String message) {
        boolean rejected = false;
        try { action.run(); } catch (RuntimeException expected) { rejected = true; }
        check(rejected, message);
    }
    static void reset() {
        db.update("delete from orders"); db.update("delete from seat"); db.update("delete from screening");
        db.update("insert into screening(id, goods_id, goods_name, hall_name, start_time, price, total_seats, available_seats) values(900,1,'Test','Test hall',DATEADD('DAY',1,CURRENT_TIMESTAMP),40,3,3)");
        for (int i=1;i<=3;i++) db.update("insert into seat(id,screening_id,seat_row,seat_column,status,seat_name) values(?,900,'A',?,'AVAILABLE',?)",900+i,i,"A"+i);
        db.update("update upcoming_movies set tickets=3 where id=1");
    }
    static int count(String sql) { return db.queryForObject(sql,Integer.class); }
    static void book(String seats) { service.addOrderWithSeats(1,900,1,null,seats); }
    static Orders latest() { return service.selectById(count("select max(id) from orders")); }
    static void state(int id,String status) { Orders o=new Orders();o.setId(id);o.setStatus(status);service.updateById(o); }
    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext context = SpringApplication.run(SpringbootApplication.class,
                "--server.port=0", "--spring.datasource.url=jdbc:h2:mem:order_verification;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
                "--spring.sql.init.mode=always", "--spring.h2.console.enabled=false",
                "--mybatis.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl")) {
            db=context.getBean(JdbcTemplate.class);service=context.getBean(OrdersService.class);
            reset(); book("A1,A2"); Orders order=latest();
            check(order.getNum()==2 && order.getTotalPrice()==80,"Server price and quantity");
            check(service.selectAllOrders().size()==1,"Statistics join works on H2");
            check(service.selectPage(new Orders(),1,10,1,"USER").getList().size()==1,"Order page join works on H2");
            check(count("select available_seats from screening where id=900")==1,"Seat inventory deducted");
            check(count("select tickets from upcoming_movies where id=1")==1,"Movie inventory synchronized");
            rejects(()->book("A1"),"Occupied seat rejected");
            rejects(()->book("A3,A3"),"Duplicate seat rejected");
            rejects(()->book("Z99"),"Nonexistent seat rejected");
            rejects(()->service.addOrderWithSeats(2,900,1,null,"A3"),"Mismatched movie rejected");
            rejects(()->book("A3,A1"),"Partial claim rolls back");
            check(count("select count(*) from seat where seat_name='A3' and status='AVAILABLE'")==1,"Uncommitted seat remains available");
            state(order.getId(),"已取消");state(order.getId(),"已取消");
            check(count("select available_seats from screening where id=900")==3,"Cancel restores exactly once");
            check(count("select count(*) from seat where status='AVAILABLE'")==3,"Cancel frees actual seats");
            final int cancelledId = order.getId();
            rejects(()->state(cancelledId,"待取票"),"Cancelled order cannot be paid");
            reset();book("A1");order=latest();state(order.getId(),"待取票");state(order.getId(),"待退票");state(order.getId(),"已退票");state(order.getId(),"已退票");
            check(count("select available_seats from screening where id=900")==3,"Refund restores exactly once");
            reset();db.execute("alter table orders add constraint reject_test_insert check (screening_id <> 900)");
            rejects(()->book("A1"),"Forced insert failure propagates");
            check(count("select count(*) from orders")==0 && count("select available_seats from screening where id=900")==3 && count("select tickets from upcoming_movies where id=1")==3 && count("select count(*) from seat where status='AVAILABLE'")==3,"Insert failure rolls back all inventory");
            db.execute("alter table orders drop constraint reject_test_insert");
            reset(); ExecutorService executor=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
            Callable<Boolean> attempt=()->{start.await();try{book("A1");return true;}catch(RuntimeException conflict){return false;}};
            Future<Boolean> first=executor.submit(attempt), second=executor.submit(attempt);start.countDown();
            int successes=(first.get(15,TimeUnit.SECONDS)?1:0)+(second.get(15,TimeUnit.SECONDS)?1:0);executor.shutdownNow();
            check(successes==1 && count("select count(*) from orders")==1,"Concurrent same-seat booking has one winner");
            check(count("select available_seats from screening where id=900")==2,"Concurrent inventory consistent");
            reset();book("A2");
            rejects(()->book("A1,A2"),"Failure after first seat claim propagates");
            check(count("select count(*) from seat where seat_name='A1' and status='AVAILABLE'")==1,"Partial seat claim rolled back");
            rejects(()->context.getBean(com.example.service.ScreeningService.class).deleteById(900),"Booked screening cannot be deleted");
            reset();
            com.example.entity.Screening show = new com.example.entity.Screening();
            show.setGoodsId(1);show.setGoodsName("Test");show.setHallName("Test hall");show.setPrice(35);
            show.setStartTime(new java.util.Date(System.currentTimeMillis()+86400000));
            show.setEndTime(new java.util.Date(System.currentTimeMillis()+93600000));
            context.getBean(com.example.service.ScreeningService.class).add(show);
            check(count("select total_seats from screening where id="+show.getId())==144,"New screening persists capacity");
            check(count("select count(*) from screening where id="+show.getId()+" and end_time is not null")==1,"New screening persists end time");
            context.getBean(com.example.service.ScreeningService.class).deleteById(show.getId());
            check(count("select count(*) from seat where screening_id="+show.getId())==0,"Unbooked screening deletion removes seats");
            reset();db.update("update screening set start_time=DATEADD('MINUTE',4,CURRENT_TIMESTAMP) where id=900");
            rejects(()->book("A1"),"Five minute cutoff enforced");
            System.out.println("ORDER_REGRESSION_PASSED: " + passed + " assertions");
        }
    }
}
