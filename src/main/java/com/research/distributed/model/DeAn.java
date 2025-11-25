package com.research.distributed.model;

import java.time.LocalDateTime;

public class DeAn {
    private String maDa;
    private String tenDa;
    private String maHomnc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeAn() {}

    public DeAn(String maDa, String tenDa, String maHomnc) {
        this.maDa = maDa;
        this.tenDa = tenDa;
        this.maHomnc = maHomnc;
    }

    public DeAn(String maDa, String tenDa, String maHomnc,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.maDa = maDa;
        this.tenDa = tenDa;
        this.maHomnc = maHomnc;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getMaDa() {
        return maDa;
    }

    public void setMaDa(String maDa) {
        this.maDa = maDa;
    }

    public String getTenDa() {
        return tenDa;
    }

    public void setTenDa(String tenDa) {
        this.tenDa = tenDa;
    }

    public String getMaHomnc() {
        return maHomnc;
    }

    public void setMaHomnc(String maHomnc) {
        this.maHomnc = maHomnc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DeAn{" +
                "maDa='" + maDa + '\'' +
                ", tenDa='" + tenDa + '\'' +
                ", maHomnc='" + maHomnc + '\'' +
                '}';
    }
}
