package com.example.TestingApp.TestingApp_week7.services.impl;

import com.example.TestingApp.TestingApp_week7.TestContainerConfiguration;
import com.example.TestingApp.TestingApp_week7.dto.EmployeeDTO;
import com.example.TestingApp.TestingApp_week7.entities.Employee;
import com.example.TestingApp.TestingApp_week7.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfiguration.class)
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee mockEmployee;
    private EmployeeDTO mockEmployeeDTO;

    @BeforeEach
    void setUp() {
        mockEmployee= Employee.builder()
                .id(1L)
                .email("saif@gmail.com")
                .name("saif")
                .salary(5000L)
                .build();
       mockEmployeeDTO= modelMapper.map(mockEmployee, EmployeeDTO.class);
    }


    @Test
    void testGetEmployeeById_WhenEmployeeExists_ThenReturnEmployee() {

 // arrange
        Long id=mockEmployee.getId();

    when(employeeRepository.findById(id)).thenReturn(Optional.of(mockEmployee)); // stubbing


 // act
        EmployeeDTO employeeDTO=employeeService.getEmployeeById(id);


 //assert
        assertThat(employeeDTO).isNotNull();
assertThat(employeeDTO.getId()).isEqualTo(id);
assertThat(employeeDTO.getEmail()).isEqualTo(mockEmployee.getEmail());
verify(employeeRepository,only()).findById(1L);

    }

    @Test
    void testCreateEmployee_WhenEmployeeIsValid_ThenCreateNewEmployee(){
        // arrange
        when(employeeRepository.findByEmail(anyString())).thenReturn(List.of());
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        //act
EmployeeDTO employeeDTO=employeeService.createNewEmployee(mockEmployeeDTO);
        //assert

        assertThat(employeeDTO).isNotNull();
        assertThat(employeeDTO.getEmail()).isEqualTo(mockEmployeeDTO.getEmail());
        ArgumentCaptor<Employee> employeeArgumentCaptor=ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeArgumentCaptor.capture());

      Employee capturedEmployee= employeeArgumentCaptor.getValue();
      assertThat(capturedEmployee.getEmail()).isEqualTo(mockEmployee.getEmail());
    }



}