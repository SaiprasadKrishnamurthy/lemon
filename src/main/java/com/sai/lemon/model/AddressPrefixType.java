package com.sai.lemon.model;

/**
 * Created by saipkri on 14/04/17.
 */
public enum AddressPrefixType {
    FETCH_DATA("FETCH_DATA:"), FETCH_DATA_ONCE("FETCH_DATA_ONCE:"), AUDIT_DATA("AUDIT_DATA:"), TRANSFORM_DATA("TRANSFORM_DATA:"), CLEAR_CACHE("CLEAR_CACHE:"), OUTPUT_DISPATCHER("OUTPUT_DISPATCHER:"), VISUALIZER_CONVERSION("VISUALIZER_CONVERSION:"), OUTPUT("OUTPUT");

    private final String value;

    AddressPrefixType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String address(final String config) {
        return this.getValue() + config;
    }
}
