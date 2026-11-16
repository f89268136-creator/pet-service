package com.pet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 宠物预约平台启动类。
 *
 * <p>开启事务管理、缓存注解和定时任务调度。</p>
 */
@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching//开发缓存注解功能
@EnableScheduling //开启任务调度
public class PetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetServiceApplication.class, args);
        log.info("server started");
    }
}
