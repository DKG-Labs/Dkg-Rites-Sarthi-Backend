package com.sarthi.dto.summaryDtos;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
public class PageResponseDTO<T> {

    private List<T> content;

    private int page;
    private int size;

    private long totalElements;
    private int totalPages;

    private boolean last;


}
