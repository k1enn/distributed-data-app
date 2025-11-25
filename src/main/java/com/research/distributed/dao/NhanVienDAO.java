package com.research.distributed.dao;

import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.model.NhanVien;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NhanVienDAO extends BaseDAO<NhanVien> {

    public NhanVienDAO() {
        super("nhanvien");
    }

    @Override
    protected NhanVien mapResultSetToEntity(ResultSet rs) throws SQLException {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNv(rs.getString("manv"));
        nhanVien.setHoTen(rs.getString("hoten"));
        nhanVien.setMaHomnc(rs.getString("manhomnc"));

        if (hasColumn(rs, "created_at")) {
            nhanVien.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
        }
        if (hasColumn(rs, "updated_at")) {
            nhanVien.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
        }

        return nhanVien;
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<NhanVien> findAll(TransparencyLevel level) throws DatabaseException {
        String sql = "SELECT manv, hoten, manhomnc, created_at, updated_at FROM {TABLE}";
        return executeQueryAllFragments(sql, level);
    }

    public NhanVien findById(String maNv, TransparencyLevel level) throws DatabaseException {
        // Search all fragments since we can't determine fragment from employee ID
        for (String fragment : connectionManager.getAllFragments()) {
            List<NhanVien> results = executeQuerySingleFragment(
                    "SELECT manv, hoten, manhomnc, created_at, updated_at FROM {TABLE} WHERE manv = ?",
                    fragment, maNv);
            if (!results.isEmpty()) {
                return results.get(0);
            }
        }
        return null;
    }

    public List<NhanVien> findByGroup(String maHomnc, TransparencyLevel level) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(maHomnc);
        if (fragment != null && level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
            return executeQuerySingleFragment(
                    "SELECT manv, hoten, manhomnc, created_at, updated_at FROM {TABLE} WHERE manhomnc = ?",
                    fragment, maHomnc);
        }

        // Search all fragments for location transparency
        String sql = "SELECT manv, hoten, manhomnc, created_at, updated_at FROM {TABLE} WHERE manhomnc = '" + maHomnc + "'";
        return executeQueryAllFragments(sql, level);
    }

    public void insert(NhanVien nhanVien) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(nhanVien.getMaHomnc());
        if (fragment == null) {
            throw new DatabaseException("Cannot determine fragment for group: " + nhanVien.getMaHomnc());
        }

        executeInsertSingleFragment(
                "INSERT INTO {TABLE} (manv, hoten, manhomnc) VALUES (?, ?, ?)",
                fragment,
                nhanVien.getMaNv(),
                nhanVien.getHoTen(),
                nhanVien.getMaHomnc()
        );
    }

    public int update(NhanVien nhanVien) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(nhanVien.getMaHomnc());
        if (fragment == null) {
            throw new DatabaseException("Cannot determine fragment for group: " + nhanVien.getMaHomnc());
        }

        return executeUpdateSingleFragment(
                "UPDATE {TABLE} SET hoten = ?, manhomnc = ?, updated_at = GETDATE() WHERE manv = ?",
                fragment,
                nhanVien.getHoTen(),
                nhanVien.getMaHomnc(),
                nhanVien.getMaNv()
        );
    }

    public int delete(String maNv) throws DatabaseException {
        // Try all fragments
        int total = 0;
        for (String fragment : connectionManager.getAllFragments()) {
            total += executeDeleteSingleFragment(
                    "DELETE FROM {TABLE} WHERE manv = ?",
                    fragment, maNv);
        }
        return total;
    }

    public String getFragmentForEmployee(String maNv) throws DatabaseException {
        for (String fragment : connectionManager.getAllFragments()) {
            List<NhanVien> results = executeQuerySingleFragment(
                    "SELECT manv, hoten, manhomnc FROM {TABLE} WHERE manv = ?",
                    fragment, maNv);
            if (!results.isEmpty()) {
                return fragment;
            }
        }
        return null;
    }
}
