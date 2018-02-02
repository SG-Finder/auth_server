package com.finder.genie_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class GenieAiApplication {

	public static void main(String[] args) {
		System.getProperties().put("server.port", 8081);
		SpringApplication.run(GenieAiApplication.class, args);
	}

}

