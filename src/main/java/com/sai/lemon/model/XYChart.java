package com.sai.lemon.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saipkri on 16/04/17.
 */
@Data
public class XYChart {
    private String id;
    private List<XYData> data = new ArrayList<>();

    public void addData(final String key, final Object value) {
        data.add(new XYData(key, (Number) value));
    }
}
