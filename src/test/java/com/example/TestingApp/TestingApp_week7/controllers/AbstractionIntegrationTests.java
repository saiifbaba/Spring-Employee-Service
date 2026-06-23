package com.example.TestingApp.TestingApp_week7.controllers;


import com.example.TestingApp.TestingApp_week7.TestContainerConfiguration;
import com.example.TestingApp.TestingApp_week7.dto.EmployeeDTO;
import com.example.TestingApp.TestingApp_week7.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient(timeout = "100000")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainerConfiguration.class)

public class AbstractionIntegrationTests {
    @Autowired
     WebTestClient webTestClient;

   Employee testEmployee =  Employee.builder()

            .email("saif@gmail.com")
                .name("saif")
                .salary(5000L)
                .build();

    EmployeeDTO testEmployeeDTO= EmployeeDTO.builder()
            .id(1L)
                .email("saif@gmail.com")
                .name("saif")
                .salary(5000L)
                .build();
}
