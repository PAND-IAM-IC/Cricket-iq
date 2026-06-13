package com.cricketiq.cricketiq;

import org.springframework.boot.SpringApplication;

public class TestCricketiqApplication {

	public static void main(String[] args) {
		SpringApplication.from(CricketiqApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
