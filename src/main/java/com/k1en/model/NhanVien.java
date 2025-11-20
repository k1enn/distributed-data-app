package com.k1en.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NhanVien {
    private final StringProperty maNV;
    private final StringProperty hoTen;
    private final StringProperty maNhom;

    public NhanVien(String maNV, String hoTen, String maNhom) {
        this.maNV = new SimpleStringProperty(maNV);
        this.hoTen = new SimpleStringProperty(hoTen);
        this.maNhom = new SimpleStringProperty(maNhom);
    }

    // Property getters for TableView
    public StringProperty maNVProperty() {
        return maNV;
    }

    public StringProperty hoTenProperty() {
        return hoTen;
    }

    public StringProperty maNhomProperty() {
        return maNhom;
    }

    // Standard getters
    public String getMaNV() {
        return maNV.get();
    }

    public String getHoTen() {
        return hoTen.get();
    }

    public String getMaNhom() {
        return maNhom.get();
    }

    // Setters
    public void setMaNV(String value) {
        maNV.set(value);
    }

    public void setHoTen(String value) {
        hoTen.set(value);
    }

    public void setMaNhom(String value) {
        maNhom.set(value);
    }
}
