package com.sai.lemon.model;

import lombok.Data;

@Data
public class Line
{
    private String name;

    private String xAxisField;

    private String dataPushFrequencyInSeconds;

    private String yAxisField;

    private String dataTransfomerClass;

    public String getxAxisField() {
        return xAxisField;
    }

    public void setxAxisField(String xAxisField) {
        this.xAxisField = xAxisField;
    }

    public String getyAxisField() {
        return yAxisField;
    }

    public void setyAxisField(String yAxisField) {
        this.yAxisField = yAxisField;
    }
}