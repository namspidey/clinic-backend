package com.example.demo.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    public PageResponse(Page<T> springPage) {
        this.content       = springPage.getContent();
        this.page          = springPage.getNumber();
        this.size          = springPage.getSize();
        this.totalElements = springPage.getTotalElements();
        this.totalPages    = springPage.getTotalPages();
        this.last          = springPage.isLast();
    }
}