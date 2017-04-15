package com.sai.lemon.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PieChart {
    private String id;
    private Map<String, Object> data = new LinkedHashMap<>();

    public void addAllData(final Map<String, Object> data) {
        data.putAll(data);
    }
}