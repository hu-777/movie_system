package com.example.security;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
@Component
public class RecoveryAnswerMigration implements ApplicationRunner {
    @Resource private JdbcTemplate jdbc;
    @Resource private BCryptPasswordEncoder encoder;
    @Transactional public void run(ApplicationArguments args){
        jdbc.query("select id, security_answer from user where security_answer is not null", rs->{
            String answer=rs.getString("security_answer");
            if(!answer.matches("\\$2[aby]\\$\\d{2}\\$.{53}"))
                jdbc.update("update user set security_answer=? where id=?",encoder.encode(answer),rs.getInt("id"));
        });
    }
}
