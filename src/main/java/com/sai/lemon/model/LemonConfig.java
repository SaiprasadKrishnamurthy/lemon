package com.sai.lemon.model;

import lombok.Data;

@Data
public class LemonConfig
{
    private String name;

    private String jdbcTemplateName;

    private Visualization[] visualizations;

}