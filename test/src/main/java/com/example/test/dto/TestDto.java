package com.example.test.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
public class TestDto {

    private String value1;
    private Integer value2;
    private TestChild testChild;

    private String nullValue1;
    private Integer nullValue2;
    private TestChild nullTestChild;

    @AllArgsConstructor
    public static class TestChild {
        private String child1;
    }
}
