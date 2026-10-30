package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

/**
 * One page of a list plus the totals a screen needs to draw page controls.
 * page is zero-based; size is capped by the endpoints (see MAX_PAGE_SIZE).
 */
@Data
public class PagedResponse<T> {

    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 25;

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    public static int clampPage(Integer page) {
        return (page == null || page < 0) ? 0 : page;
    }

    public static int clampSize(Integer size) {
        if (size == null || size < 1) return DEFAULT_PAGE_SIZE;
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
