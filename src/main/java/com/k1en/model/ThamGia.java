package com.k1en.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ThamGia {
    private final StringProperty maNV;
    private final StringProperty maDA;

    public ThamGia(String maNV, String maDA) {
        this.maNV = new SimpleStringProperty(maNV);
        this.maDA = new SimpleStringProperty(maDA);
    }

    // Property getters for TableView
    public StringProperty maNVProperty() {
        return maNV;
    }

    public StringProperty maDAProperty() {
        return maDA;
    }

    // Standard getters
    public String getMaNV() {
        return maNV.get();
    }

    public String getMaDA() {
        return maDA.get();
    }

    // Setters
    public void setMaNV(String value) {
        maNV.set(value);
    }

    public void setMaDA(String value) {
        maDA.set(value);
    }
}
