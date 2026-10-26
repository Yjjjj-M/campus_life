package com.campus.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应封装
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private int page;
    private int size;
    
    // 游标分页字段
    private Boolean hasMore;
    private Long nextCursor;

    public PageResult() {}

    public PageResult(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
