package com.furniturebid.mockapi.dto.response;

import java.util.List;

public class PaginatedResponse<T> {

    private List<T> data;
    private int total;
    private int page;
    private int pageSize;
    private boolean hasMore;

    public PaginatedResponse() {
    }

    public PaginatedResponse(List<T> data, int total, int page, int pageSize, boolean hasMore) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.hasMore = hasMore;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
