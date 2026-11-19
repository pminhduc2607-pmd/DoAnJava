import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLyCaPhe {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhap().setVisible(true));
    }
}

class KetNoi {
    static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QLQuanCaPhe;encrypt=false;trustServerCertificate=true";
    static final String USER = "sv";
    static final String PASS = "Sv@12345";

    static {
        java.security.Security.setProperty("jdk.tls.disabledAlgorithms",
                "SSLv3, RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static int chay(String sql, Object... p) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
            return ps.executeUpdate();
        }
    }

    public static void nap(DefaultTableModel m, String sql, Object... p) throws SQLException {
        m.setRowCount(0);
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] r = new Object[n];
                    for (int i = 0; i < n; i++) r[i] = rs.getObject(i + 1);
                    m.addRow(r);
                }
            }
        }
    }

    public static Object layMot(String sql, Object... p) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        }
    }
}

class DangNhap extends JFrame {
    public static String maNV, hoTen, chucVu;

    private JTextField txtMa = new JTextField();
    private JPasswordField txtMK = new JPasswordField();

    public DangNhap() {
        setTitle("Đăng nhập");
        setSize(380, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.BLUE);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(2, 2, 5, 15));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        pnlInput.add(new JLabel("Mã nhân viên:"));
        pnlInput.add(txtMa);
        pnlInput.add(new JLabel("Mật khẩu:"));
        pnlInput.add(txtMK);
        add(pnlInput, BorderLayout.CENTER);

        JButton btnDN = new JButton("Đăng nhập");
        JButton btnThoat = new JButton("Thoát");
        JPanel pnlBtn = new JPanel();
        pnlBtn.add(btnDN);
        pnlBtn.add(btnThoat);
        add(pnlBtn, BorderLayout.SOUTH);

        btnDN.addActionListener(e -> dangNhap());
        txtMK.addActionListener(e -> dangNhap());
        btnThoat.addActionListener(e -> System.exit(0));
    }

    private void dangNhap() {
        String ma = txtMa.getText().trim();
        String mk = new String(txtMK.getPassword());
        if (ma.isEmpty() || mk.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã nhân viên và mật khẩu!");
            return;
        }
        String sql = "SELECT HoTen, ChucVu FROM NhanVien WHERE MaNV = ? AND MatKhau = ?";
        try (Connection c = KetNoi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, mk);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maNV = ma;
                    hoTen = rs.getString(1);
                    chucVu = rs.getString(2);
                    new MainForm().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Sai mã nhân viên hoặc mật khẩu!");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối: " + ex.getMessage());
        }
    }

}

class MainForm extends JFrame {

    public MainForm() {
        setTitle("Quản lý quán cà phê");
        setSize(420, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel("QUẢN LÝ QUÁN CÀ PHÊ", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.BLUE);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnl = new JPanel(new GridLayout(5, 1, 10, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        JButton btnMon = new JButton("Quản lý món");
        JButton btnBan = new JButton("Quản lý bàn");
        JButton btnNV = new JButton("Quản lý nhân viên");
        JButton btnHD = new JButton("Lập hóa đơn / Hóa đơn");
        JButton btnDX = new JButton("Đăng xuất");
        pnl.add(btnMon);
        pnl.add(btnBan);
        pnl.add(btnNV);
        pnl.add(btnHD);
        pnl.add(btnDX);
        add(pnl, BorderLayout.CENTER);

        JLabel lblUser = new JLabel("Xin chào: " + DangNhap.hoTen + " (" + DangNhap.chucVu + ")", JLabel.CENTER);
        add(lblUser, BorderLayout.SOUTH);

        btnNV.setEnabled(DangNhap.chucVu != null && DangNhap.chucVu.startsWith("Quản"));

        btnMon.addActionListener(e -> new FormMon().setVisible(true));
        btnBan.addActionListener(e -> new FormBan().setVisible(true));
        btnNV.addActionListener(e -> new FormNhanVien().setVisible(true));
        btnHD.addActionListener(e -> new FormHoaDon().setVisible(true));
        btnDX.addActionListener(e -> {
            new DangNhap().setVisible(true);
            dispose();
        });
    }
}

class FormMon extends JFrame {
    private JTextField txtMa = new JTextField();
    private JTextField txtTen = new JTextField();
    private JTextField txtGia = new JTextField();
    private JTextField txtTim = new JTextField(12);
    private JComboBox<String> cboDVT = new JComboBox<>(new String[]{"Ly", "Cái", "Phần"});
    private JComboBox<String> cboLoai = new JComboBox<>();
    private ArrayList<String> dsMaLoai = new ArrayList<>();
    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã món", "Tên món", "ĐVT", "Đơn giá", "Loại món"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private JTable table = new JTable(model);

    public FormMon() {
        setTitle("Quản lý món");
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel("QUẢN LÝ MÓN", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.BLUE);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(5, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông tin món"));
        pnlInput.add(new JLabel("Mã món:"));
        pnlInput.add(txtMa);
        pnlInput.add(new JLabel("Tên món:"));
        pnlInput.add(txtTen);
        pnlInput.add(new JLabel("Đơn vị tính:"));
        pnlInput.add(cboDVT);
        pnlInput.add(new JLabel("Đơn giá:"));
        pnlInput.add(txtGia);
        pnlInput.add(new JLabel("Loại món:"));
        pnlInput.add(cboLoai);

        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnLamMoi = new JButton("Làm mới");
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnDong = new JButton("Đóng");
        JPanel pnlBtn = new JPanel(new FlowLayout());
        pnlBtn.add(btnThem);
        pnlBtn.add(btnSua);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);
        pnlBtn.add(new JLabel("Mã/Tên món:"));
        pnlBtn.add(txtTim);
        pnlBtn.add(btnTim);
        pnlBtn.add(btnDong);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlBtn, BorderLayout.SOUTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(5, 5));
        pnlCenter.add(pnlTop, BorderLayout.NORTH);
        pnlCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                txtMa.setText(model.getValueAt(r, 0).toString());
                txtTen.setText(model.getValueAt(r, 1).toString());
                cboDVT.setSelectedItem(model.getValueAt(r, 2).toString());
                txtGia.setText(model.getValueAt(r, 3).toString());
                cboLoai.setSelectedItem(model.getValueAt(r, 4).toString());
            }
        });

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> lamMoi());
        btnTim.addActionListener(e -> taiDuLieu(txtTim.getText().trim()));
        btnDong.addActionListener(e -> dispose());

        napLoai();
        taiDuLieu("");
    }

    private void tb(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void napLoai() {
        cboLoai.removeAllItems();
        dsMaLoai.clear();
        try (Connection c = KetNoi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT MaLoai, TenLoai FROM LoaiMon")) {
            while (rs.next()) {
                dsMaLoai.add(rs.getString(1));
                cboLoai.addItem(rs.getString(2));
            }
        } catch (SQLException ex) {
            tb("Lỗi tải loại món: " + ex.getMessage());
        }
    }

    private void taiDuLieu(String tk) {
        String sql = "SELECT m.MaMon, m.TenMon, m.DonViTinh, CAST(m.DonGia AS DECIMAL(12,0)), l.TenLoai "
                + "FROM Mon m JOIN LoaiMon l ON m.MaLoai = l.MaLoai";
        try {
            if (tk.isEmpty()) {
                KetNoi.nap(model, sql);
            } else {
                KetNoi.nap(model, sql + " WHERE m.MaMon LIKE ? OR m.TenMon LIKE ?", "%" + tk + "%", "%" + tk + "%");
            }
        } catch (SQLException ex) {
            tb("Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private double layGia() {
        double gia;
        try {
            gia = Double.parseDouble(txtGia.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Đơn giá phải là số!");
        }
        if (gia < 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn hoặc bằng 0!");
        }
        return gia;
    }

    private void kiemTra() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()
                || txtGia.getText().trim().isEmpty() || cboLoai.getSelectedIndex() < 0) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin!");
        }
    }

    private void them() {
        try {
            kiemTra();
            double gia = layGia();
            String ma = txtMa.getText().trim();
            if (KetNoi.layMot("SELECT 1 FROM Mon WHERE MaMon = ?", ma) != null) {
                tb("Mã món đã tồn tại!");
                return;
            }
            KetNoi.chay("INSERT INTO Mon VALUES (?,?,?,?,?)", ma, txtTen.getText().trim(),
                    cboDVT.getSelectedItem().toString(), gia, dsMaLoai.get(cboLoai.getSelectedIndex()));
            tb("Thêm thành công!");
            lamMoi();
        } catch (IllegalArgumentException ex) {
            tb(ex.getMessage());
        } catch (SQLException ex) {
            tb("Thêm thất bại: " + ex.getMessage());
        }
    }

    private void sua() {
        try {
            kiemTra();
            double gia = layGia();
            int kq = KetNoi.chay("UPDATE Mon SET TenMon=?, DonViTinh=?, DonGia=?, MaLoai=? WHERE MaMon=?",
                    txtTen.getText().trim(), cboDVT.getSelectedItem().toString(), gia,
                    dsMaLoai.get(cboLoai.getSelectedIndex()), txtMa.getText().trim());
            if (kq > 0) {
                tb("Sửa thành công!");
                lamMoi();
            } else {
                tb("Sửa thất bại: không tìm thấy mã món!");
            }
        } catch (IllegalArgumentException ex) {
            tb(ex.getMessage());
        } catch (SQLException ex) {
            tb("Sửa thất bại: " + ex.getMessage());
        }
    }

    private void xoa() {
        int r = table.getSelectedRow();
        if (r < 0) {
            tb("Hãy chọn món cần xóa!");
            return;
        }
        String ma = model.getValueAt(r, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xóa món " + ma + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            KetNoi.chay("DELETE FROM Mon WHERE MaMon = ?", ma);
            tb("Xóa thành công!");
            lamMoi();
        } catch (SQLException ex) {
            tb("Xóa thất bại (món có thể đã nằm trong hóa đơn): " + ex.getMessage());
        }
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtGia.setText("");
        txtTim.setText("");
        cboDVT.setSelectedIndex(0);
        if (cboLoai.getItemCount() > 0) cboLoai.setSelectedIndex(0);
        table.clearSelection();
        taiDuLieu("");
    }
}

class FormBan extends JFrame {
    private JTextField txtMa = new JTextField();
    private JTextField txtTen = new JTextField();
    private JTextField txtTim = new JTextField(12);
    private JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{"Trống", "Có khách"});
    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã bàn", "Tên bàn", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private JTable table = new JTable(model);

    public FormBan() {
        setTitle("Quản lý bàn");
        setSize(700, 520);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel("QUẢN LÝ BÀN", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.BLUE);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông tin bàn"));
        pnlInput.add(new JLabel("Mã bàn:"));
        pnlInput.add(txtMa);
        pnlInput.add(new JLabel("Tên bàn:"));
        pnlInput.add(txtTen);
        pnlInput.add(new JLabel("Trạng thái:"));
        pnlInput.add(cboTrangThai);

        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnLamMoi = new JButton("Làm mới");
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnDong = new JButton("Đóng");
        JPanel pnlBtn = new JPanel(new FlowLayout());
        pnlBtn.add(btnThem);
        pnlBtn.add(btnSua);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);
        pnlBtn.add(new JLabel("Mã/Tên bàn:"));
        pnlBtn.add(txtTim);
        pnlBtn.add(btnTim);
        pnlBtn.add(btnDong);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlBtn, BorderLayout.SOUTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(5, 5));
        pnlCenter.add(pnlTop, BorderLayout.NORTH);
        pnlCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                txtMa.setText(model.getValueAt(r, 0).toString());
                txtTen.setText(model.getValueAt(r, 1).toString());
                cboTrangThai.setSelectedItem(model.getValueAt(r, 2).toString());
            }
        });

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> lamMoi());
        btnTim.addActionListener(e -> taiDuLieu(txtTim.getText().trim()));
        btnDong.addActionListener(e -> dispose());

        taiDuLieu("");
    }

    private void tb(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void taiDuLieu(String tk) {
        String sql = "SELECT MaBan, TenBan, TrangThai FROM Ban";
        try {
            if (tk.isEmpty()) {
                KetNoi.nap(model, sql);
            } else {
                KetNoi.nap(model, sql + " WHERE MaBan LIKE ? OR TenBan LIKE ?", "%" + tk + "%", "%" + tk + "%");
            }
        } catch (SQLException ex) {
            tb("Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void kiemTra() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin!");
        }
    }

    private void them() {
        try {
            kiemTra();
            String ma = txtMa.getText().trim();
            if (KetNoi.layMot("SELECT 1 FROM Ban WHERE MaBan = ?", ma) != null) {
                tb("Mã bàn đã tồn tại!");
                return;
            }
            KetNoi.chay("INSERT INTO Ban VALUES (?,?,?)", ma, txtTen.getText().trim(),
                    cboTrangThai.getSelectedItem().toString());
            tb("Thêm thành công!");
            lamMoi();
        } catch (IllegalArgumentException ex) {
            tb(ex.getMessage());
        } catch (SQLException ex) {
            tb("Thêm thất bại: " + ex.getMessage());
        }
    }

    private void sua() {
        try {
            kiemTra();
            int kq = KetNoi.chay("UPDATE Ban SET TenBan=?, TrangThai=? WHERE MaBan=?",
                    txtTen.getText().trim(), cboTrangThai.getSelectedItem().toString(), txtMa.getText().trim());
            if (kq > 0) {
                tb("Sửa thành công!");
                lamMoi();
            } else {
                tb("Sửa thất bại: không tìm thấy mã bàn!");
            }
        } catch (IllegalArgumentException ex) {
            tb(ex.getMessage());
        } catch (SQLException ex) {
            tb("Sửa thất bại: " + ex.getMessage());
        }
    }

    private void xoa() {
        int r = table.getSelectedRow();
        if (r < 0) {
            tb("Hãy chọn bàn cần xóa!");
            return;
        }
        String ma = model.getValueAt(r, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xóa bàn " + ma + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            KetNoi.chay("DELETE FROM Ban WHERE MaBan = ?", ma);
            tb("Xóa thành công!");
            lamMoi();
        } catch (SQLException ex) {
            tb("Xóa thất bại (bàn có thể đã có hóa đơn): " + ex.getMessage());
        }
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtTim.setText("");
        cboTrangThai.setSelectedIndex(0);
        table.clearSelection();
        taiDuLieu("");
    }
}

class FormNhanVien extends JFrame {
    private JTextField txtMa = new JTextField();
    private JTextField txtTen = new JTextField();
    private JTextField txtSDT = new JTextField();
    private JTextField txtMK = new JTextField();
    private JTextField txtTim = new JTextField(12);
    private JComboBox<String> cboChucVu = new JComboBox<>(new String[]{"Quản lý", "Thu ngân", "Pha chế"});
    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã NV", "Họ tên", "Số điện thoại", "Chức vụ", "Mật khẩu"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private JTable table = new JTable(model);

    public FormNhanVien() {
        setTitle("Quản lý nhân viên");
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.BLUE);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(5, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));
        pnlInput.add(new JLabel("Mã nhân viên:"));
        pnlInput.add(txtMa);
        pnlInput.add(new JLabel("Họ tên:"));
        pnlInput.add(txtTen);
        pnlInput.add(new JLabel("Số điện thoại:"));
        pnlInput.add(txtSDT);
        pnlInput.add(new JLabel("Chức vụ:"));
        pnlInput.add(cboChucVu);
        pnlInput.add(new JLabel("Mật khẩu:"));
        pnlInput.add(txtMK);

        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnLamMoi = new JButton("Làm mới");
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnDong = new JButton("Đóng");
        JPanel pnlBtn = new JPanel(new FlowLayout());
        pnlBtn.add(btnThem);
        pnlBtn.add(btnSua);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);
        pnlBtn.add(new JLabel("Mã/Họ tên:"));
        pnlBtn.add(txtTim);
        pnlBtn.add(btnTim);
        pnlBtn.add(btnDong);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlBtn, BorderLayout.SOUTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(5, 5));
        pnlCenter.add(pnlTop, BorderLayout.NORTH);
        pnlCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                txtMa.setText(model.getValueAt(r, 0).toString());
                txtTen.setText(model.getValueAt(r, 1).toString());
                txtSDT.setText(model.getValueAt(r, 2).toString());
                cboChucVu.setSelectedItem(model.getValueAt(r, 3).toString());
                txtMK.setText(model.getValueAt(r, 4).toString());
            }
        });

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> lamMoi());
        btnTim.addActionListener(e -> taiDuLieu(txtTim.getText().trim()));
        btnDong.addActionListener(e -> dispose());

        taiDuLieu("");
    }

    private void tb(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void taiDuLieu(String tk) {
        String sql = "SELECT MaNV, HoTen, SoDienThoai, ChucVu, MatKhau FROM NhanVien";
        try {
            if (tk.isEmpty()) {
                KetNoi.nap(model, sql);
            } else {
                KetNoi.nap(model, sql + " WHERE MaNV LIKE ? OR HoTen LIKE ?", "%" + tk + "%", "%" + tk + "%");
            }
        } catch (SQLException ex) {
            tb("Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void kiemTra() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()
                || txtSDT.getText().trim().isEmpty() || txtMK.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin!");
        }
        if (!txtSDT.getText().trim().matches("\\d{9,11}")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 9 đến 11 chữ số!");
        }
    }

    private void them() {
        try {
            kiemTra();
            String ma = txtMa.getText().trim();
            if (KetNoi.layMot("SELECT 1 FROM NhanVien WHERE MaNV = ?", ma) != null) {
                tb("Mã nhân viên đã tồn tại!");
                return;
            }
            KetNoi.chay("INSERT INTO NhanVien VALUES (?,?,?,?,?)", ma, txtTen.getText().trim(),
                    txtSDT.getText().trim(), cboChucVu.getSelectedItem().toString(), txtMK.getText().trim());
            tb("Thêm thành công!");
            lamMoi();
        } catch (IllegalArgumentException ex) {
            tb(ex.getMessage());
        } catch (SQLException ex) {
            tb("Thêm thất bại: " + ex.getMessage());
        }
    }

    private void sua() {
        try {
            kiemTra();
            int kq = KetNoi.chay("UPDATE NhanVien SET HoTen=?, SoDienThoai=?, ChucVu=?, MatKhau=? WHERE MaNV=?",
                    txtTen.getText().trim(), txtSDT.getText().trim(), cboChucVu.getSelectedItem().toString(),
                    txtMK.getText().trim(), txtMa.getText().trim());
            if (kq > 0) {
                tb("Sửa thành công!");
                lamMoi();
            } else {
                tb("Sửa thất bại: không tìm thấy mã nhân viên!");
            }
        } catch (IllegalArgumentException ex) {
            tb(ex.getMessage());
        } catch (SQLException ex) {
            tb("Sửa thất bại: " + ex.getMessage());
        }
    }

    private void xoa() {
        int r = table.getSelectedRow();
        if (r < 0) {
            tb("Hãy chọn nhân viên cần xóa!");
            return;
        }
        String ma = model.getValueAt(r, 0).toString();
        if (ma.equals(DangNhap.maNV)) {
            tb("Không thể xóa tài khoản đang đăng nhập!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa nhân viên " + ma + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            KetNoi.chay("DELETE FROM NhanVien WHERE MaNV = ?", ma);
            tb("Xóa thành công!");
            lamMoi();
        } catch (SQLException ex) {
            tb("Xóa thất bại (nhân viên có thể đã lập hóa đơn): " + ex.getMessage());
        }
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtSDT.setText("");
        txtMK.setText("");
        txtTim.setText("");
        cboChucVu.setSelectedIndex(0);
        table.clearSelection();
        taiDuLieu("");
    }
}

class FormHoaDon extends JFrame {
    private JTextField txtMaHD = new JTextField();
    private JTextField txtNgay = new JTextField();
    private JTextField txtSL = new JTextField("1");
    private JTextField txtTim = new JTextField(12);
    private JComboBox<String> cboBan = new JComboBox<>();
    private JComboBox<String> cboMon = new JComboBox<>();
    private ArrayList<String> dsMaBan = new ArrayList<>();
    private ArrayList<String> dsMaMon = new ArrayList<>();
    private ArrayList<Long> dsGia = new ArrayList<>();
    private JLabel lblTong = new JLabel("Tổng tiền: 0 VNĐ", JLabel.RIGHT);

    private DefaultTableModel gio = taoModel("Mã món", "Tên món", "Số lượng", "Đơn giá", "Thành tiền");
    private DefaultTableModel hd = taoModel("Mã HD", "Ngày lập", "Nhân viên", "Bàn", "Tổng tiền");
    private DefaultTableModel ct = taoModel("Tên món", "Số lượng", "Đơn giá", "Thành tiền");
    private JTable tblGio = new JTable(gio);
    private JTable tblHD = new JTable(hd);
    private JTable tblCT = new JTable(ct);

    private static DefaultTableModel taoModel(String... cot) {
        return new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    public FormHoaDon() {
        setTitle("Hóa đơn");
        setSize(850, 650);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel("QUẢN LÝ HÓA ĐƠN", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.BLUE);
        add(lblTitle, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Lập hóa đơn", taoTabLap());
        tabs.addTab("Danh sách hóa đơn", taoTabDS());
        add(tabs, BorderLayout.CENTER);

        napBan();
        napMon();
        lamMoiHD();
    }

    private JPanel taoTabLap() {
        txtMaHD.setEditable(false);
        JPanel pnlInput = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông tin hóa đơn"));
        pnlInput.add(new JLabel("Mã hóa đơn:"));
        pnlInput.add(txtMaHD);
        pnlInput.add(new JLabel("Ngày lập (yyyy-MM-dd):"));
        pnlInput.add(txtNgay);
        pnlInput.add(new JLabel("Nhân viên lập:"));
        pnlInput.add(new JLabel(DangNhap.hoTen));
        pnlInput.add(new JLabel("Bàn:"));
        pnlInput.add(cboBan);
        pnlInput.add(new JLabel("Món:"));
        pnlInput.add(cboMon);
        pnlInput.add(new JLabel("Số lượng:"));
        pnlInput.add(txtSL);

        JButton btnThemMon = new JButton("Thêm món");
        JButton btnBoMon = new JButton("Bỏ món");
        JButton btnLuu = new JButton("Lưu hóa đơn");
        JButton btnLamMoi = new JButton("Làm mới");
        JPanel pnlBtn = new JPanel(new FlowLayout());
        pnlBtn.add(btnThemMon);
        pnlBtn.add(btnBoMon);
        pnlBtn.add(btnLuu);
        pnlBtn.add(btnLamMoi);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlBtn, BorderLayout.SOUTH);

        lblTong.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblGio), BorderLayout.CENTER);
        pnl.add(lblTong, BorderLayout.SOUTH);

        btnThemMon.addActionListener(e -> themMon());
        btnBoMon.addActionListener(e -> boMon());
        btnLuu.addActionListener(e -> luu());
        btnLamMoi.addActionListener(e -> lamMoiHD());
        return pnl;
    }

    private JPanel taoTabDS() {
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnXoa = new JButton("Xóa hóa đơn");
        JButton btnLamMoi = new JButton("Làm mới");
        JButton btnDong = new JButton("Đóng");
        JPanel pnlBtn = new JPanel(new FlowLayout());
        pnlBtn.add(new JLabel("Mã HD/Ngày (yyyy-MM-dd):"));
        pnlBtn.add(txtTim);
        pnlBtn.add(btnTim);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);
        pnlBtn.add(btnDong);

        JScrollPane spHD = new JScrollPane(tblHD);
        spHD.setBorder(BorderFactory.createTitledBorder("Hóa đơn"));
        JScrollPane spCT = new JScrollPane(tblCT);
        spCT.setBorder(BorderFactory.createTitledBorder("Chi tiết hóa đơn"));
        JPanel pnlBang = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlBang.add(spHD);
        pnlBang.add(spCT);

        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.add(pnlBtn, BorderLayout.NORTH);
        pnl.add(pnlBang, BorderLayout.CENTER);

        tblHD.getSelectionModel().addListSelectionListener(e -> {
            int r = tblHD.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                taiChiTiet(hd.getValueAt(r, 0).toString());
            }
        });

        btnTim.addActionListener(e -> taiHD(txtTim.getText().trim()));
        btnXoa.addActionListener(e -> xoaHD());
        btnLamMoi.addActionListener(e -> {
            txtTim.setText("");
            taiHD("");
        });
        btnDong.addActionListener(e -> dispose());
        return pnl;
    }

    private void tb(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void napBan() {
        cboBan.removeAllItems();
        dsMaBan.clear();
        try (Connection c = KetNoi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT MaBan, TenBan FROM Ban")) {
            while (rs.next()) {
                dsMaBan.add(rs.getString(1));
                cboBan.addItem(rs.getString(2));
            }
        } catch (SQLException ex) {
            tb("Lỗi tải danh sách bàn: " + ex.getMessage());
        }
    }

    private void napMon() {
        cboMon.removeAllItems();
        dsMaMon.clear();
        dsGia.clear();
        try (Connection c = KetNoi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT MaMon, TenMon, CAST(DonGia AS BIGINT) FROM Mon")) {
            while (rs.next()) {
                dsMaMon.add(rs.getString(1));
                cboMon.addItem(rs.getString(2));
                dsGia.add(rs.getLong(3));
            }
        } catch (SQLException ex) {
            tb("Lỗi tải danh sách món: " + ex.getMessage());
        }
    }

    private void maMoi() {
        try {
            Object o = KetNoi.layMot("SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(MaHD, 3, 10) AS INT)), 0) + 1 FROM HoaDon");
            txtMaHD.setText(String.format("HD%02d", ((Number) o).intValue()));
        } catch (SQLException ex) {
            tb("Lỗi tạo mã hóa đơn: " + ex.getMessage());
        }
    }

    private long tong() {
        long t = 0;
        for (int r = 0; r < gio.getRowCount(); r++) {
            t += (Long) gio.getValueAt(r, 4);
        }
        lblTong.setText("Tổng tiền: " + String.format("%,d", t) + " VNĐ");
        return t;
    }

    private void themMon() {
        int i = cboMon.getSelectedIndex();
        if (i < 0) {
            tb("Chưa có món để chọn!");
            return;
        }
        int sl;
        try {
            sl = Integer.parseInt(txtSL.getText().trim());
        } catch (NumberFormatException ex) {
            tb("Số lượng phải là số nguyên!");
            return;
        }
        if (sl <= 0) {
            tb("Số lượng phải lớn hơn 0!");
            return;
        }
        String ma = dsMaMon.get(i);
        long gia = dsGia.get(i);
        for (int r = 0; r < gio.getRowCount(); r++) {
            if (gio.getValueAt(r, 0).equals(ma)) {
                int moi = (Integer) gio.getValueAt(r, 2) + sl;
                gio.setValueAt(moi, r, 2);
                gio.setValueAt(moi * gia, r, 4);
                tong();
                return;
            }
        }
        gio.addRow(new Object[]{ma, cboMon.getSelectedItem(), sl, gia, sl * gia});
        tong();
    }

    private void boMon() {
        int r = tblGio.getSelectedRow();
        if (r < 0) {
            tb("Hãy chọn món cần bỏ!");
            return;
        }
        gio.removeRow(r);
        tong();
    }

    private void luu() {
        if (gio.getRowCount() == 0) {
            tb("Chưa có món nào trong hóa đơn!");
            return;
        }
        if (cboBan.getSelectedIndex() < 0) {
            tb("Hãy chọn bàn!");
            return;
        }
        Date ngay;
        try {
            ngay = Date.valueOf(txtNgay.getText().trim());
        } catch (IllegalArgumentException ex) {
            tb("Ngày lập phải có dạng yyyy-MM-dd!");
            return;
        }
        String maHD = txtMaHD.getText();
        try (Connection c = KetNoi.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement(
                    "INSERT INTO HoaDon (MaHD, NgayLap, MaNV, MaBan, TongTien) VALUES (?,?,?,?,?)");
                 PreparedStatement p2 = c.prepareStatement(
                         "INSERT INTO ChiTietHoaDon (MaHD, MaMon, SoLuong, DonGia) VALUES (?,?,?,?)")) {
                p1.setString(1, maHD);
                p1.setDate(2, ngay);
                p1.setString(3, DangNhap.maNV);
                p1.setString(4, dsMaBan.get(cboBan.getSelectedIndex()));
                p1.setLong(5, tong());
                p1.executeUpdate();
                for (int r = 0; r < gio.getRowCount(); r++) {
                    p2.setString(1, maHD);
                    p2.setString(2, (String) gio.getValueAt(r, 0));
                    p2.setInt(3, (Integer) gio.getValueAt(r, 2));
                    p2.setLong(4, (Long) gio.getValueAt(r, 3));
                    p2.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            tb("Lưu hóa đơn thất bại: " + ex.getMessage());
            return;
        }
        tb("Lưu hóa đơn thành công!");
        lamMoiHD();
    }

    private void lamMoiHD() {
        gio.setRowCount(0);
        txtSL.setText("1");
        txtNgay.setText(LocalDate.now().toString());
        tong();
        maMoi();
        taiHD("");
    }

    private void taiHD(String tk) {
        String sql = "SELECT h.MaHD, h.NgayLap, n.HoTen, b.TenBan, CAST(h.TongTien AS BIGINT) "
                + "FROM HoaDon h JOIN NhanVien n ON h.MaNV = n.MaNV JOIN Ban b ON h.MaBan = b.MaBan";
        try {
            if (tk.isEmpty()) {
                KetNoi.nap(hd, sql + " ORDER BY h.MaHD");
            } else {
                KetNoi.nap(hd, sql + " WHERE h.MaHD LIKE ? OR CONVERT(VARCHAR(10), h.NgayLap, 23) LIKE ? ORDER BY h.MaHD",
                        "%" + tk + "%", "%" + tk + "%");
            }
            ct.setRowCount(0);
        } catch (SQLException ex) {
            tb("Lỗi tải hóa đơn: " + ex.getMessage());
        }
    }

    private void taiChiTiet(String maHD) {
        try {
            KetNoi.nap(ct, "SELECT m.TenMon, c.SoLuong, CAST(c.DonGia AS BIGINT), CAST(c.ThanhTien AS BIGINT) "
                    + "FROM ChiTietHoaDon c JOIN Mon m ON c.MaMon = m.MaMon WHERE c.MaHD = ?", maHD);
        } catch (SQLException ex) {
            tb("Lỗi tải chi tiết: " + ex.getMessage());
        }
    }

    private void xoaHD() {
        int r = tblHD.getSelectedRow();
        if (r < 0) {
            tb("Hãy chọn hóa đơn cần xóa!");
            return;
        }
        String ma = hd.getValueAt(r, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xóa hóa đơn " + ma + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            KetNoi.chay("DELETE FROM HoaDon WHERE MaHD = ?", ma);
            tb("Xóa thành công!");
            maMoi();
            taiHD("");
        } catch (SQLException ex) {
            tb("Xóa thất bại: " + ex.getMessage());
        }
    }
}
