package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.patterns.builder.StudentBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PersonnelView extends BasePage {

    private User currentUser;

    // Tablolar
    private JTable tblRequests, tblRooms, tblStudents;
    private DefaultTableModel modelRequests, modelRooms, modelStudents;

    // Form Elemanları
    private JTextField txtOdaBaslangic, txtOdaBitis, txtKapasite;
    private JTextField txtOgrenciTc, txtAtanacakOda;

    // Öğrenci Ekleme Formu
    private JTextField txtYeniAd, txtYeniSoyad, txtYeniTc, txtYeniSifre, txtYeniEmail, txtYeniTel, txtYeniKadi;

    // Arama ve Raporlama
    private JTextField txtOgrenciAra;
    private JTextField txtRaporBaslangic;

    public PersonnelView(User user) {
        super("Yurt Yönetim Paneli - " + user.getAdSoyad(), 1280, 800);
        this.currentUser = user;
        initializeComponents();

        loadPermissionRequests("BEKLEMEDE");
        loadRooms();
        loadStudents("");

        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("FlatLaf.style", "tabType: card; tabHeight: 40");

        // ==========================================
        // SEKME 1: İZİN YÖNETİMİ
        // ==========================================
        JPanel pnlPermissions = new JPanel(new BorderLayout(10, 10));
        pnlPermissions.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- ÜST PANEL (FİLTRELER) ---
        JPanel pnlPermTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlPermTop.add(new JLabel("Filtrele:"));

        JButton btnBekleyenler = new JButton("Onay Bekleyenler");
        btnBekleyenler.setBackground(new Color(255, 165, 0));
        btnBekleyenler.setForeground(Color.WHITE);
        pnlPermTop.add(btnBekleyenler);

        JButton btnGecmis = new JButton("Geçmiş İşlemler");
        btnGecmis.setBackground(new Color(100, 149, 237));
        btnGecmis.setForeground(Color.WHITE);
        pnlPermTop.add(btnGecmis);

        pnlPermTop.add(Box.createHorizontalStrut(20)); // Boşluk
        pnlPermTop.add(new JLabel("Yıla Göre Ara (Örn: 2025):"));
        txtRaporBaslangic = new JTextField(10);
        pnlPermTop.add(txtRaporBaslangic);
        JButton btnAraTarih = new JButton("Ara");
        pnlPermTop.add(btnAraTarih);

        pnlPermissions.add(pnlPermTop, BorderLayout.NORTH);

        // --- ORTA PANEL (TABLO) ---
        String[] colPerms = { "ID", "Öğrenci Adı", "TC No", "Başlangıç", "Bitiş", "Sebep", "Durum" };
        modelRequests = new DefaultTableModel(colPerms, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRequests = new JTable(modelRequests);
        tblRequests.setRowHeight(25);
        tblRequests.setShowVerticalLines(false);
        pnlPermissions.add(new JScrollPane(tblRequests), BorderLayout.CENTER);

        // --- ALT PANEL (BUTONLAR) ---
        JPanel pnlPermBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton btnOnayla = new JButton("SEÇİLENİ ONAYLA");
        btnOnayla.setBackground(new Color(60, 179, 113));
        btnOnayla.setForeground(Color.WHITE);
        pnlPermBottom.add(btnOnayla);

        JButton btnReddet = new JButton("SEÇİLENİ REDDET");
        btnReddet.setBackground(new Color(220, 20, 60));
        btnReddet.setForeground(Color.WHITE);
        pnlPermBottom.add(btnReddet);

        JButton btnIzinSil = new JButton("KAYDI SİL");
        btnIzinSil.setBackground(Color.DARK_GRAY);
        btnIzinSil.setForeground(Color.WHITE);
        pnlPermBottom.add(btnIzinSil);

        pnlPermissions.add(pnlPermBottom, BorderLayout.SOUTH);

        // Listenerlar
        btnBekleyenler.addActionListener(e -> loadPermissionRequests("BEKLEMEDE"));
        btnGecmis.addActionListener(e -> loadPermissionHistory());
        btnAraTarih.addActionListener(e -> searchPermissionsByDate(txtRaporBaslangic.getText()));
        btnOnayla.addActionListener(e -> updatePermissionStatus("ONAYLANDI"));
        btnReddet.addActionListener(e -> updatePermissionStatus("REDDEDILDI"));
        btnIzinSil.addActionListener(e -> deletePermission());

        tabbedPane.addTab("İzin Yönetimi", pnlPermissions);

        // ==========================================
        // SEKME 2: ODA VE KAYIT (SPLIT PANE)
        // ==========================================
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane2.setDividerLocation(600);
        splitPane2.setResizeWeight(0.5);

        // --- SOL: ODALAR LİSTESİ ---
        JPanel pnlRoomsLeft = new JPanel(new BorderLayout(5, 5));
        pnlRoomsLeft.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblRoomsHeader = new JLabel("MEVCUT ODALAR");
        lblRoomsHeader.putClientProperty("FlatLaf.style", "font: bold +2");
        pnlRoomsLeft.add(lblRoomsHeader, BorderLayout.NORTH);

        String[] colRooms = { "ID", "Oda No", "Kapasite", "Mevcut", "Durum" };
        modelRooms = new DefaultTableModel(colRooms, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRooms = new JTable(modelRooms);
        tblRooms.setRowHeight(25);
        pnlRoomsLeft.add(new JScrollPane(tblRooms), BorderLayout.CENTER);

        JButton btnOdaSil = new JButton("Seçili Odayı Sil");
        btnOdaSil.setBackground(Color.RED);
        btnOdaSil.setForeground(Color.WHITE);
        pnlRoomsLeft.add(btnOdaSil, BorderLayout.SOUTH);

        splitPane2.setLeftComponent(pnlRoomsLeft);

        // --- SAĞ: FORMLAR (SCROLLABLE) ---
        JPanel pnlFormsWrapper = new JPanel();
        pnlFormsWrapper.setLayout(new BoxLayout(pnlFormsWrapper, BoxLayout.Y_AXIS));
        pnlFormsWrapper.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Form 1: Toplu Oda
        JPanel pnlAddRoom = createCardPanel("Toplu Oda Ekleme");
        JPanel pnlRoomForm = new JPanel(new GridLayout(2, 4, 5, 5));

        pnlRoomForm.add(new JLabel("Başlangıç No:"));
        txtOdaBaslangic = new JTextField();
        pnlRoomForm.add(txtOdaBaslangic);

        pnlRoomForm.add(new JLabel("Bitiş No:"));
        txtOdaBitis = new JTextField();
        pnlRoomForm.add(txtOdaBitis);

        pnlRoomForm.add(new JLabel("Kapasite:"));
        txtKapasite = new JTextField();
        pnlRoomForm.add(txtKapasite);

        JButton btnOdaEkle = new JButton("Oda Ekle");
        pnlRoomForm.add(new JLabel(""));
        pnlRoomForm.add(btnOdaEkle);

        pnlAddRoom.add(pnlRoomForm);
        pnlFormsWrapper.add(pnlAddRoom);
        pnlFormsWrapper.add(Box.createVerticalStrut(20));

        // Form 2: Öğrenci Kayıt
        JPanel pnlAddStudent = createCardPanel("Yeni Öğrenci Kayıt");
        JPanel pnlStudentForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlStudentForm.add(new JLabel("Ad:"), gbc);
        gbc.gridx = 1;
        txtYeniAd = new JTextField();
        pnlStudentForm.add(txtYeniAd, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlStudentForm.add(new JLabel("Soyad:"), gbc);
        gbc.gridx = 1;
        txtYeniSoyad = new JTextField();
        pnlStudentForm.add(txtYeniSoyad, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        pnlStudentForm.add(new JLabel("TC Kimlik:"), gbc);
        gbc.gridx = 1;
        txtYeniTc = new JTextField();
        pnlStudentForm.add(txtYeniTc, gbc);
        setupTcField(txtYeniTc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        pnlStudentForm.add(new JLabel("Şifre:"), gbc);
        gbc.gridx = 1;
        txtYeniSifre = new JTextField();
        pnlStudentForm.add(txtYeniSifre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        pnlStudentForm.add(new JLabel("Kullanıcı Adı:"), gbc);
        gbc.gridx = 1;
        txtYeniKadi = new JTextField();
        pnlStudentForm.add(txtYeniKadi, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        pnlStudentForm.add(new JLabel("E-Posta:"), gbc);
        gbc.gridx = 1;
        txtYeniEmail = new JTextField();
        pnlStudentForm.add(txtYeniEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        pnlStudentForm.add(new JLabel("Telefon:"), gbc);
        gbc.gridx = 1;
        txtYeniTel = new JTextField();
        pnlStudentForm.add(txtYeniTel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton btnOgrKaydet = new JButton("Öğrenciyi Sisteme Kaydet");
        btnOgrKaydet.setBackground(new Color(255, 140, 0));
        btnOgrKaydet.setForeground(Color.WHITE);
        pnlStudentForm.add(btnOgrKaydet, gbc);

        pnlAddStudent.add(pnlStudentForm);
        pnlFormsWrapper.add(pnlAddStudent);
        pnlFormsWrapper.add(Box.createVerticalStrut(20));

        // Form 3: Oda Atama
        JPanel pnlAssign = createCardPanel("Öğrenciyi Odaya Yerleştir");
        JPanel pnlAssignForm = new JPanel(new GridLayout(3, 2, 5, 5));

        pnlAssignForm.add(new JLabel("Öğrenci TC:"));
        txtOgrenciTc = new JTextField();
        pnlAssignForm.add(txtOgrenciTc);

        pnlAssignForm.add(new JLabel("Oda No:"));
        txtAtanacakOda = new JTextField();
        pnlAssignForm.add(txtAtanacakOda);

        JButton btnAta = new JButton("Yerleştir / Transfer Et");
        pnlAssignForm.add(new JLabel(""));
        pnlAssignForm.add(btnAta);

        pnlAssign.add(pnlAssignForm);
        pnlFormsWrapper.add(pnlAssign);

        JScrollPane rightScroll = new JScrollPane(pnlFormsWrapper);
        rightScroll.setBorder(null);
        splitPane2.setRightComponent(rightScroll);

        // Listenerlar
        btnOdaEkle.addActionListener(e -> addBulkRooms());
        btnOgrKaydet.addActionListener(e -> addNewStudent());
        btnAta.addActionListener(e -> assignStudentToRoom());
        btnOdaSil.addActionListener(e -> deleteRoom());

        tabbedPane.addTab("Oda ve Kayıt İşlemleri", splitPane2);

        // ==========================================
        // SEKME 3: ÖĞRENCİ LİSTESİ
        // ==========================================
        JPanel pnlStudentList = new JPanel(new BorderLayout(10, 10));
        pnlStudentList.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlListTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlListTop.add(new JLabel("Ara (Ad/Soyad/Oda No):"));
        txtOgrenciAra = new JTextField(20);
        pnlListTop.add(txtOgrenciAra);
        JButton btnAra = new JButton("Ara");
        pnlListTop.add(btnAra);
        JButton btnYenile = new JButton("Yenile");
        pnlListTop.add(btnYenile);

        JButton btnOgrSil = new JButton("SEÇİLİ ÖĞRENCİYİ SİL");
        btnOgrSil.setBackground(Color.RED);
        btnOgrSil.setForeground(Color.WHITE);

        JPanel pnlListTopWrapper = new JPanel(new BorderLayout());
        pnlListTopWrapper.add(pnlListTop, BorderLayout.WEST);
        pnlListTopWrapper.add(btnOgrSil, BorderLayout.EAST);

        pnlStudentList.add(pnlListTopWrapper, BorderLayout.NORTH);

        String[] colStudents = { "ID", "TC No", "Ad", "Soyad", "Kullanıcı Adı", "Kaldığı Oda" };
        modelStudents = new DefaultTableModel(colStudents, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblStudents = new JTable(modelStudents);
        tblStudents.setRowHeight(25);
        pnlStudentList.add(new JScrollPane(tblStudents), BorderLayout.CENTER);

        // Listenerlar
        btnAra.addActionListener(e -> loadStudents(txtOgrenciAra.getText()));
        btnYenile.addActionListener(e -> {
            txtOgrenciAra.setText("");
            loadStudents("");
        });
        btnOgrSil.addActionListener(e -> deleteStudent());

        tabbedPane.addTab("Öğrenci Listesi", pnlStudentList);

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- YARDIMCI GÖRÜNÜM METOTLARI ---
    private JPanel createCardPanel(String title) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), title,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        JPanel inner = new JPanel();
        inner.setBorder(new EmptyBorder(10, 10, 10, 10));
        inner.setLayout(new BorderLayout());
        pnl.add(inner, BorderLayout.CENTER);
        return inner; // İç panel döner, border dışta kalır
    }

    private void setupTcField(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (!field.getText().matches("\\d*"))
                    field.setText(field.getText().replaceAll("[^\\d]", ""));
                if (field.getText().length() != 11)
                    field.setBackground(new Color(255, 230, 230));
                else
                    field.setBackground(Color.WHITE);
            }

            public void keyTyped(KeyEvent e) {
                if (field.getText().length() >= 11)
                    e.consume();
            }
        });
    }

    // --- MANTIKSAL METOTLAR (ESKİ KODUN AYNISI) ---
    // ... Burada logic değişmedi, sadece kopyalıyorum ...

    private void addNewStudent() {
        String ad = txtYeniAd.getText();
        String soyad = txtYeniSoyad.getText();
        String tc = txtYeniTc.getText();
        String sifre = txtYeniSifre.getText();
        String email = txtYeniEmail.getText();
        String tel = txtYeniTel.getText();
        String kadi = txtYeniKadi.getText();

        if (ad.isEmpty() || soyad.isEmpty() || tc.isEmpty() || email.isEmpty() || tel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ad, Soyad, TC, Email ve Telefon zorunludur!", "Eksik Bilgi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tc.length() != 11) {
            JOptionPane.showMessageDialog(this, "TC 11 hane olmalı!", "Format Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            if (checkIfExists(conn, "student_details", "telefon", tel)) {
                JOptionPane.showMessageDialog(this, "Bu telefon numarası zaten kayıtlı!", "Mükerrer Kayıt",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (checkIfExists(conn, "users", "email", email)) {
                JOptionPane.showMessageDialog(this, "Bu E-posta adresi zaten kayıtlı!", "Mükerrer Kayıt",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String finalSifre = sifre.isEmpty() ? tc : sifre;
            String finalKadi = kadi.isEmpty() ? ad.toLowerCase() + tc.substring(0, 3) : kadi;

            if (checkIfExists(conn, "users", "kullanici_adi", finalKadi)) {
                JOptionPane.showMessageDialog(this, "Bu kullanıcı adı alınmış (" + finalKadi + ")! Lütfen değiştirin.",
                        "Mükerrer Kayıt", JOptionPane.ERROR_MESSAGE);
                return;
            }

            StudentBuilder builder = new StudentBuilder();
            User yeniOgrenci = builder.setAd(ad).setSoyad(soyad).setTcNo(tc).setSifre(finalSifre)
                    .setEmail(email).setTelefon(tel).setKullaniciAdi(finalKadi).build();

            String sqlUser = "INSERT INTO users (tc_no, ad, soyad, email, sifre, rol, kullanici_adi) VALUES (?, ?, ?, ?, ?, 'OGRENCI', ?)";
            PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            psUser.setString(1, yeniOgrenci.getTcNo());
            psUser.setString(2, yeniOgrenci.getAd());
            psUser.setString(3, yeniOgrenci.getSoyad());
            psUser.setString(4, yeniOgrenci.getEmail());
            psUser.setString(5, yeniOgrenci.getSifre());
            psUser.setString(6, builder.getKullaniciAdi());
            psUser.executeUpdate();

            int userId = -1;
            ResultSet rsKeys = psUser.getGeneratedKeys();
            if (rsKeys.next())
                userId = rsKeys.getInt(1);
            else {
                ResultSet rsId = conn.createStatement().executeQuery("SELECT last_insert_rowid()");
                if (rsId.next())
                    userId = rsId.getInt(1);
            }

            if (userId != -1) {
                PreparedStatement psDetail = conn
                        .prepareStatement("INSERT INTO student_details (user_id, adres, telefon) VALUES (?, ?, ?)");
                psDetail.setInt(1, userId);
                psDetail.setString(2, "Girilmedi");
                psDetail.setString(3, tel);
                psDetail.executeUpdate();
                JOptionPane.showMessageDialog(this, "Kayıt Başarılı! K.Adı: " + finalKadi);

                txtYeniAd.setText("");
                txtYeniSoyad.setText("");
                txtYeniTc.setText("");
                txtYeniSifre.setText("");
                txtYeniEmail.setText("");
                txtYeniTel.setText("");
                txtYeniKadi.setText("");
                loadStudents("");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE"))
                JOptionPane.showMessageDialog(this, "TC zaten kayıtlı!");
            else
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private boolean checkIfExists(Connection conn, String table, String column, String value) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM " + table + " WHERE " + column + " = ?");
        ps.setString(1, value);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private void loadRooms() {
        modelRooms.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                int kapasite = rs.getInt("kapasite");
                int mevcut = rs.getInt("mevcut_kisi");
                String durum = (mevcut >= kapasite) ? "DOLU" : "MUSAIT";
                modelRooms.addRow(new Object[] { rs.getInt("id"), rs.getString("oda_no"), kapasite, mevcut, durum });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPermissionRequests(String durumFiltresi) {
        modelRequests.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT p.id, u.ad, u.soyad, u.tc_no, p.baslangic, p.bitis, p.sebep, p.durum FROM permissions p JOIN users u ON p.ogrenci_id = u.id WHERE p.durum = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, durumFiltresi);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelRequests.addRow(new Object[] { rs.getInt("id"), rs.getString("ad") + " " + rs.getString("soyad"),
                        rs.getString("tc_no"), rs.getString("baslangic"), rs.getString("bitis"), rs.getString("sebep"),
                        rs.getString("durum") });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadPermissionHistory() {
        modelRequests.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT p.id, u.ad, u.soyad, u.tc_no, p.baslangic, p.bitis, p.sebep, p.durum FROM permissions p JOIN users u ON p.ogrenci_id = u.id WHERE p.durum != 'BEKLEMEDE'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                modelRequests.addRow(new Object[] { rs.getInt("id"), rs.getString("ad") + " " + rs.getString("soyad"),
                        rs.getString("tc_no"), rs.getString("baslangic"), rs.getString("bitis"), rs.getString("sebep"),
                        rs.getString("durum") });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void searchPermissionsByDate(String dateText) {
        if (dateText.isEmpty()) {
            loadPermissionRequests("ONAYLANDI");
            return;
        }
        modelRequests.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT p.id, u.ad, u.soyad, u.tc_no, p.baslangic, p.bitis, p.sebep, p.durum FROM permissions p JOIN users u ON p.ogrenci_id = u.id WHERE (p.durum = 'ONAYLANDI' OR p.durum = 'REDDEDILDI') AND (p.baslangic LIKE ? OR p.bitis LIKE ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + dateText + "%");
            ps.setString(2, "%" + dateText + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelRequests.addRow(new Object[] { rs.getInt("id"), rs.getString("ad") + " " + rs.getString("soyad"),
                        rs.getString("tc_no"), rs.getString("baslangic"), rs.getString("bitis"), rs.getString("sebep"),
                        rs.getString("durum") });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updatePermissionStatus(String yeniDurum) {
        int selectedRow = tblRequests.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen listeden bir talep seçin!");
            return;
        }
        try {
            int id = (int) modelRequests.getValueAt(selectedRow, 0);
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE permissions SET durum = ? WHERE id = ?");
            ps.setString(1, yeniDurum);
            ps.setInt(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "İşlem Tamam: " + yeniDurum);
            String mevcutDurum = (String) modelRequests.getValueAt(selectedRow, 6);
            if (mevcutDurum.equals("BEKLEMEDE"))
                loadPermissionRequests("BEKLEMEDE");
            else
                loadPermissionHistory();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteStudent() {
        int row = tblStudents.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silinecek öğrenciyi seçin!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Öğrenciyi silmek istediğinize emin misiniz?", "Onay",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        int userId = (int) modelStudents.getValueAt(row, 0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rsOda = conn.createStatement()
                    .executeQuery("SELECT oda_id FROM student_details WHERE user_id=" + userId);
            if (rsOda.next()) {
                int odaId = rsOda.getInt(1);
                if (odaId > 0)
                    conn.createStatement().executeUpdate(
                            "UPDATE rooms SET mevcut_kisi = mevcut_kisi - 1, durum='MUSAIT' WHERE id=" + odaId);
            }
            conn.createStatement().executeUpdate("DELETE FROM permissions WHERE ogrenci_id=" + userId);
            conn.createStatement().executeUpdate("DELETE FROM student_details WHERE user_id=" + userId);
            conn.createStatement().executeUpdate("DELETE FROM users WHERE id=" + userId);
            JOptionPane.showMessageDialog(this, "Öğrenci silindi.");
            loadStudents("");
            loadRooms();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void deleteRoom() {
        int row = tblRooms.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silinecek odayı seçin!");
            return;
        }
        int mevcut = (int) modelRooms.getValueAt(row, 3);
        if (mevcut > 0) {
            JOptionPane.showMessageDialog(this, "İçinde öğrenci olan oda silinemez!", "Hata",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int odaId = (int) modelRooms.getValueAt(row, 0);
        try {
            DatabaseConnection.getInstance().getConnection().createStatement()
                    .executeUpdate("DELETE FROM rooms WHERE id=" + odaId);
            JOptionPane.showMessageDialog(this, "Oda silindi.");
            loadRooms();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void deletePermission() {
        int row = tblRequests.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silinecek kaydı seçin!");
            return;
        }
        int permId = (int) modelRequests.getValueAt(row, 0);
        try {
            DatabaseConnection.getInstance().getConnection().createStatement()
                    .executeUpdate("DELETE FROM permissions WHERE id=" + permId);
            JOptionPane.showMessageDialog(this, "Kayıt silindi.");
            loadPermissionRequests("BEKLEMEDE");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void loadStudents(String searchText) {
        modelStudents.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT u.id, u.tc_no, u.ad, u.soyad, u.kullanici_adi, r.oda_no FROM users u LEFT JOIN student_details sd ON u.id = sd.user_id LEFT JOIN rooms r ON sd.oda_id = r.id WHERE u.rol = 'OGRENCI'";
            if (!searchText.isEmpty())
                sql += " AND (lower(u.ad) LIKE ? OR lower(u.soyad) LIKE ? OR r.oda_no LIKE ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            if (!searchText.isEmpty()) {
                String p = "%" + searchText.toLowerCase() + "%";
                ps.setString(1, p);
                ps.setString(2, p);
                ps.setString(3, p);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String oda = rs.getString("oda_no");
                modelStudents.addRow(new Object[] { rs.getInt("id"), rs.getString("tc_no"), rs.getString("ad"),
                        rs.getString("soyad"), rs.getString("kullanici_adi"), (oda == null ? "-" : oda) });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addBulkRooms() {
        try {
            int start = Integer.parseInt(txtOdaBaslangic.getText());
            int end = Integer.parseInt(txtOdaBitis.getText());
            int kap = Integer.parseInt(txtKapasite.getText());
            if (start > end)
                return;
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO rooms (oda_no, kapasite, mevcut_kisi, durum) VALUES (?, ?, 0, 'MUSAIT')");
            for (int i = start; i <= end; i++) {
                try {
                    ps.setString(1, String.valueOf(i));
                    ps.setInt(2, kap);
                    ps.executeUpdate();
                } catch (SQLException ignored) {
                }
            }
            JOptionPane.showMessageDialog(this, "Odalar eklendi.");
            loadRooms();
        } catch (Exception e) {
        }
    }

    private void assignStudentToRoom() {
        String tc = txtOgrenciTc.getText();
        String odaNo = txtAtanacakOda.getText();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement psOda = conn
                    .prepareStatement("SELECT id, kapasite, mevcut_kisi FROM rooms WHERE oda_no = ?");
            psOda.setString(1, odaNo);
            ResultSet rsOda = psOda.executeQuery();
            if (!rsOda.next()) {
                JOptionPane.showMessageDialog(this, "Oda yok");
                return;
            }
            int odaId = rsOda.getInt("id");
            if (rsOda.getInt("mevcut_kisi") >= rsOda.getInt("kapasite")) {
                JOptionPane.showMessageDialog(this, "Dolu");
                return;
            }
            PreparedStatement psOgr = conn.prepareStatement("SELECT id FROM users WHERE tc_no = ?");
            psOgr.setString(1, tc);
            ResultSet rsOgr = psOgr.executeQuery();
            if (!rsOgr.next()) {
                JOptionPane.showMessageDialog(this, "Öğrenci yok");
                return;
            }
            int userId = rsOgr.getInt("id");

            ResultSet rsCheck = conn.createStatement()
                    .executeQuery("SELECT oda_id FROM student_details WHERE user_id=" + userId);
            int eskiOdaId = -1;
            boolean kayitVar = false;
            if (rsCheck.next()) {
                kayitVar = true;
                eskiOdaId = rsCheck.getInt(1);
            }
            if (eskiOdaId == odaId) {
                JOptionPane.showMessageDialog(this, "Zaten burada!");
                return;
            }

            if (eskiOdaId > 0)
                conn.createStatement().executeUpdate(
                        "UPDATE rooms SET mevcut_kisi = mevcut_kisi - 1, durum='MUSAIT' WHERE id=" + eskiOdaId);

            if (kayitVar)
                conn.createStatement()
                        .executeUpdate("UPDATE student_details SET oda_id=" + odaId + " WHERE user_id=" + userId);
            else
                conn.createStatement().executeUpdate(
                        "INSERT INTO student_details (user_id, oda_id) VALUES (" + userId + ", " + odaId + ")");

            conn.createStatement().executeUpdate("UPDATE rooms SET mevcut_kisi = mevcut_kisi + 1 WHERE id=" + odaId);

            PreparedStatement psCheckFull = conn
                    .prepareStatement("SELECT kapasite, mevcut_kisi FROM rooms WHERE id = ?");
            psCheckFull.setInt(1, odaId);
            ResultSet rsFull = psCheckFull.executeQuery();
            if (rsFull.next() && rsFull.getInt("mevcut_kisi") >= rsFull.getInt("kapasite")) {
                conn.createStatement().executeUpdate("UPDATE rooms SET durum='DOLU' WHERE id=" + odaId);
            }
            JOptionPane.showMessageDialog(this, "Atandı");
            loadRooms();
            loadStudents("");
        } catch (Exception e) {
        }
    }
}