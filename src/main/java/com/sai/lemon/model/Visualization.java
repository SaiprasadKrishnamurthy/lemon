package com.sai.lemon.model;

import lombok.Data;

@Data
public class Visualization {
    private String dataPushFrequencyInSeconds = "";
    private String id = "";
    private String dataTransfomerClass = "";
    private String sql = "";
    private String sqlProviderClass = "";
    private boolean disableDatabasePolling;
    private String scaleInstances = "1";
    private String type = "";


}
