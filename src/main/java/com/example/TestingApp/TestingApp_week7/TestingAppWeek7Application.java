package com.example.TestingApp.TestingApp_week7;

import com.example.TestingApp.TestingApp_week7.services.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor
@SpringBootApplication
public class TestingAppWeek7Application implements CommandLineRunner {

	//private final DataService dataService;

	@Value("${my.variable}")
	public String myVariable;

	public static void main(String[] args) {
		SpringApplication.run(TestingAppWeek7Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception{

		System.out.println("My Variable: " + myVariable);

//		System.out.println("The data is :"+ dataService.getData());

	}



}
