package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.model.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentView extends BasePage {

    private User currentUser;

    // Tablo Modelleri
    private DefaultTableModel modelPermissions;
    private DefaultTableModel modelRoommates;

    private JTextField txtBaslangic, txtBitis, txtSebep;
    private JLabel lblOdaBilgisi;

    public StudentView(User user) {
        super("Öğrenci Paneli - " + user.getAdSoyad(), 1200, 700);
        this.currentUser = user;
        initializeComponents();
        loadPermissions();
        loadRoomInfoAndMates();
        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        // --- 1. ÜST PANEL (HEADER) ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(new Color(40, 44, 52)); // Modern Dark
        pnlTop.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblHosgeldin = new JLabel("Hoşgeldin, " + currentUser.getAdSoyad());
        lblHosgeldin.setForeground(Color.WHITE);
        lblHosgeldin.putClientProperty("FlatLaf.style", "font: bold +4");
        pnlTop.add(lblHosgeldin, BorderLayout.WEST);

        JPanel pnlInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        pnlInfo.setOpaque(false);

        lblOdaBilgisi = new JLabel("Oda: Yükleniyor...");
        lblOdaBilgisi.setForeground(new Color(255, 200, 0)); // Gold
        lblOdaBilgisi.putClientProperty("FlatLaf.style", "font: bold");
        pnlInfo.add(lblOdaBilgisi);

        JButton btnProfil = new JButton("Bilgilerimi Güncelle");
        pnlInfo.add(btnProfil);
        btnProfil.addActionListener(e -> showUpdateProfileDialog());

        pnlTop.add(pnlInfo, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. ANA İÇERİK (3 SÜTUN) ---
        JPanel pnlCenter = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCenter.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(pnlCenter, BorderLayout.CENTER);

        // --- SOL: İZİN TALEP FORMU ---
        JPanel pnlLeft = createCardPanel("Yeni İzin Talebi");

        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        pnlForm.add(new JLabel("Başlangıç (GG.AA.YYYY):"), gbc);
        gbc.gridy++;
        txtBaslangic = new JTextField();
        txtBaslangic.putClientProperty("JTextField.placeholderText", "Örn: 15.05.2025");
        pnlForm.add(txtBaslangic, gbc);

        gbc.gridy++;
        pnlForm.add(new JLabel("Bitiş (GG.AA.YYYY):"), gbc);
        gbc.gridy++;
        txtBitis = new JTextField();
        txtBitis.putClientProperty("JTextField.placeholderText", "Örn: 20.05.2025");
        pnlForm.add(txtBitis, gbc);

        gbc.gridy++;
        pnlForm.add(new JLabel("Sebep:"), gbc);
        gbc.gridy++;
        txtSebep = new JTextField();
        pnlForm.add(txtSebep, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5);
        JButton btnGonder = new JButton("Talep Gönder");
        btnGonder.putClientProperty("FlatLaf.style", "font: bold");
        btnGonder.setBackground(new Color(40, 167, 69)); // Success Green
        btnGonder.setForeground(Color.WHITE);
        pnlForm.add(btnGonder, gbc);

        // Formu yukarı sabitlemek için kuzey'e ekle
        JPanel pnlFormWrapper = new JPanel(new BorderLayout());
        pnlFormWrapper.add(pnlForm, BorderLayout.NORTH);
        pnlLeft.add(pnlFormWrapper, BorderLayout.CENTER);

        btnGonder.addActionListener(e -> sendPermissionRequest());
        pnlCenter.add(pnlLeft);

        // --- ORTA: ODA ARKADAŞLARI ---
        JPanel pnlMiddle = createCardPanel("Oda Arkadaşlarım");
        String[] colMates = { "Ad", "Soyad", "İletişim" };
        modelRoommates = new DefaultTableModel(colMates, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tblRoommates = new JTable(modelRoommates);
        tblRoommates.setShowVerticalLines(false);
        tblRoommates.setRowHeight(25);
        pnlMiddle.add(new JScrollPane(tblRoommates), BorderLayout.CENTER);
        pnlCenter.add(pnlMiddle);

        // --- SAĞ: İZİN GEÇMİŞİ ---
        JPanel pnlRight = createCardPanel("İzin Geçmişim");
        String[] colPerms = { "Tarih Aralığı", "Sebep", "Durum" };
        modelPermissions = new DefaultTableModel(colPerms, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tblPermissions = new JTable(modelPermissions);
        tblPermissions.setShowVerticalLines(false);
        tblPermissions.setRowHeight(25);
        tblPermissions.getColumnModel().getColumn(0).setPreferredWidth(120);

        pnlRight.add(new JScrollPane(tblPermissions), BorderLayout.CENTER);
        pnlCenter.add(pnlRight);
    }

    // Yardımcı method: Kart görünümlü panel
    private JPanel createCardPanel(String title) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(50, 50, 50)));
        pnl.setBackground(Color.WHITE);
        return pnl;
    }

    // --- PROFİL GÜNCELLEME PENCERESİ ---
    private void showUpdateProfileDialog() {
        JDialog dialog = new JDialog(this, "Profil Bilgilerini Güncelle", true);
        dialog.setSize(400, 450);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Mevcut bilgileri çek
        String currentTel = "", currentAdres = "", currentEmail = currentUser.getEmail();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT sd.telefon, sd.adres FROM student_details sd WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUser.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentTel = rs.getString("telefon");
                currentAdres = rs.getString("adres");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        pnlForm.add(new JLabel("Email:"), gbc);
        gbc.gridy++;
        JTextField txtEmail = new JTextField(currentEmail);
        pnlForm.add(txtEmail, gbc);

        gbc.gridy++;
        pnlForm.add(new JLabel("Telefon:"), gbc);
        gbc.gridy++;
        JTextField txtTel = new JTextField(currentTel);
        pnlForm.add(txtTel, gbc);

        gbc.gridy++;
        pnlForm.add(new JLabel("Adres:"), gbc);
        gbc.gridy++;
        JTextArea txtAdres = new JTextArea(currentAdres);
        txtAdres.setRows(3);
        txtAdres.setLineWrap(true);
        pnlForm.add(new JScrollPane(txtAdres), gbc);

        gbc.gridy++;
        pnlForm.add(new JLabel("Yeni Şifre:"), gbc);
        gbc.gridy++;
        JPasswordField txtPass = new JPasswordField(currentUser.getSifre());
        pnlForm.add(txtPass, gbc);

        dialog.add(pnlForm, BorderLayout.CENTER);

        JButton btnSave = new JButton("Kaydet");
        btnSave.putClientProperty("FlatLaf.style", "font: bold; background: #007bff; foreground: #ffffff");
        btnSave.addActionListener(e -> {
            updateProfileInDB(txtEmail.getText(), txtTel.getText(), txtAdres.getText(),
                    new String(txtPass.getPassword()));
            dialog.dispose();
        });

        JPanel pnlBtn = new JPanel();
        pnlBtn.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlBtn.add(btnSave);
        dialog.add(pnlBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void updateProfileInDB(String mail, String tel, String adr, String pass) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            String sqlUser = "UPDATE users SET email = ?, sifre = ? WHERE id = ?";
            PreparedStatement psUser = conn.prepareStatement(sqlUser);
            psUser.setString(1, mail);
            psUser.setString(2, pass);
            psUser.setInt(3, currentUser.getId());
            psUser.executeUpdate();

            ResultSet rsCheck = conn.createStatement()
                    .executeQuery("SELECT * FROM student_details WHERE user_id=" + currentUser.getId());
            if (rsCheck.next()) {
                String sqlDet = "UPDATE student_details SET telefon = ?, adres = ? WHERE user_id = ?";
                PreparedStatement psDet = conn.prepareStatement(sqlDet);
                psDet.setString(1, tel);
                psDet.setString(2, adr);
                psDet.setInt(3, currentUser.getId());
                psDet.executeUpdate();
            } else {
                String sqlIns = "INSERT INTO student_details (user_id, telefon, adres) VALUES (?, ?, ?)";
                PreparedStatement psIns = conn.prepareStatement(sqlIns);
                psIns.setInt(1, currentUser.getId());
                psIns.setString(2, tel);
                psIns.setString(3, adr);
                psIns.executeUpdate();
            }

            currentUser.setEmail(mail);
            currentUser.setSifre(pass);
            JOptionPane.showMessageDialog(this, "Bilgileriniz başarıyla güncellendi!");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    // --- İZİN TALEBİ (TARİH KONTROLLÜ) ---
    private void sendPermissionRequest() {
        String baslangic = txtBaslangic.getText();
        String bitis = txtBitis.getText();
        String sebep = txtSebep.getText();

        if (baslangic.isEmpty() || bitis.isEmpty() || sebep.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!");
            return;
        }

        // DateUtils sınıfını kullanıyoruz
        if (!DateUtils.isValidFormat(baslangic) || !DateUtils.isValidFormat(bitis)) {
            JOptionPane.showMessageDialog(this, "Tarih formatı GG.AA.YYYY olmalı! (Örn: 15.05.2025)", "Format Hatası",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DateUtils.isReasonableDate(baslangic) || !DateUtils.isReasonableDate(bitis)) {
            JOptionPane.showMessageDialog(this, "Lütfen 2024-2026 arası bir yıl giriniz.", "Tarih Hatası",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DateUtils.isFutureOrPresent(baslangic)) {
            JOptionPane.showMessageDialog(this, "Geçmiş bir tarihe izin alamazsınız!", "Tarih Hatası",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DateUtils.isStartBeforeEnd(baslangic, bitis)) {
            JOptionPane.showMessageDialog(this, "Bitiş tarihi, başlangıçtan önce olamaz!", "Mantık Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "INSERT INTO permissions (ogrenci_id, baslangic, bitis, sebep, durum) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUser.getId());
            ps.setString(2, baslangic);
            ps.setString(3, bitis);
            ps.setString(4, sebep);
            ps.setString(5, "BEKLEMEDE");
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "İzin talebiniz gönderildi!");
            txtBaslangic.setText("");
            txtBitis.setText("");
            txtSebep.setText("");
            loadPermissions();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadRoomInfoAndMates() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sqlOda = "SELECT r.id, r.oda_no FROM rooms r JOIN student_details sd ON r.id = sd.oda_id WHERE sd.user_id = ?";
            PreparedStatement psOda = conn.prepareStatement(sqlOda);
            psOda.setInt(1, currentUser.getId());
            ResultSet rsOda = psOda.executeQuery();

            if (rsOda.next()) {
                int odaId = rsOda.getInt("id");
                String odaNo = rsOda.getString("oda_no");
                lblOdaBilgisi.setText("Kaldığınız Oda: " + odaNo);

                if (currentUser instanceof com.yurt.patterns.observer.Observer) {
                    ((com.yurt.patterns.observer.Observer) currentUser).update("Oda atamanız yapıldı: " + odaNo);
                }

                String sqlMates = "SELECT u.ad, u.soyad, sd.telefon FROM users u JOIN student_details sd ON u.id = sd.user_id WHERE sd.oda_id = ? AND u.id != ?";
                PreparedStatement psMates = conn.prepareStatement(sqlMates);
                psMates.setInt(1, odaId);
                psMates.setInt(2, currentUser.getId());
                ResultSet rsMates = psMates.executeQuery();

                modelRoommates.setRowCount(0);
                while (rsMates.next()) {
                    String tel = rsMates.getString("telefon");
                    modelRoommates.addRow(new Object[] { rsMates.getString("ad"), rsMates.getString("soyad"),
                            (tel != null && !tel.isEmpty()) ? tel : "Girmedi" });
                }
            } else {
                lblOdaBilgisi.setText("Kaldığınız Oda: Henüz Atanmadı");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPermissions() {
        modelPermissions.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM permissions WHERE ogrenci_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUser.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelPermissions.addRow(new Object[] { rs.getString("baslangic") + " - " + rs.getString("bitis"),
                        rs.getString("sebep"), rs.getString("durum") });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}