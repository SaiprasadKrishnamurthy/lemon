package com.sai.lemon.model;

import lombok.Data;

@Data
public class SingleValuePlot {
private String id;
private String type="singleValueDataPoint";
private Number value;
}