package com.research.distributed.dao;

import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.model.DeAn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DeAnDAO extends BaseDAO<DeAn> {

    public DeAnDAO() {
        super("dean");
    }

    @Override
    protected DeAn mapResultSetToEntity(ResultSet rs) throws SQLException {
        DeAn deAn = new DeAn();
        deAn.setMaDa(rs.getString("mada"));
        deAn.setTenDa(rs.getString("tenda"));
        deAn.setMaHomnc(rs.getString("manhomnc"));

        if (hasColumn(rs, "created_at")) {
            deAn.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
        }
        if (hasColumn(rs, "updated_at")) {
            deAn.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
        }

        return deAn;
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<DeAn> findAll(TransparencyLevel level) throws DatabaseException {
        String sql = "SELECT mada, tenda, manhomnc, created_at, updated_at FROM {TABLE}";
        return executeQueryAllFragments(sql, level);
    }

    public DeAn findById(String maDa, TransparencyLevel level) throws DatabaseException {
        // Search all fragments
        for (String fragment : connectionManager.getAllFragments()) {
            List<DeAn> results = executeQuerySingleFragment(
                    "SELECT mada, tenda, manhomnc, created_at, updated_at FROM {TABLE} WHERE mada = ?",
                    fragment, maDa);
            if (!results.isEmpty()) {
                return results.get(0);
            }
        }
        return null;
    }

    public List<DeAn> findByGroup(String maHomnc, TransparencyLevel level) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(maHomnc);
        if (fragment != null && level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
            return executeQuerySingleFragment(
                    "SELECT mada, tenda, manhomnc, created_at, updated_at FROM {TABLE} WHERE manhomnc = ?",
                    fragment, maHomnc);
        }

        // Search all fragments
        String sql = "SELECT mada, tenda, manhomnc, created_at, updated_at FROM {TABLE} WHERE manhomnc = '" + maHomnc + "'";
        return executeQueryAllFragments(sql, level);
    }

    public void insert(DeAn deAn) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(deAn.getMaHomnc());
        if (fragment == null) {
            throw new DatabaseException("Cannot determine fragment for group: " + deAn.getMaHomnc());
        }

        executeInsertSingleFragment(
                "INSERT INTO {TABLE} (mada, tenda, manhomnc) VALUES (?, ?, ?)",
                fragment,
                deAn.getMaDa(),
                deAn.getTenDa(),
                deAn.getMaHomnc()
        );
    }

    public int update(DeAn deAn) throws DatabaseException {
        String fragment = connectionManager.getFragmentForGroup(deAn.getMaHomnc());
        if (fragment == null) {
            throw new DatabaseException("Cannot determine fragment for group: " + deAn.getMaHomnc());
        }

        return executeUpdateSingleFragment(
                "UPDATE {TABLE} SET tenda = ?, manhomnc = ?, updated_at = GETDATE() WHERE mada = ?",
                fragment,
                deAn.getTenDa(),
                deAn.getMaHomnc(),
                deAn.getMaDa()
        );
    }

    public int delete(String maDa) throws DatabaseException {
        // Try all fragments
        int total = 0;
        for (String fragment : connectionManager.getAllFragments()) {
            total += executeDeleteSingleFragment(
                    "DELETE FROM {TABLE} WHERE mada = ?",
                    fragment, maDa);
        }
        return total;
    }

    public String getFragmentForProject(String maDa) throws DatabaseException {
        for (String fragment : connectionManager.getAllFragments()) {
            List<DeAn> results = executeQuerySingleFragment(
                    "SELECT mada, tenda, manhomnc FROM {TABLE} WHERE mada = ?",
                    fragment, maDa);
            if (!results.isEmpty()) {
                return fragment;
            }
        }
        return null;
    }
}
