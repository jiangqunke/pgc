package com.bestv.pgc.beans;

public class FunctionSpeedModel {
    private String name;
    private float value;
    private boolean isSelect;

    public FunctionSpeedModel(String name, float value, boolean isSelect) {
        this.name = name;
        this.value = value;
        this.isSelect = isSelect;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
