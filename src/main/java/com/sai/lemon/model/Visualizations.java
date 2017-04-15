package com.sai.lemon.model;

import lombok.Data;

@Data
public class Visualizations {
    private Pie pie;
    private Line line;
    private Multiline multiline;
    private Bar bar;
    private Json json;
}