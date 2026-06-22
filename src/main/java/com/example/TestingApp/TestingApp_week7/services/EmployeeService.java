package com.example.TestingApp.TestingApp_week7.services;

import com.example.TestingApp.TestingApp_week7.dto.EmployeeDTO;

public interface EmployeeService {

    EmployeeDTO getEmployeeById(Long id);
    EmployeeDTO createNewEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    void deleteEmployee(Long id);
}
