package com.example.TestingApp.TestingApp_week7.services.impl;

import com.example.TestingApp.TestingApp_week7.dto.EmployeeDTO;
import com.example.TestingApp.TestingApp_week7.entities.Employee;
import com.example.TestingApp.TestingApp_week7.exceptions.ResourceNotFoundException;
import com.example.TestingApp.TestingApp_week7.repositories.EmployeeRepository;
import com.example.TestingApp.TestingApp_week7.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        log.info("Fetching employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });

        log.info("Employee found with email: {}", employee.getEmail());
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    @Override
    public EmployeeDTO createNewEmployee(EmployeeDTO employeeDTO) {
        log.info("Creating new employee with email: {}", employeeDTO.getEmail());
        List<Employee> existingEmployees = employeeRepository.findByEmail(employeeDTO.getEmail());

        if (!existingEmployees.isEmpty()) {
            log.error("Employee already exists with email: {}", employeeDTO.getEmail());
            throw new RuntimeException("Employee already exists with email: " + employeeDTO.getEmail());
        }

        Employee employee = modelMapper.map(employeeDTO, Employee.class);


        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with id: {}", savedEmployee.getId());

        return modelMapper.map(savedEmployee, EmployeeDTO.class);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        log.info("Updating employee with id: {}", id);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });

        if (!existingEmployee.getEmail().equals(employeeDTO.getEmail())) {
            log.error("Attempted to update  with email for employee with Id: {}", id);
            throw new RuntimeException("The Email of the employee cannot be updated");
        }


        modelMapper.map(employeeDTO, existingEmployee);
       existingEmployee.setId(id);

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        log.info("Employee updated successfully with id: {}", id);

        return modelMapper.map(updatedEmployee, EmployeeDTO.class);
    }

    @Override
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);
         boolean exists = employeeRepository.existsById(id);

         if(!exists) {
             log.error("Employee not found with id: {}", id);
             throw new ResourceNotFoundException("Employee not found with id: " + id);
         }

        employeeRepository.deleteById(id);
        log.info("Employee deleted successfully with id: {}", id);
    }
}
