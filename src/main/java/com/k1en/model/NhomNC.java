package com.k1en.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NhomNC {
    private final StringProperty maNhom;
    private final StringProperty tenNhom;
    private final StringProperty tenPhong;

    public NhomNC(String maNhom, String tenNhom, String tenPhong) {
        this.maNhom = new SimpleStringProperty(maNhom);
        this.tenNhom = new SimpleStringProperty(tenNhom);
        this.tenPhong = new SimpleStringProperty(tenPhong);
    }

    // Property getters for TableView
    public StringProperty maNhomProperty() {
        return maNhom;
    }

    public StringProperty tenNhomProperty() {
        return tenNhom;
    }

    public StringProperty tenPhongProperty() {
        return tenPhong;
    }

    // Standard getters
    public String getMaNhom() {
        return maNhom.get();
    }

    public String getTenNhom() {
        return tenNhom.get();
    }

    public String getTenPhong() {
        return tenPhong.get();
    }

    // Setters (if needed)
    public void setMaNhom(String value) {
        maNhom.set(value);
    }

    public void setTenNhom(String value) {
        tenNhom.set(value);
    }

    public void setTenPhong(String value) {
        tenPhong.set(value);
    }
}
