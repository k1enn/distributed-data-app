package com.research.distributed.model;

import java.time.LocalDateTime;

public class NhomNC {
    private String maHomnc;
    private String tenNhomnc;
    private String tenPhong;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NhomNC() {}

    public NhomNC(String maHomnc, String tenNhomnc, String tenPhong) {
        this.maHomnc = maHomnc;
        this.tenNhomnc = tenNhomnc;
        this.tenPhong = tenPhong;
    }

    public NhomNC(String maHomnc, String tenNhomnc, String tenPhong,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.maHomnc = maHomnc;
        this.tenNhomnc = tenNhomnc;
        this.tenPhong = tenPhong;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getMaHomnc() {
        return maHomnc;
    }

    public void setMaHomnc(String maHomnc) {
        this.maHomnc = maHomnc;
    }

    public String getTenNhomnc() {
        return tenNhomnc;
    }

    public void setTenNhomnc(String tenNhomnc) {
        this.tenNhomnc = tenNhomnc;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
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
        return "NhomNC{" +
                "maHomnc='" + maHomnc + '\'' +
                ", tenNhomnc='" + tenNhomnc + '\'' +
                ", tenPhong='" + tenPhong + '\'' +
                '}';
    }
}
