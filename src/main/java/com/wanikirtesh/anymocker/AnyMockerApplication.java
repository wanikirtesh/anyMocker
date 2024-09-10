package com.wanikirtesh.anymocker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.AntPathMatcher;

@SpringBootApplication
@EnableAsync
@Slf4j
public class AnyMockerApplication {
	@Value("${post.threadpool.size:100}")
	int postThreadPoolSize;
	public static void main(final String[] args) {
        SpringApplication.run(AnyMockerApplication.class, args);
	}
	@Bean
	public AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}

	@Bean(name = "threadPoolTaskExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(this.postThreadPoolSize);
		executor.setMaxPoolSize(this.postThreadPoolSize);
		executor.setQueueCapacity(Integer.MAX_VALUE);
		executor.setThreadNamePrefix("PostActionExecutor-");
		executor.initialize();
		return executor;
	}

}
