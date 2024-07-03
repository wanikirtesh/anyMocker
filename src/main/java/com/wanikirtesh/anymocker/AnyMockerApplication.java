package com.wanikirtesh.anymocker;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.AntPathMatcher;

@SpringBootApplication
@EnableAsync
@Log
public class AnyMockerApplication {
	@Value("${post.threadpool.size:100}")
	int postThreadPoolSize;
	private static ConfigurableApplicationContext  context;

	public static void main(String[] args) {
		context = SpringApplication.run(AnyMockerApplication.class, args);

	}
	@Bean
	public AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}
	public static void restart() {
		ApplicationArguments args = context.getBean(ApplicationArguments.class);
		log.info("Restarting application....");
		Thread thread = new Thread(() -> {
			context.close();
			context = SpringApplication.run(AnyMockerApplication.class, args.getSourceArgs());
		});

		thread.setDaemon(false);
		thread.start();
	}

	@Bean(name = "threadPoolTaskExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(postThreadPoolSize);
		executor.setMaxPoolSize(postThreadPoolSize);
		executor.setQueueCapacity(Integer.MAX_VALUE);
		executor.setThreadNamePrefix("PostActionExecutor-");
		executor.initialize();
		return executor;
	}

}
