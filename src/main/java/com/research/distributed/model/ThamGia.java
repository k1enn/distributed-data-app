package com.research.distributed.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ThamGia {
    private String maNv;
    private String maDa;
    private LocalDate ngayThamGia;
    private LocalDateTime createdAt;

    public ThamGia() {}

    public ThamGia(String maNv, String maDa) {
        this.maNv = maNv;
        this.maDa = maDa;
    }

    public ThamGia(String maNv, String maDa, LocalDate ngayThamGia) {
        this.maNv = maNv;
        this.maDa = maDa;
        this.ngayThamGia = ngayThamGia;
    }

    public ThamGia(String maNv, String maDa, LocalDate ngayThamGia, LocalDateTime createdAt) {
        this.maNv = maNv;
        this.maDa = maDa;
        this.ngayThamGia = ngayThamGia;
        this.createdAt = createdAt;
    }

    public String getMaNv() {
        return maNv;
    }

    public void setMaNv(String maNv) {
        this.maNv = maNv;
    }

    public String getMaDa() {
        return maDa;
    }

    public void setMaDa(String maDa) {
        this.maDa = maDa;
    }

    public LocalDate getNgayThamGia() {
        return ngayThamGia;
    }

    public void setNgayThamGia(LocalDate ngayThamGia) {
        this.ngayThamGia = ngayThamGia;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ThamGia{" +
                "maNv='" + maNv + '\'' +
                ", maDa='" + maDa + '\'' +
                ", ngayThamGia=" + ngayThamGia +
                '}';
    }
}
