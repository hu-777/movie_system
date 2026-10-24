package com.example.common;
import com.example.security.IdentityService;
import com.example.entity.Account;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean public BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Resource private IdentityService identities;
    @Value("${spring.h2.console.enabled:false}") private boolean h2Console;
    public static void error(HttpServletResponse response,int status,String message) throws IOException {
        response.setStatus(status);response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\""+status+"\",\"msg\":\""+message+"\"}");
    }
    @Override protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().and().formLogin().disable().httpBasic().disable().logout().disable()
            .requestCache().disable()
            .exceptionHandling().authenticationEntryPoint((req,res,e)->error(res,401,"请先登录"))
            .accessDeniedHandler((req,res,e)->error(res,403,"无权限或安全凭据已过期，请刷新后重试"));
        http.headers().frameOptions().sameOrigin();
        if(h2Console) {
            http.csrf().ignoringAntMatchers("/h2-console/**");
            http.authorizeRequests().antMatchers("/h2-console/**").access("hasIpAddress('127.0.0.1') or hasIpAddress('::1')");
        }
        http.authorizeRequests()
            .antMatchers(HttpMethod.GET,"/auth/csrf","/check-username","/","/files/download/**","/files/hasDefaultAvatar").permitAll()
            .antMatchers(HttpMethod.POST,"/login","/register","/getSecurityQuestion","/verifySecurityAnswer","/resetPassword").permitAll()
            .antMatchers(HttpMethod.GET,"/goods/**","/goodsStock/**","/category/**","/notice/**","/screening/getById","/screening/getByGoodsId","/screening/canBuyTicket","/seat/selectByScreeningId").permitAll()
            .antMatchers(HttpMethod.GET,"/auth/me").authenticated()
            .antMatchers(HttpMethod.PUT,"/auth/profile","/updatePassword").authenticated()
            .antMatchers(HttpMethod.POST,"/logout","/files/upload").authenticated()
            .antMatchers(HttpMethod.GET,"/orders/selectAll","/orders/selectPage","/orders/selectById/*").authenticated()
            .antMatchers(HttpMethod.POST,"/orders/addWithSeats").hasRole("USER")
            .antMatchers(HttpMethod.POST,"/orders/exportBatchInvoices").authenticated()
            .antMatchers(HttpMethod.PUT,"/orders/update").authenticated()
            .antMatchers(HttpMethod.DELETE,"/orders/delete/*").hasRole("ADMIN")
            .antMatchers("/admin/**","/user/**","/goods/**","/goodsStock/**","/category/**","/notice/**","/screening/**",
                "/lineCharts","/barCharts","/pieCharts","/scatterCharts").hasRole("ADMIN")
            // Legacy direct seat mutation and seatless purchase would bypass inventory rules.
            .anyRequest().denyAll();
        http.addFilterAfter(new OncePerRequestFilter(){
            @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
                org.springframework.security.core.Authentication auth=SecurityContextHolder.getContext().getAuthentication();
                if(auth!=null && auth.isAuthenticated()) {
                    Account account=identities.byKey(auth.getName());
                    if(account==null || !identities.stamp(account).equals(auth.getCredentials()) || !identities.key(account).equals(auth.getName())) {
                        SecurityContextHolder.clearContext();
                        HttpSession session=req.getSession(false);if(session!=null) session.invalidate();
                    }
                }
                chain.doFilter(req,res);
            }
        },SecurityContextPersistenceFilter.class);
    }
}
