package com.example;

import com.example.entity.*;
import com.example.service.OrdersService;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Real HTTP security regression; disposable H2 DB, no production account changes. */
public class AuthSecurityVerification {
    static final ObjectMapper json=new ObjectMapper();
    static String base; static int passed;
    static void check(boolean condition,String label){if(!condition)throw new AssertionError(label);passed++;}
    static Map<String,Object> fields(Object... pairs){Map<String,Object> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put(pairs[i].toString(),pairs[i+1]);return out;}
    static class Reply {int status;JsonNode body;String raw;String cookieHeader;}
    static class Client {
        String cookie="", token;
        Reply call(String method,String path,Object body) throws Exception {return call(method,path,body,true);}
        Reply call(String method,String path,Object body,boolean protect) throws Exception {
            HttpURLConnection conn=(HttpURLConnection)new URL(base+path).openConnection();
            conn.setConnectTimeout(5000);conn.setReadTimeout(15000);conn.setInstanceFollowRedirects(false);conn.setRequestMethod(method);
            if(!cookie.isEmpty())conn.setRequestProperty("Cookie",cookie);
            if(protect && token!=null)conn.setRequestProperty("X-CSRF-TOKEN",token);
            if(body!=null){conn.setDoOutput(true);conn.setRequestProperty("Content-Type","application/json");try(OutputStream stream=conn.getOutputStream()){stream.write(json.writeValueAsBytes(body));}}
            Reply reply=new Reply();reply.status=conn.getResponseCode();
            reply.cookieHeader=conn.getHeaderField("Set-Cookie");
            if(reply.cookieHeader!=null && reply.cookieHeader.startsWith("JSESSIONID="))cookie=reply.cookieHeader.split(";",2)[0];
            InputStream input=reply.status>=400?conn.getErrorStream():conn.getInputStream();ByteArrayOutputStream data=new ByteArrayOutputStream();
            if(input!=null){byte[] bytes=new byte[4096];int size;while((size=input.read(bytes))!=-1)data.write(bytes,0,size);input.close();}
            reply.raw=new String(data.toByteArray(),StandardCharsets.UTF_8);
            reply.body=reply.raw.isEmpty()?json.createObjectNode():json.readTree(reply.raw);conn.disconnect();return reply;
        }
        Reply csrf() throws Exception {Reply r=call("GET","/auth/csrf",null);token=r.body.path("data").path("token").asText();check(!token.isEmpty(),"CSRF bootstrap");return r;}
        Reply upload(byte[] bytes,String filename) throws Exception {
            HttpURLConnection conn=(HttpURLConnection)new URL(base+"/files/upload").openConnection();
            conn.setRequestMethod("POST");conn.setDoOutput(true);conn.setReadTimeout(15000);
            conn.setRequestProperty("Cookie",cookie);conn.setRequestProperty("X-CSRF-TOKEN",token);
            conn.setRequestProperty("Content-Type","multipart/form-data; boundary=auth-test-boundary");
            try(OutputStream out=conn.getOutputStream()) {
                out.write(("--auth-test-boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\""+filename+"\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                out.write(bytes);out.write("\r\n--auth-test-boundary--\r\n".getBytes(StandardCharsets.UTF_8));
            }
            Reply reply=new Reply();reply.status=conn.getResponseCode();
            try(InputStream in=reply.status>=400?conn.getErrorStream():conn.getInputStream()){reply.body=json.readTree(in);}
            conn.disconnect();return reply;
        }
        void login(String name,String role,String password) throws Exception {
            csrf();String old=cookie;
            Reply reply=call("POST","/login",fields("username",name,"role",role,"password",password));
            check(reply.status==200 && reply.body.path("code").asText().equals("200"),"Login "+name);
            check(!cookie.equals(old),"Session ID rotates");
            check(!reply.raw.contains("password") && !reply.raw.contains("securityAnswer"),"Login response redacted");csrf();
        }
    }
    public static void main(String[] args) throws Exception {
        try(ConfigurableApplicationContext context=SpringApplication.run(SpringbootApplication.class,
            "--server.port=0","--server.address=127.0.0.1","--spring.datasource.url=jdbc:h2:mem:auth_verification;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
            "--spring.sql.init.mode=always","--spring.h2.console.enabled=false","--file.storage-path=./target/auth-test-uploads","--mybatis.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl")) {
            base="http://127.0.0.1:"+((ServletWebServerApplicationContext)context).getWebServer().getPort();
            JdbcTemplate db=context.getBean(JdbcTemplate.class);BCryptPasswordEncoder encoder=context.getBean(BCryptPasswordEncoder.class);
            String password="TestPass1!", changed="ChangedPass2!";
            for(int id=1;id<=2;id++)db.update("insert into user(id,username,name,password,role,security_question,security_answer) values(?,?,?,?,?,?,?)",id,"authuser"+id,"User "+id,encoder.encode(password),"USER","Color?",encoder.encode("blue"));
            db.update("update user set security_answer='legacy answer' where id=2");
            context.getBean(com.example.security.RecoveryAnswerMigration.class).run(null);
            String upgraded=db.queryForObject("select security_answer from user where id=2",String.class);
            check(encoder.matches("legacy answer",upgraded),"Legacy answer migration");
            context.getBean(com.example.security.RecoveryAnswerMigration.class).run(null);
            check(upgraded.equals(db.queryForObject("select security_answer from user where id=2",String.class)),"Answer migration idempotent");
            db.update("update user set security_answer=? where id=2",encoder.encode("blue"));
            db.update("update screening set start_time=DATEADD('DAY',1,CURRENT_TIMESTAMP) where id=3");
            check("24h".equals(context.getEnvironment().getProperty("server.servlet.session.timeout")),"24h idle setting");
            Client anonymous=new Client();
            check(anonymous.call("GET","/auth/me",null).status==401,"Anonymous me rejected");
            check(anonymous.call("GET","/admin/selectAll",null).status==401,"Anonymous admin rejected");
            check(anonymous.call("GET","/goodsStock/selectAll",null).status==200,"Public movie browsing");
            check(anonymous.call("POST","/login",fields("username","authuser1","password",password,"role","USER"),false).status==403,"Login CSRF enforced");
            Reply bootstrap=new Client().csrf();anonymous.csrf();check(bootstrap.cookieHeader!=null && bootstrap.cookieHeader.contains("HttpOnly") && bootstrap.cookieHeader.contains("SameSite=Lax"),"Session cookie flags");
            Client user=new Client();user.login("authuser1","USER",password);
            ByteArrayOutputStream png=new ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",png);
            check(anonymous.upload(png.toByteArray(),"avatar.png").status==401,"Anonymous upload blocked");
            check(user.upload("<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8),"fake.png").status==400,"Fake image rejected");
            Reply uploaded=user.upload(png.toByteArray(),"../../outside.png");
            check(uploaded.status==200 && uploaded.body.path("data").asText().matches("/api/files/download/[a-f0-9\\-]+\\.png"),"Upload uses server-generated safe filename");
            check(user.call("GET","/admin/selectAll",null).status==403,"User cannot manage admins");
            check(user.call("GET","/lineCharts",null).status==403,"User cannot read statistics");
            check(user.call("POST","/seat/updateStatus",fields("id",1,"status","AVAILABLE")).status==403,"Direct seat mutation blocked");
            check(user.call("POST","/orders/add",fields("goodsId",3,"userId",2,"num",1)).status==403,"Legacy seatless booking blocked");
            check(user.call("PUT","/auth/profile",fields("name","Missing CSRF"),false).status==403,"Write CSRF enforced");
            Reply profile=user.call("PUT","/auth/profile",fields("id",2,"name","Own profile","role","ADMIN","password",changed));
            check(profile.status==200 && "Own profile".equals(db.queryForObject("select name from user where id=1",String.class)),"Profile targets current account");
            check("User 2".equals(db.queryForObject("select name from user where id=2",String.class)) && encoder.matches(password,db.queryForObject("select password from user where id=1",String.class)),"Profile cannot change other account or password");
            check(user.call("PUT","/user/update",fields("id",1,"role","ADMIN")).status==403,"User management protected");
            Reply bought=user.call("POST","/orders/addWithSeats",fields("goodsId",3,"screeningId",3,"userId",2,"seatInfo","A1"));
            check(bought.status==200 && "200".equals(bought.body.path("code").asText()),"Authenticated seat order succeeds");
            int own=db.queryForObject("select max(id) from orders",Integer.class);
            check(db.queryForObject("select user_id from orders where id=?",Integer.class,own)==1,"Forged buyer ignored");
            context.getBean(OrdersService.class).addOrderWithSeats(3,3,2,null,"A2");int other=db.queryForObject("select max(id) from orders",Integer.class);
            Reply page=user.call("GET","/orders/selectPage?userRole=ADMIN&userId=2",null);
            check(page.body.path("data").path("total").asInt()==1 && page.body.path("data").path("list").get(0).path("userId").asInt()==1,"Forged list role ignored");
            check(user.call("GET","/orders/selectById/"+other,null).status==403,"Other order detail forbidden");
            check(user.call("PUT","/orders/update",fields("id",other,"status","已取消")).status==403,"Other order mutation forbidden");
            check(user.call("POST","/orders/exportBatchInvoices",fields("orderIds",Arrays.asList(own,other))).status==403,"Mixed ownership batch forbidden");
            check(user.call("PUT","/orders/update",fields("id",own,"status","已退票")).status==403,"User cannot approve refunds");
            Client admin=new Client();admin.login("admin","ADMIN","admin");
            Reply accounts=admin.call("GET","/user/selectAll",null);check(accounts.status==200 && !accounts.raw.contains("password") && !accounts.raw.contains("securityAnswer"),"Account list redacted");
            check(admin.call("POST","/orders/addWithSeats",fields("goodsId",3,"screeningId",3,"seatInfo","A3")).status==403,"Admin cannot impersonate buyer");
            check(admin.call("PUT","/orders/update",fields("id",own,"status","待取票")).status==403,"Admin cannot simulate user payment");
            user.call("PUT","/orders/update",fields("id",own,"status","待取票"));user.call("PUT","/orders/update",fields("id",own,"status","待退票"));
            check("200".equals(admin.call("PUT","/orders/update",fields("id",own,"status","已退票")).body.path("code").asText()),"Admin refund approval succeeds");
            Client second=new Client();second.login("authuser1","USER",password);
            check(user.call("PUT","/updatePassword",fields("username","authuser2","role","ADMIN","password",password,"newPassword",changed)).status==200,"Change own password");
            check(second.call("GET","/auth/me",null).status==401,"Other sessions revoked after password change");
            user.login("authuser1","USER",changed);
            Client recovery=new Client();recovery.csrf();
            check(recovery.call("POST","/resetPassword",fields("username","authuser1","securityAnswer","blue","newPassword",password)).status==400,"Cannot reset with answer alone");
            Reply verified=recovery.call("POST","/verifySecurityAnswer",fields("username","authuser1","securityAnswer","blue"));String grant=verified.body.path("data").path("resetToken").asText();check(!grant.isEmpty(),"Recovery grant issued");
            check(anonymous.call("POST","/resetPassword",fields("resetToken",grant,"newPassword",password)).status==400,"Recovery grant bound to session");
            check(recovery.call("POST","/resetPassword",fields("username","authuser2","resetToken",grant,"newPassword",password)).status==200,"Recovery grant binds target account");
            check(user.call("GET","/auth/me",null).status==401,"Recovery revokes existing sessions");
            recovery.csrf();check(recovery.call("POST","/resetPassword",fields("resetToken",grant,"newPassword",changed)).status==400,"Recovery grant single use");
            for(int i=0;i<5;i++)recovery.call("POST","/verifySecurityAnswer",fields("username","authuser2","securityAnswer","wrong"));
            check(recovery.call("POST","/verifySecurityAnswer",fields("username","authuser2","securityAnswer","blue")).status==429,"Recovery rate limit");
            user.login("authuser1","USER",password);String stolen=user.cookie;
            check(user.call("POST","/logout",fields()).status==200,"Logout succeeds");Client replay=new Client();replay.cookie=stolen;
            check(replay.call("GET","/auth/me",null).status==401,"Logged out session cannot be replayed");
            second.login("authuser2","USER",password);db.update("delete from user where id=2");
            check(second.call("GET","/auth/me",null).status==401,"Deleted account session revoked");
            check(admin.call("GET","/h2-console/",null).status==403,"H2 console disabled");
            Client registration=new Client();registration.csrf();
            Reply registered=registration.call("POST","/register",fields("username","newauthuser","password",password,"role","ADMIN","id",1,"securityQuestion","Color?","securityAnswer","blue"));
            check(registered.status==200 && registered.body.path("code").asText().equals("200"),"Registration succeeds");
            check("USER".equals(db.queryForObject("select role from user where username='newauthuser'",String.class)),"Registration cannot choose role");
            check(encoder.matches("blue",db.queryForObject("select security_answer from user where username='newauthuser'",String.class)),"Registration hashes answer");
            System.out.println("AUTH_REGRESSION_PASSED: "+passed+" assertions");
        }
    }
}
