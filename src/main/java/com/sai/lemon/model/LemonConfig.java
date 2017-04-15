package com.sai.lemon.model;

import lombok.Data;

@Data
public class LemonConfig
{
    private String name;

    private String dbObjectName;

    private String dataSourceSpringBeanName;

    private Visualizations[] visualizations;

    private String dataSourceType;
}