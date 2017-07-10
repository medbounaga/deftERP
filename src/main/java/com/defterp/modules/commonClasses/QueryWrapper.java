package com.defterp.modules.commonClasses;

import java.util.HashMap;
import java.util.Map;

public class QueryWrapper {

    private final String query;
    private final Map<String, Object> parameters;

    public QueryWrapper(String query) {
        parameters = new HashMap();
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public QueryWrapper setParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }
}
