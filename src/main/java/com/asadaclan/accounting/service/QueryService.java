package com.asadaclan.accounting.service;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.models.ReceivedMessage;

@Service
public class QueryService {

    // private Dao dao;

    public String answer(ReceivedMessage message){
        // List<Rows> rows = dao.findAll(message);
        return "results from DB";

    }

}
