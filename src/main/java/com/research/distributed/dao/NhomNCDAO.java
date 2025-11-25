package com.research.distributed.dao;

import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.model.NhomNC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NhomNCDAO extends BaseDAO<NhomNC> {

    public NhomNCDAO() {
        super("nhomnc");
    }

    @Override
    protected NhomNC mapResultSetToEntity(ResultSet rs) throws SQLException {
        NhomNC nhomNC = new NhomNC();
        nhomNC.setMaHomnc(rs.getString("manhomnc"));
        nhomNC.setTenNhomnc(rs.getString("tennhomnc"));
        nhomNC.setTenPhong(rs.getString("tenphong"));

        if (hasColumn(rs, "created_at")) {
            nhomNC.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
        }
        if (hasColumn(rs, "updated_at")) {
            nhomNC.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
        }

        return nhomNC;
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<NhomNC> findAll(TransparencyLevel level) throws DatabaseException {
        String sql = "SELECT manhomnc, tennhomnc, tenphong, created_at, updated_at FROM {TABLE}";
        return executeQueryAllFragments(sql, level);
    }

    public NhomNC findById(String maHomnc, TransparencyLevel level) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(maHomnc);
        if (fragment == null) {
            // Search all fragments
            for (String frag : connectionManager.getAllFragments()) {
                List<NhomNC> results = executeQuerySingleFragment(
                        "SELECT manhomnc, tennhomnc, tenphong, created_at, updated_at FROM {TABLE} WHERE manhomnc = ?",
                        frag, maHomnc);
                if (!results.isEmpty()) {
                    return results.get(0);
                }
            }
            return null;
        }

        List<NhomNC> results = executeQuerySingleFragment(
                "SELECT manhomnc, tennhomnc, tenphong, created_at, updated_at FROM {TABLE} WHERE manhomnc = ?",
                fragment, maHomnc);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<NhomNC> findByDepartment(String tenPhong, TransparencyLevel level) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(tenPhong.equals("P1") ? "NC01" : "NC03");
        if (fragment != null && level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
            return executeQuerySingleFragment(
                    "SELECT manhomnc, tennhomnc, tenphong, created_at, updated_at FROM {TABLE} WHERE tenphong = ?",
                    fragment, tenPhong);
        }

        // Search all fragments for location transparency
        String sql = "SELECT manhomnc, tennhomnc, tenphong, created_at, updated_at FROM {TABLE} WHERE tenphong = '" + tenPhong + "'";
        return executeQueryAllFragments(sql, level);
    }

    public void insert(NhomNC nhomNC) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(nhomNC.getMaHomnc());
        if (fragment == null) {
            // Determine fragment from department
            fragment = nhomNC.getTenPhong().equals("P1") ? "p1" : "p2";
        }

        executeInsertSingleFragment(
                "INSERT INTO {TABLE} (manhomnc, tennhomnc, tenphong) VALUES (?, ?, ?)",
                fragment,
                nhomNC.getMaHomnc(),
                nhomNC.getTenNhomnc(),
                nhomNC.getTenPhong()
        );
    }

    public int update(NhomNC nhomNC) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(nhomNC.getMaHomnc());
        if (fragment == null) {
            throw new DatabaseException("Cannot determine fragment for group: " + nhomNC.getMaHomnc());
        }

        return executeUpdateSingleFragment(
                "UPDATE {TABLE} SET tennhomnc = ?, tenphong = ?, updated_at = GETDATE() WHERE manhomnc = ?",
                fragment,
                nhomNC.getTenNhomnc(),
                nhomNC.getTenPhong(),
                nhomNC.getMaHomnc()
        );
    }

    public int delete(String maHomnc) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(maHomnc);
        if (fragment == null) {
            // Try all fragments
            int total = 0;
            for (String frag : connectionManager.getAllFragments()) {
                total += executeDeleteSingleFragment(
                        "DELETE FROM {TABLE} WHERE manhomnc = ?",
                        frag, maHomnc);
            }
            return total;
        }

        return executeDeleteSingleFragment(
                "DELETE FROM {TABLE} WHERE manhomnc = ?",
                fragment, maHomnc);
    }
}
