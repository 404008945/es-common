package com.xishan.store.escommon.page;

import java.util.List;

public class EsPage<T> {

    private int pageNo;

    private int pageSize;

    private long total;

    private List<T> data;

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "EsPage{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", total=" + total +
                ", data=" + data +
                '}';
    }
}
