package com.example.TestingApp.TestingApp_week7.controllers;

import com.example.TestingApp.TestingApp_week7.dto.EmployeeDTO;
import com.example.TestingApp.TestingApp_week7.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        log.info("Received request to fetch employee with id: {}", id);
        EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employeeDTO);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("Received request to create employee with email: {}", employeeDTO.getEmail());
        EmployeeDTO createdEmployeeDto = employeeService.createNewEmployee(employeeDTO);
        return new ResponseEntity<>(createdEmployeeDto,HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id,
                                                      @RequestBody EmployeeDTO employeeDTO) {
        log.info("Received request to update employee with id: {}", id);
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("Received request to delete employee with id: {}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
