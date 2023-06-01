package com.bestv.pgc.beans;

import java.io.Serializable;

public class Entity<T> implements Serializable {
    public int code;
    public boolean ss;
    public String em="";
    public T dt;
    public int pageNum;
    public int count;
    public String ec;
}
