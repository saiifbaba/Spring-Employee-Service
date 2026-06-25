package com.example.TestingApp.TestingApp_week7.services.impl;

import com.example.TestingApp.TestingApp_week7.services.DataService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class DataServiceProdimpl implements DataService {

    @Override
    public String getData() {
        return "Prod Data";
    }
}
