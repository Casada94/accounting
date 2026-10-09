package com.asadaclan.accounting.parsers;

import com.asadaclan.accounting.models.Query;

public class QueryParser implements Parser<Query> {
    @Override
    public Query parse(String[] parts) {
        // Implement the parsing logic for Query here
        return new Query();
    }
  
}
