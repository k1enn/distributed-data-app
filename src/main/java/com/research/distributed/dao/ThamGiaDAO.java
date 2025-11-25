package com.research.distributed.dao;

import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.model.ThamGia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ThamGiaDAO extends BaseDAO<ThamGia> {

    public ThamGiaDAO() {
        super("thamgia");
    }

    @Override
    protected ThamGia mapResultSetToEntity(ResultSet rs) throws SQLException {
        ThamGia thamGia = new ThamGia();
        thamGia.setMaNv(rs.getString("manv"));
        thamGia.setMaDa(rs.getString("mada"));

        if (hasColumn(rs, "ngaythamgia")) {
            thamGia.setNgayThamGia(rs.getDate("ngaythamgia") != null ?
                    rs.getDate("ngaythamgia").toLocalDate() : null);
        }
        if (hasColumn(rs, "created_at")) {
            thamGia.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
        }

        return thamGia;
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<ThamGia> findAll(TransparencyLevel level) throws DatabaseException {
        String sql = "SELECT manv, mada, ngaythamgia, created_at FROM {TABLE}";
        return executeQueryAllFragments(sql, level);
    }

    public ThamGia findById(String maNv, String maDa, TransparencyLevel level) throws DatabaseException {
        for (String fragment : connectionManager.getAllFragments()) {
            List<ThamGia> results = executeQuerySingleFragment(
                    "SELECT manv, mada, ngaythamgia, created_at FROM {TABLE} WHERE manv = ? AND mada = ?",
                    fragment, maNv, maDa);
            if (!results.isEmpty()) {
                return results.get(0);
            }
        }
        return null;
    }

    public List<ThamGia> findByEmployee(String maNv, TransparencyLevel level) throws DatabaseException {
        String sql = "SELECT manv, mada, ngaythamgia, created_at FROM {TABLE} WHERE manv = '" + maNv + "'";
        return executeQueryAllFragments(sql, level);
    }

    public List<ThamGia> findByProject(String maDa, TransparencyLevel level) throws DatabaseException {
        String sql = "SELECT manv, mada, ngaythamgia, created_at FROM {TABLE} WHERE mada = '" + maDa + "'";
        return executeQueryAllFragments(sql, level);
    }

    public void insert(ThamGia thamGia, String fragment) throws DatabaseException {
        if (fragment == null) {
            throw new DatabaseException("Fragment must be specified for ThamGia insert");
        }

        if (thamGia.getNgayThamGia() != null) {
            executeInsertSingleFragment(
                    "INSERT INTO {TABLE} (manv, mada, ngaythamgia) VALUES (?, ?, ?)",
                    fragment,
                    thamGia.getMaNv(),
                    thamGia.getMaDa(),
                    java.sql.Date.valueOf(thamGia.getNgayThamGia())
            );
        } else {
            executeInsertSingleFragment(
                    "INSERT INTO {TABLE} (manv, mada) VALUES (?, ?)",
                    fragment,
                    thamGia.getMaNv(),
                    thamGia.getMaDa()
            );
        }
    }

    public int delete(String maNv, String maDa) throws DatabaseException {
        int total = 0;
        for (String fragment : connectionManager.getAllFragments()) {
            total += executeDeleteSingleFragment(
                    "DELETE FROM {TABLE} WHERE manv = ? AND mada = ?",
                    fragment, maNv, maDa);
        }
        return total;
    }

    public int deleteByEmployee(String maNv) throws DatabaseException {
        int total = 0;
        for (String fragment : connectionManager.getAllFragments()) {
            total += executeDeleteSingleFragment(
                    "DELETE FROM {TABLE} WHERE manv = ?",
                    fragment, maNv);
        }
        return total;
    }

    public int deleteByProject(String maDa) throws DatabaseException {
        int total = 0;
        for (String fragment : connectionManager.getAllFragments()) {
            total += executeDeleteSingleFragment(
                    "DELETE FROM {TABLE} WHERE mada = ?",
                    fragment, maDa);
        }
        return total;
    }

    public String getFragmentForParticipation(String maNv) throws DatabaseException {
        for (String fragment : connectionManager.getAllFragments()) {
            List<ThamGia> results = executeQuerySingleFragment(
                    "SELECT manv, mada FROM {TABLE} WHERE manv = ?",
                    fragment, maNv);
            if (!results.isEmpty()) {
                return fragment;
            }
        }
        return null;
    }
}
