package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.patterns.builder.StudentBuilder;
import com.yurt.utils.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JButton currentActiveBtn; // Aktif butonu tutmak için

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
        super("Admin Dashboard - " + user.getAdSoyad(), 1400, 900);
        this.currentUser = user;
        initializeComponents();

        loadPermissionRequests("BEKLEMEDE");
        loadRooms();
        loadStudents("");

        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        // --- ANA LAYOUT: SIDEBAR + CONTENT ---
        setLayout(new BorderLayout());

        // 1. SIDEBAR (SOL MENÜ)
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 37, 41)); // Dark Sidebar
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Sidebar Header
        JLabel lblBrand = new JLabel("YURT YÖNETİM", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblBrand);

        sidebar.add(Box.createVerticalStrut(10));
        JLabel lblRole = new JLabel("Admin Panel", SwingConstants.CENTER);
        lblRole.setForeground(Color.LIGHT_GRAY);
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblRole);
        sidebar.add(Box.createVerticalStrut(40));

        // Menü Butonları
        sidebar.add(createSidebarButton("📊  İzin İşlemleri", "cardPermissions"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createSidebarButton("🏠  Oda Yönetimi", "cardRooms"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createSidebarButton("👥  Öğrenci Listesi", "cardStudents"));

        sidebar.add(Box.createVerticalGlue()); // Alt kısma boşluk
        JButton btnLogout = createSidebarButton("🚪  Çıkış Yap", "logout");
        btnLogout.setBackground(new Color(220, 53, 69)); // Red Logout
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        // 2. ANA İÇERİK (CARD LAYOUT)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UIHelper.BG_LIGHT);
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- KART 1: İZİN YÖNETİMİ ---
        mainContentPanel.add(createPermissionsPanel(), "cardPermissions");

        // --- KART 2: ODA & KAYIT YÖNETİMİ ---
        mainContentPanel.add(createRoomsPanel(), "cardRooms");

        // --- KART 3: ÖĞRENCİ LİSTESİ ---
        mainContentPanel.add(createStudentsPanel(), "cardStudents");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(33, 37, 41)); // Transparent-like
        btn.setMaximumSize(new Dimension(230, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty("FlatLaf.style", "arc: 10; margin: 0, 20, 0, 0"); // Left margin icons

        btn.addActionListener(e -> {
            if (cardName.equals("logout")) {
                this.dispose();
                new LoginView();
            } else {
                cardLayout.show(mainContentPanel, cardName);
                if (currentActiveBtn != null)
                    currentActiveBtn.setBackground(new Color(33, 37, 41));
                btn.setBackground(UIHelper.PRIMARY_COLOR);
                currentActiveBtn = btn;
            }
        });

        // Hover Effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != currentActiveBtn && !cardName.equals("logout"))
                    btn.setBackground(new Color(60, 60, 60));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != currentActiveBtn && !cardName.equals("logout"))
                    btn.setBackground(new Color(33, 37, 41));
            }
        });
        return btn;
    }

    // --- PANEL OLUŞTURUCULAR ---

    private JPanel createPermissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);

        // Header
        JPanel pnlHeader = UIHelper.createCardPanel();
        pnlHeader.add(UIHelper.createHeaderLabel("İzin Talepleri Yönetimi"));

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setOpaque(false);

        JButton btnBekleyen = UIHelper.createModernButton("Bekleyenler", Color.ORANGE);
        JButton btnGecmis = UIHelper.createModernButton("Geçmiş", new Color(100, 149, 237));

        pnlActions.add(btnBekleyen);
        pnlActions.add(btnGecmis);
        pnlActions.add(new JLabel("  |  Tarih:"));
        txtRaporBaslangic = new JTextField(8);
        pnlActions.add(txtRaporBaslangic);
        JButton btnAra = new JButton("Ara");
        pnlActions.add(btnAra);

        pnlHeader.add(pnlActions, BorderLayout.EAST);
        panel.add(pnlHeader, BorderLayout.NORTH);

        // Table
        String[] colPerms = { "ID", "Öğrenci Adı", "TC No", "Başlangıç", "Bitiş", "Sebep", "Durum" };
        modelRequests = new DefaultTableModel(colPerms, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblRequests = new JTable(modelRequests);
        UIHelper.decorateTable(tblRequests);

        JScrollPane scroll = new JScrollPane(tblRequests);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel pnlFooter = UIHelper.createCardPanel();
        pnlFooter.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));

        JButton btnOnayla = UIHelper.createModernButton("✅ ONAYLA", new Color(46, 204, 113));
        JButton btnReddet = UIHelper.createModernButton("❌ REDDET", new Color(231, 76, 60));
        JButton btnSil = UIHelper.createModernButton("🗑 SİL", Color.GRAY);

        pnlFooter.add(btnOnayla);
        pnlFooter.add(btnReddet);
        pnlFooter.add(btnSil);
        panel.add(pnlFooter, BorderLayout.SOUTH);

        // Listeners
        btnBekleyen.addActionListener(e -> loadPermissionRequests("BEKLEMEDE"));
        btnGecmis.addActionListener(e -> loadPermissionHistory());
        btnAra.addActionListener(e -> searchPermissionsByDate(txtRaporBaslangic.getText()));
        btnOnayla.addActionListener(e -> updatePermissionStatus("ONAYLANDI"));
        btnReddet.addActionListener(e -> updatePermissionStatus("REDDEDILDI"));
        btnSil.addActionListener(e -> deletePermission());

        return panel;
    }

    private JPanel createRoomsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);

        // SOL: Oda Listesi
        JPanel pnlLeft = UIHelper.createCardPanel();
        pnlLeft.add(UIHelper.createHeaderLabel("Oda Durumları"), BorderLayout.NORTH);

        String[] colRooms = { "ID", "Oda No", "Kapasite", "Mevcut", "Durum" };
        modelRooms = new DefaultTableModel(colRooms, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblRooms = new JTable(modelRooms);
        UIHelper.decorateTable(tblRooms);
        pnlLeft.add(new JScrollPane(tblRooms), BorderLayout.CENTER);

        JButton btnSil = UIHelper.createModernButton("Seçili Odayı Sil", Color.RED);
        pnlLeft.add(btnSil, BorderLayout.SOUTH);
        panel.add(pnlLeft);

        // SAĞ: İşlem Kartları (ScrolPane içinde)
        JPanel pnlRight = new JPanel();
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setOpaque(false);

        // Form 1
        JPanel pnlAddRef = UIHelper.createCardPanel();
        pnlAddRef.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Basitlik için flow kullanalım burda
        pnlAddRef.add(new JLabel("Odalar: "), g);
        txtOdaBaslangic = new JTextField(3);
        pnlAddRef.add(txtOdaBaslangic, g);
        pnlAddRef.add(new JLabel(" - "), g);
        txtOdaBitis = new JTextField(3);
        pnlAddRef.add(txtOdaBitis, g);
        pnlAddRef.add(new JLabel(" Kap: "), g);
        txtKapasite = new JTextField(3);
        pnlAddRef.add(txtKapasite, g);
        JButton btnEkle = new JButton("Oluştur");
        btnEkle.addActionListener(e -> addBulkRooms());
        pnlAddRef.add(btnEkle, g);

        pnlRight.add(pnlAddRef);
        pnlRight.add(Box.createVerticalStrut(15));

        // Form 2: Kayıt
        JPanel pnlReg = UIHelper.createCardPanel();
        pnlReg.setLayout(new GridLayout(9, 2, 5, 10)); // Grid daha kolay
        pnlReg.add(new JLabel("Ad:"));
        txtYeniAd = new JTextField();
        pnlReg.add(txtYeniAd);
        pnlReg.add(new JLabel("Soyad:"));
        txtYeniSoyad = new JTextField();
        pnlReg.add(txtYeniSoyad);
        pnlReg.add(new JLabel("TC:"));
        txtYeniTc = new JTextField();
        setupTcField(txtYeniTc);
        pnlReg.add(txtYeniTc);
        pnlReg.add(new JLabel("Şifre:"));
        txtYeniSifre = new JTextField();
        pnlReg.add(txtYeniSifre);
        pnlReg.add(new JLabel("K.Adı:"));
        txtYeniKadi = new JTextField();
        pnlReg.add(txtYeniKadi);
        pnlReg.add(new JLabel("Email:"));
        txtYeniEmail = new JTextField();
        pnlReg.add(txtYeniEmail);
        pnlReg.add(new JLabel("Tel:"));
        txtYeniTel = new JTextField();
        pnlReg.add(txtYeniTel);

        JButton btnKaydet = UIHelper.createModernButton("Öğrenciyi Kaydet", UIHelper.ACCENT_COLOR);
        btnKaydet.addActionListener(e -> addNewStudent());
        pnlReg.add(new JLabel(""));
        pnlReg.add(btnKaydet);

        pnlRight.add(pnlReg);

        // Form 3: Atama
        pnlRight.add(Box.createVerticalStrut(15));
        JPanel pnlAssign = UIHelper.createCardPanel();
        pnlAssign.add(new JLabel("Hızlı Oda Atama"), BorderLayout.NORTH);
        JPanel innerAssign = new JPanel(new FlowLayout());
        innerAssign.setOpaque(false);
        txtOgrenciTc = new JTextField(11);
        txtOgrenciTc.putClientProperty("JTextField.placeholderText", "Öğrenci TC");
        txtAtanacakOda = new JTextField(5);
        txtAtanacakOda.putClientProperty("JTextField.placeholderText", "Oda No");
        JButton btnAta = new JButton("Ata");
        btnAta.addActionListener(e -> assignStudentToRoom());
        innerAssign.add(txtOgrenciTc);
        innerAssign.add(txtAtanacakOda);
        innerAssign.add(btnAta);
        pnlAssign.add(innerAssign, BorderLayout.CENTER);
        pnlRight.add(pnlAssign);

        panel.add(pnlRight);

        btnSil.addActionListener(e -> deleteRoom());

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);

        JPanel header = UIHelper.createCardPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        header.add(UIHelper.createHeaderLabel("Tüm Öğrenciler"));
        header.add(Box.createHorizontalStrut(30));
        txtOgrenciAra = new JTextField(20);
        header.add(txtOgrenciAra);
        JButton btnAra = new JButton("🔍 Ara");
        header.add(btnAra);
        JButton btnYenile = new JButton("🔄 Yenile");
        header.add(btnYenile);

        panel.add(header, BorderLayout.NORTH);

        String[] colStudents = { "ID", "TC No", "Ad", "Soyad", "Kullanıcı Adı", "Kaldığı Oda" };
        modelStudents = new DefaultTableModel(colStudents, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblStudents = new JTable(modelStudents);
        UIHelper.decorateTable(tblStudents);
        panel.add(new JScrollPane(tblStudents), BorderLayout.CENTER);

        JButton btnSil = UIHelper.createModernButton("Seçili Öğrenciyi Sil", Color.RED);
        btnSil.addActionListener(e -> deleteStudent());

        JPanel footer = UIHelper.createCardPanel();
        footer.add(btnSil);
        panel.add(footer, BorderLayout.SOUTH);

        btnAra.addActionListener(e -> loadStudents(txtOgrenciAra.getText()));
        btnYenile.addActionListener(e -> {
            txtOgrenciAra.setText("");
            loadStudents("");
        });

        return panel;
    }

    // --- LOGIC METHODS (UNCHANGED) ---
    // (Aynı iş mantığı korunuyor)

    // ... Bu kısımlar önceki kodla birebir aynı, sadece görünüme effect etmiyor ...
    // Hız kazanmak için logic methodlarını buraya taşıyorum:

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