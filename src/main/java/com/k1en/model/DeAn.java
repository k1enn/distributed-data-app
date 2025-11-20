package com.k1en.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DeAn {
    private final StringProperty maDA;
    private final StringProperty tenDA;
    private final StringProperty maNhom;

    public DeAn(String maDA, String tenDA, String maNhom) {
        this.maDA = new SimpleStringProperty(maDA);
        this.tenDA = new SimpleStringProperty(tenDA);
        this.maNhom = new SimpleStringProperty(maNhom);
    }

    // Constructor for query results (without manhom)
    public DeAn(String maDA, String tenDA) {
        this(maDA, tenDA, "");
    }

    // Property getters for TableView
    public StringProperty maDAProperty() {
        return maDA;
    }

    public StringProperty tenDAProperty() {
        return tenDA;
    }

    public StringProperty maNhomProperty() {
        return maNhom;
    }

    // Standard getters
    public String getMaDA() {
        return maDA.get();
    }

    public String getTenDA() {
        return tenDA.get();
    }

    public String getMaNhom() {
        return maNhom.get();
    }

    // Setters
    public void setMaDA(String value) {
        maDA.set(value);
    }

    public void setTenDA(String value) {
        tenDA.set(value);
    }

    public void setMaNhom(String value) {
        maNhom.set(value);
    }
}

