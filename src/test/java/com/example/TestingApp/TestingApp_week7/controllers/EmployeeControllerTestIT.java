package com.example.TestingApp.TestingApp_week7.controllers;


import com.example.TestingApp.TestingApp_week7.dto.EmployeeDTO;
import com.example.TestingApp.TestingApp_week7.entities.Employee;
import com.example.TestingApp.TestingApp_week7.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import static org.assertj.core.api.Assertions.assertThat;



class EmployeeControllerTestIT extends AbstractionIntegrationTests {



    @Autowired
    private EmployeeRepository employeeRepository;



    @BeforeEach
    void setUp() {

        employeeRepository.deleteAll();
    }






    @Test
    void testGetEmployeeById_Success(){
  Employee savedEmployee=employeeRepository.save(testEmployee);
  webTestClient.get()
          .uri("/employees/{id}",savedEmployee.getId())
          .exchange()
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$.id").isEqualTo(savedEmployee.getId())
          .jsonPath("$.email").isEqualTo(savedEmployee.getEmail());
   //       .isEqualTo(testEmployeeDTO);
//            .value(employeeDTO -> {
//                assertThat(employeeDTO.getEmail()).isEqualTo(savedEmployee.getEmail());
//                assertThat(employeeDTO.getId()).isEqualTo(savedEmployee.getId());
//
  //          });

            }

            @Test
    void testGetEmployeeById_Failure(){
        webTestClient.get()
                .uri("employees/1")
                .exchange()
                .expectStatus().isNotFound();
            }

            @Test
    void testCreateEmployee_WhenEmployeeExists_ThrowException(){
        Employee savedEmployee=employeeRepository.save(testEmployee);

        webTestClient.post()
                .uri("/employees")
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().is5xxServerError();

            }

    @Test
    void testCreateEmployee_WhenEmployeeDoesNotExist_Success(){
        testEmployeeDTO.setId(null);

        webTestClient.post()
                .uri("/employees")
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo(testEmployeeDTO.getEmail())
                .jsonPath("$.name").isEqualTo(testEmployeeDTO.getName());
    }

    @Test
    void testUpdateEmployee_WhenEmployeeDoesNotExist_ThrowException(){

        webTestClient.put()
                .uri("/employees/999")
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateEmployee_WhenAttemptingToUpdateEmployee_ThrowException(){
        Employee savedEmployee=employeeRepository.save(testEmployee);
        testEmployeeDTO.setName("pipu");
        testEmployeeDTO.setEmail("pipu@gmail.com");

        webTestClient.put()
                .uri("/employees/{id}",savedEmployee.getId())
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testUpdateEmployee_WhenEmployeeIsValid_ThenUpdateEmployee(){
        Employee savedEmployee=employeeRepository.save(testEmployee);
        testEmployeeDTO.setName("kipro");
        testEmployeeDTO.setSalary(200L);

        webTestClient.put()
                .uri("/employees/{id}",savedEmployee.getId())
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeDTO.class)
              //  .isEqualTo(testEmployeeDTO)
         .value(employeeDTO -> {
                assertThat(employeeDTO.getEmail()).isEqualTo(testEmployeeDTO.getEmail());
              assertThat(employeeDTO.getName()).isEqualTo(testEmployeeDTO.getName());
                     });
    }

    @Test
    void testDeleteEmployee_WhenEmployeeDoesNotExist_ThrowException(){
        webTestClient.delete()
                .uri("/employees/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteEmployee_WhenEmployeeExist_ThenDeleteEmployee(){
        Employee savedEmployee=employeeRepository.save(testEmployee);
        webTestClient.delete()
                .uri("/employees/{id}",savedEmployee.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

        webTestClient.delete()
                .uri("/employees/1")
                .exchange()
                .expectStatus().isNotFound();
    }

}