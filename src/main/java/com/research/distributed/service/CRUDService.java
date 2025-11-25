package com.research.distributed.service;

import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.dao.DeAnDAO;
import com.research.distributed.dao.NhanVienDAO;
import com.research.distributed.dao.NhomNCDAO;
import com.research.distributed.dao.ThamGiaDAO;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.exception.ValidationException;
import com.research.distributed.model.DeAn;
import com.research.distributed.model.NhanVien;
import com.research.distributed.model.NhomNC;
import com.research.distributed.model.ThamGia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CRUDService {
    private static final Logger logger = LoggerFactory.getLogger(CRUDService.class);

    private final NhomNCDAO nhomNCDAO;
    private final NhanVienDAO nhanVienDAO;
    private final DeAnDAO deAnDAO;
    private final ThamGiaDAO thamGiaDAO;

    public CRUDService() {
        this.nhomNCDAO = new NhomNCDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.deAnDAO = new DeAnDAO();
        this.thamGiaDAO = new ThamGiaDAO();
    }

    // ==================== NhomNC Operations ====================

    public List<NhomNC> getAllNhomNC(TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting all research groups with level: {}", level);
        return nhomNCDAO.findAll(level);
    }

    public NhomNC getNhomNCById(String maHomnc, TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting research group by ID: {}", maHomnc);
        return nhomNCDAO.findById(maHomnc, level);
    }

    public void createNhomNC(NhomNC nhomNC) throws DatabaseException, ValidationException {
        validateNhomNC(nhomNC);

        // Check if group already exists
        NhomNC existing = nhomNCDAO.findById(nhomNC.getMaHomnc(), TransparencyLevel.LOCATION_TRANSPARENCY);
        if (existing != null) {
            throw new ValidationException("Research group already exists: " + nhomNC.getMaHomnc(),
                    "maHomnc", nhomNC.getMaHomnc());
        }

        nhomNCDAO.insert(nhomNC);
        logger.info("Created research group: {}", nhomNC.getMaHomnc());
    }

    public void updateNhomNC(NhomNC nhomNC) throws DatabaseException, ValidationException {
        validateNhomNC(nhomNC);

        int updated = nhomNCDAO.update(nhomNC);
        if (updated == 0) {
            throw new ValidationException("Research group not found: " + nhomNC.getMaHomnc(),
                    "maHomnc", nhomNC.getMaHomnc());
        }
        logger.info("Updated research group: {}", nhomNC.getMaHomnc());
    }

    public void deleteNhomNC(String maHomnc) throws DatabaseException, ValidationException {
        // Check for dependent records
        List<NhanVien> employees = nhanVienDAO.findByGroup(maHomnc, TransparencyLevel.LOCATION_TRANSPARENCY);
        if (!employees.isEmpty()) {
            throw new ValidationException(
                    "Cannot delete group with " + employees.size() + " employees",
                    "maHomnc", maHomnc);
        }

        List<DeAn> projects = deAnDAO.findByGroup(maHomnc, TransparencyLevel.LOCATION_TRANSPARENCY);
        if (!projects.isEmpty()) {
            throw new ValidationException(
                    "Cannot delete group with " + projects.size() + " projects",
                    "maHomnc", maHomnc);
        }

        int deleted = nhomNCDAO.delete(maHomnc);
        if (deleted == 0) {
            throw new ValidationException("Research group not found: " + maHomnc,
                    "maHomnc", maHomnc);
        }
        logger.info("Deleted research group: {}", maHomnc);
    }

    private void validateNhomNC(NhomNC nhomNC) throws ValidationException {
        if (nhomNC.getMaHomnc() == null || nhomNC.getMaHomnc().trim().isEmpty()) {
            throw new ValidationException("Group ID is required", "maHomnc");
        }
        if (nhomNC.getTenNhomnc() == null || nhomNC.getTenNhomnc().trim().isEmpty()) {
            throw new ValidationException("Group name is required", "tenNhomnc");
        }
        if (nhomNC.getTenPhong() == null ||
                (!nhomNC.getTenPhong().equals("P1") && !nhomNC.getTenPhong().equals("P2"))) {
            throw new ValidationException("Department must be P1 or P2", "tenPhong", nhomNC.getTenPhong());
        }
    }

    // ==================== NhanVien Operations ====================

    public List<NhanVien> getAllNhanVien(TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting all employees with level: {}", level);
        return nhanVienDAO.findAll(level);
    }

    public NhanVien getNhanVienById(String maNv, TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting employee by ID: {}", maNv);
        return nhanVienDAO.findById(maNv, level);
    }

    public List<NhanVien> getNhanVienByGroup(String maHomnc, TransparencyLevel level)
            throws DatabaseException {
        logger.debug("Getting employees by group: {}", maHomnc);
        return nhanVienDAO.findByGroup(maHomnc, level);
    }

    public void createNhanVien(NhanVien nhanVien) throws DatabaseException, ValidationException {
        validateNhanVien(nhanVien);

        // Check if employee already exists
        NhanVien existing = nhanVienDAO.findById(nhanVien.getMaNv(), TransparencyLevel.LOCATION_TRANSPARENCY);
        if (existing != null) {
            throw new ValidationException("Employee already exists: " + nhanVien.getMaNv(),
                    "maNv", nhanVien.getMaNv());
        }

        // Check if group exists
        NhomNC group = nhomNCDAO.findById(nhanVien.getMaHomnc(), TransparencyLevel.LOCATION_TRANSPARENCY);
        if (group == null) {
            throw new ValidationException("Research group not found: " + nhanVien.getMaHomnc(),
                    "maHomnc", nhanVien.getMaHomnc());
        }

        nhanVienDAO.insert(nhanVien);
        logger.info("Created employee: {}", nhanVien.getMaNv());
    }

    public void updateNhanVien(NhanVien nhanVien) throws DatabaseException, ValidationException {
        validateNhanVien(nhanVien);

        int updated = nhanVienDAO.update(nhanVien);
        if (updated == 0) {
            throw new ValidationException("Employee not found: " + nhanVien.getMaNv(),
                    "maNv", nhanVien.getMaNv());
        }
        logger.info("Updated employee: {}", nhanVien.getMaNv());
    }

    public void deleteNhanVien(String maNv) throws DatabaseException, ValidationException {
        // Delete participations first
        thamGiaDAO.deleteByEmployee(maNv);

        int deleted = nhanVienDAO.delete(maNv);
        if (deleted == 0) {
            throw new ValidationException("Employee not found: " + maNv, "maNv", maNv);
        }
        logger.info("Deleted employee: {}", maNv);
    }

    private void validateNhanVien(NhanVien nhanVien) throws ValidationException {
        if (nhanVien.getMaNv() == null || nhanVien.getMaNv().trim().isEmpty()) {
            throw new ValidationException("Employee ID is required", "maNv");
        }
        if (nhanVien.getHoTen() == null || nhanVien.getHoTen().trim().isEmpty()) {
            throw new ValidationException("Employee name is required", "hoTen");
        }
        if (nhanVien.getMaHomnc() == null || nhanVien.getMaHomnc().trim().isEmpty()) {
            throw new ValidationException("Group ID is required", "maHomnc");
        }
    }

    // ==================== DeAn Operations ====================

    public List<DeAn> getAllDeAn(TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting all projects with level: {}", level);
        return deAnDAO.findAll(level);
    }

    public DeAn getDeAnById(String maDa, TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting project by ID: {}", maDa);
        return deAnDAO.findById(maDa, level);
    }

    public List<DeAn> getDeAnByGroup(String maHomnc, TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting projects by group: {}", maHomnc);
        return deAnDAO.findByGroup(maHomnc, level);
    }

    public void createDeAn(DeAn deAn) throws DatabaseException, ValidationException {
        validateDeAn(deAn);

        // Check if project already exists
        DeAn existing = deAnDAO.findById(deAn.getMaDa(), TransparencyLevel.LOCATION_TRANSPARENCY);
        if (existing != null) {
            throw new ValidationException("Project already exists: " + deAn.getMaDa(),
                    "maDa", deAn.getMaDa());
        }

        // Check if group exists
        NhomNC group = nhomNCDAO.findById(deAn.getMaHomnc(), TransparencyLevel.LOCATION_TRANSPARENCY);
        if (group == null) {
            throw new ValidationException("Research group not found: " + deAn.getMaHomnc(),
                    "maHomnc", deAn.getMaHomnc());
        }

        deAnDAO.insert(deAn);
        logger.info("Created project: {}", deAn.getMaDa());
    }

    public void updateDeAn(DeAn deAn) throws DatabaseException, ValidationException {
        validateDeAn(deAn);

        int updated = deAnDAO.update(deAn);
        if (updated == 0) {
            throw new ValidationException("Project not found: " + deAn.getMaDa(),
                    "maDa", deAn.getMaDa());
        }
        logger.info("Updated project: {}", deAn.getMaDa());
    }

    public void deleteDeAn(String maDa) throws DatabaseException, ValidationException {
        // Delete participations first
        thamGiaDAO.deleteByProject(maDa);

        int deleted = deAnDAO.delete(maDa);
        if (deleted == 0) {
            throw new ValidationException("Project not found: " + maDa, "maDa", maDa);
        }
        logger.info("Deleted project: {}", maDa);
    }

    private void validateDeAn(DeAn deAn) throws ValidationException {
        if (deAn.getMaDa() == null || deAn.getMaDa().trim().isEmpty()) {
            throw new ValidationException("Project ID is required", "maDa");
        }
        if (deAn.getTenDa() == null || deAn.getTenDa().trim().isEmpty()) {
            throw new ValidationException("Project name is required", "tenDa");
        }
        if (deAn.getMaHomnc() == null || deAn.getMaHomnc().trim().isEmpty()) {
            throw new ValidationException("Group ID is required", "maHomnc");
        }
    }

    // ==================== ThamGia Operations ====================

    public List<ThamGia> getAllThamGia(TransparencyLevel level) throws DatabaseException {
        logger.debug("Getting all participations with level: {}", level);
        return thamGiaDAO.findAll(level);
    }

    public List<ThamGia> getThamGiaByEmployee(String maNv, TransparencyLevel level)
            throws DatabaseException {
        logger.debug("Getting participations by employee: {}", maNv);
        return thamGiaDAO.findByEmployee(maNv, level);
    }

    public List<ThamGia> getThamGiaByProject(String maDa, TransparencyLevel level)
            throws DatabaseException {
        logger.debug("Getting participations by project: {}", maDa);
        return thamGiaDAO.findByProject(maDa, level);
    }

    public void createThamGia(ThamGia thamGia) throws DatabaseException, ValidationException {
        validateThamGia(thamGia);

        // Check if participation already exists
        ThamGia existing = thamGiaDAO.findById(thamGia.getMaNv(), thamGia.getMaDa(),
                TransparencyLevel.LOCATION_TRANSPARENCY);
        if (existing != null) {
            throw new ValidationException(
                    "Participation already exists for employee " + thamGia.getMaNv() +
                            " and project " + thamGia.getMaDa(),
                    "participation");
        }

        // Get employee's fragment
        String fragment = nhanVienDAO.getFragmentForEmployee(thamGia.getMaNv());
        if (fragment == null) {
            throw new ValidationException("Employee not found: " + thamGia.getMaNv(),
                    "maNv", thamGia.getMaNv());
        }

        // Check if project exists
        DeAn project = deAnDAO.findById(thamGia.getMaDa(), TransparencyLevel.LOCATION_TRANSPARENCY);
        if (project == null) {
            throw new ValidationException("Project not found: " + thamGia.getMaDa(),
                    "maDa", thamGia.getMaDa());
        }

        thamGiaDAO.insert(thamGia, fragment);
        logger.info("Created participation: {} - {}", thamGia.getMaNv(), thamGia.getMaDa());
    }

    public void deleteThamGia(String maNv, String maDa) throws DatabaseException, ValidationException {
        int deleted = thamGiaDAO.delete(maNv, maDa);
        if (deleted == 0) {
            throw new ValidationException(
                    "Participation not found for employee " + maNv + " and project " + maDa,
                    "participation");
        }
        logger.info("Deleted participation: {} - {}", maNv, maDa);
    }

    private void validateThamGia(ThamGia thamGia) throws ValidationException {
        if (thamGia.getMaNv() == null || thamGia.getMaNv().trim().isEmpty()) {
            throw new ValidationException("Employee ID is required", "maNv");
        }
        if (thamGia.getMaDa() == null || thamGia.getMaDa().trim().isEmpty()) {
            throw new ValidationException("Project ID is required", "maDa");
        }
    }
}
