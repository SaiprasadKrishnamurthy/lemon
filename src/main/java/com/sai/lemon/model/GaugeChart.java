package com.sai.lemon.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class GaugeChart {
private String id;
private String type="gauge";
private Number minValue;
private Number maxValue;
private Number selectedValue;
}