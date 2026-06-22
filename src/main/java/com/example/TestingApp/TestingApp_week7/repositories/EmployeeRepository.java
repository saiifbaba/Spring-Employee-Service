package com.example.TestingApp.TestingApp_week7.repositories;


import com.example.TestingApp.TestingApp_week7.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    List<Employee> findByEmail(String email);
}
