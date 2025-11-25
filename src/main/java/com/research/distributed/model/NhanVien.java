package com.research.distributed.model;

import java.time.LocalDateTime;

public class NhanVien {
    private String maNv;
    private String hoTen;
    private String maHomnc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NhanVien() {}

    public NhanVien(String maNv, String hoTen, String maHomnc) {
        this.maNv = maNv;
        this.hoTen = hoTen;
        this.maHomnc = maHomnc;
    }

    public NhanVien(String maNv, String hoTen, String maHomnc,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.maNv = maNv;
        this.hoTen = hoTen;
        this.maHomnc = maHomnc;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getMaNv() {
        return maNv;
    }

    public void setMaNv(String maNv) {
        this.maNv = maNv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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
        return "NhanVien{" +
                "maNv='" + maNv + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", maHomnc='" + maHomnc + '\'' +
                '}';
    }
}
