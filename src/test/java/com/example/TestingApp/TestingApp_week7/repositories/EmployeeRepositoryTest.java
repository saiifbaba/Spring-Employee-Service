package com.example.TestingApp.TestingApp_week7.repositories;

import com.example.TestingApp.TestingApp_week7.TestContainerConfiguration;
import com.example.TestingApp.TestingApp_week7.entities.Employee;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@Import(TestContainerConfiguration.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;
    private Employee employee;

    @BeforeEach
    void setUp(){
      employee = Employee.builder()
              .name("John")
              .email("jphn@gmail.com")
              .salary(2500L)
              .build();
    }


    @Test
    void testFindByEmail_WhenEmailIsPresent_ThenReturnEmployee() {
        //Arrange or Given
        employeeRepository.save(employee);

        //Act or When
      List<Employee> employeeList= employeeRepository.findByEmail(employee.getEmail());

      //Assert or Then

       assertThat(employeeList).isNotNull();
      assertThat(employeeList).isNotEmpty();
      assertThat(employeeList.get(0).getEmail()).isEqualTo(employee.getEmail());



    }

    @Test
    void testFindByEmail_WhenEmailIsNotValid_ThenReturnEmptyList() {
        //Arrange // Given
        String email="NOtFound EMail 123@gmail.com";

        //Act
        List<Employee> employeeList=employeeRepository.findByEmail(email);

        //assert
        assertThat(employeeList).isNotNull();
         assertThat(employeeList).isEmpty();

    }
}