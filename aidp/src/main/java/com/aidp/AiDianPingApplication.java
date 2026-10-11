package com.aidp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.aidp.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class AiDianPingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDianPingApplication.class, args);
    }

}
