package com.test.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> data;
    private long count;
}