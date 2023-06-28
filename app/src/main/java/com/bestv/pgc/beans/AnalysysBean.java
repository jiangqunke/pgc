package com.bestv.pgc.beans;

public class AnalysysBean {
    private String refer_tab;
    private String ex_id;
    private String request_id;
    private int request_item_rank;

    public String getRefer_tab() {
        return refer_tab;
    }

    public void setRefer_tab(String refer_tab) {
        this.refer_tab = refer_tab;
    }

    public String getEx_id() {
        return ex_id;
    }

    public void setEx_id(String ex_id) {
        this.ex_id = ex_id;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public int getRequest_item_rank() {
        return request_item_rank;
    }

    public void setRequest_item_rank(int request_item_rank) {
        this.request_item_rank = request_item_rank;
    }
}
