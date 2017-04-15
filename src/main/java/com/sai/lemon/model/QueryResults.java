package com.sai.lemon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 16/04/17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryResults {
    List<Map<String, Object>> results;
}
