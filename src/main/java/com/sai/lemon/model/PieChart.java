package com.sai.lemon.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PieChart {
    private String id;
    private String type="pie";
    private Map<String, Object> data = new LinkedHashMap<>();

    public void addData(final String key, final Object value) {
        data.put(key, value);
    }
}