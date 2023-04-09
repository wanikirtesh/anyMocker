package com.ideas.anymocker;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.AntPathMatcher;

@SpringBootApplication
@EnableAsync
public class AnyMockerApplication {

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

		Thread thread = new Thread(() -> {
			context.close();
			context = SpringApplication.run(AnyMockerApplication.class, args.getSourceArgs());
		});

		thread.setDaemon(false);
		thread.start();
	}

}
