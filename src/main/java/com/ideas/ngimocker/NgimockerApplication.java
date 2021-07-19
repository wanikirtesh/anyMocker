package com.ideas.ngimocker;

import com.ideas.ngimocker.components.PathList;

import com.ideas.ngimocker.service.FileStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@SpringBootApplication
@EnableAsync
public class NgimockerApplication {

	private static ConfigurableApplicationContext  context;

	public static void main(String[] args) {
		context = SpringApplication.run(NgimockerApplication.class, args);
	}

	@Bean
	public AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}

	public static void restart() {
		ApplicationArguments args = context.getBean(ApplicationArguments.class);

		Thread thread = new Thread(() -> {
			context.close();
			context = SpringApplication.run(NgimockerApplication.class, args.getSourceArgs());
		});

		thread.setDaemon(false);
		thread.start();
	}

}
