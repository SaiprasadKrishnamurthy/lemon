package com.sai.lemon.model;

import lombok.Data;

@Data
public class Multiline
{
    private String dataPushFrequencyInSeconds;

    private String name;

    private String[] yAxisFields;

    private String xAxisField;

    public String[] getyAxisFields() {
        return yAxisFields;
    }

    public void setyAxisFields(String[] yAxisFields) {
        this.yAxisFields = yAxisFields;
    }

    public String getxAxisField() {
        return xAxisField;
    }

    public void setxAxisField(String xAxisField) {
        this.xAxisField = xAxisField;
    }
}