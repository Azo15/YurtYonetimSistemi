package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.model.DateUtils;
import com.yurt.utils.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentView extends BasePage {

    private User currentUser;
    private DefaultTableModel modelPermissions, modelRoommates;
    private JTextField txtBaslangic, txtBitis, txtSebep;
    private JLabel lblOdaBilgisi, lblWelcome;

    public StudentView(User user) {
        super("Öğrenci Paneli - " + user.getAdSoyad(), 1300, 800);
        this.currentUser = user;
        initializeComponents();
        loadPermissions();
        loadRoomInfoAndMates();
        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BG_LIGHT);

        // --- 1. HEADER (BANNER) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(40, 44, 52)); // Dark Header
        pnlHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        lblWelcome = new JLabel("Hoşgeldin, " + currentUser.getAdSoyad());
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel pnlHeadRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        pnlHeadRight.setOpaque(false);

        lblOdaBilgisi = new JLabel("Oda: Yükleniyor...");
        lblOdaBilgisi.setForeground(new Color(255, 193, 7)); // Amber
        lblOdaBilgisi.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnProfile = UIHelper.createModernButton("👤 Profilim", new Color(70, 130, 180));
        btnProfile.setPreferredSize(new Dimension(120, 35));
        btnProfile.addActionListener(e -> showUpdateProfileDialog());

        pnlHeadRight.add(lblOdaBilgisi);
        pnlHeadRight.add(btnProfile);

        pnlHeader.add(lblWelcome, BorderLayout.WEST);
        pnlHeader.add(pnlHeadRight, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. DASHBOARD BODY ---
        JPanel pnlBody = new JPanel(new GridLayout(1, 3, 20, 20));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- WIDGET 1: İZİN TALEP ---
        JPanel pnlW1 = UIHelper.createCardPanel();
        pnlW1.setLayout(new BorderLayout());
        pnlW1.add(UIHelper.createHeaderLabel("📝  Yeni İzin Talebi"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.weightx = 1.0;

        form.add(new JLabel("Başlangıç Tarihi (GG.AA.YYYY)"), g);
        txtBaslangic = new JTextField();
        txtBaslangic.putClientProperty("FlatLaf.style", "padding: 5,5,5,5");
        form.add(txtBaslangic, g);

        form.add(new JLabel("Bitiş Tarihi (GG.AA.YYYY)"), g);
        txtBitis = new JTextField();
        txtBitis.putClientProperty("FlatLaf.style", "padding: 5,5,5,5");
        form.add(txtBitis, g);

        form.add(new JLabel("Sebep"), g);
        txtSebep = new JTextField();
        txtSebep.putClientProperty("FlatLaf.style", "padding: 5,5,5,5");
        form.add(txtSebep, g);

        g.insets = new Insets(30, 5, 5, 5);
        JButton btnSend = UIHelper.createModernButton("Talebi Gönder 🚀", new Color(46, 204, 113));
        btnSend.addActionListener(e -> sendPermissionRequest());
        form.add(btnSend, g);

        // Formu yukarı hizalamak için wrapper
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.add(form, BorderLayout.NORTH);
        pnlW1.add(formWrapper, BorderLayout.CENTER);

        // --- WIDGET 2: ODA ARKADAŞLARI ---
        JPanel pnlW2 = UIHelper.createCardPanel();
        pnlW2.setLayout(new BorderLayout());
        pnlW2.add(UIHelper.createHeaderLabel("👥  Oda Arkadaşlarım"), BorderLayout.NORTH);

        String[] colMates = { "Ad", "Soyad", "İletişim" };
        modelRoommates = new DefaultTableModel(colMates, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tblMates = new JTable(modelRoommates);
        UIHelper.decorateTable(tblMates);
        pnlW2.add(new JScrollPane(tblMates), BorderLayout.CENTER);

        // --- WIDGET 3: GEÇMİŞ İZİNLERİM ---
        JPanel pnlW3 = UIHelper.createCardPanel();
        pnlW3.setLayout(new BorderLayout());
        pnlW3.add(UIHelper.createHeaderLabel("📅  İzin Geçmişim"), BorderLayout.NORTH);

        String[] colPerms = { "Tarih Aralığı", "Sebep", "Durum" };
        modelPermissions = new DefaultTableModel(colPerms, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tblPerms = new JTable(modelPermissions);
        UIHelper.decorateTable(tblPerms);
        // Durum sütununa renk katmak için renderer yazılabilir ama basit tutalım.
        pnlW3.add(new JScrollPane(tblPerms), BorderLayout.CENTER);

        pnlBody.add(pnlW1);
        pnlBody.add(pnlW2);
        pnlBody.add(pnlW3);

        add(pnlBody, BorderLayout.CENTER);
    }

    // --- LOGIC METHODS ---
    // (Aynı mantık)

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

    private void sendPermissionRequest() {
        String baslangic = txtBaslangic.getText();
        String bitis = txtBitis.getText();
        String sebep = txtSebep.getText();

        if (baslangic.isEmpty() || bitis.isEmpty() || sebep.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!");
            return;
        }

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