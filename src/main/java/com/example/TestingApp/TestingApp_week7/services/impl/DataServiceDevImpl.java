package com.example.TestingApp.TestingApp_week7.services.impl;

import com.example.TestingApp.TestingApp_week7.services.DataService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service

public class DataServiceDevImpl implements DataService {

    @Override
    public String getData() {
        return "dev data";
    }
}
