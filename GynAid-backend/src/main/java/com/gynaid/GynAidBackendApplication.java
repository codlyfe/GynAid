package com.gynaid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@EnableAsync
@EnableScheduling
public class GynAidBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GynAidBackendApplication.class, args);
	}

}
