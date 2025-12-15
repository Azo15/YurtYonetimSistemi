package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.model.DateUtils;

import javax.swing.*;
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
        super("Öğrenci Paneli - " + user.getAdSoyad(), 1000, 600);
        this.currentUser = user;
        initializeComponents();
        loadPermissions();
        loadRoomInfoAndMates();
        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        // --- 1. ÜST PANEL ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        pnlTop.setBackground(new Color(70, 130, 180));

        JLabel lblHosgeldin = new JLabel("Hoşgeldin, " + currentUser.getAdSoyad());
        lblHosgeldin.setForeground(Color.WHITE);
        lblHosgeldin.setFont(new Font("Arial", Font.BOLD, 14));
        pnlTop.add(lblHosgeldin);

        lblOdaBilgisi = new JLabel("Oda: Atanmamış");
        lblOdaBilgisi.setForeground(Color.YELLOW);
        lblOdaBilgisi.setFont(new Font("Arial", Font.BOLD, 14));
        pnlTop.add(lblOdaBilgisi);

        // Profil Güncelleme Butonu
        JButton btnProfil = new JButton("Bilgilerimi Güncelle");
        btnProfil.setBackground(new Color(255, 165, 0)); // Turuncu
        btnProfil.setForeground(Color.WHITE);
        btnProfil.setFocusPainted(false);
        btnProfil.addActionListener(e -> showUpdateProfileDialog());
        pnlTop.add(btnProfil);

        add(pnlTop);
        pnlTop.setBounds(0, 0, 1000, 50);

        // --- 2. SOL PANEL (İZİN İSTEME) ---
        JLabel lblBaslik = new JLabel("YENİ İZİN TALEBİ");
        lblBaslik.setBounds(20, 70, 200, 20);
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblBaslik);

        add(new JLabel("Başlangıç (Gün.Ay.Yıl):")).setBounds(20, 100, 150, 20);
        txtBaslangic = new JTextField();
        txtBaslangic.setBounds(20, 120, 150, 25);
        add(txtBaslangic);

        add(new JLabel("Bitiş (Gün.Ay.Yıl):")).setBounds(20, 150, 150, 20);
        txtBitis = new JTextField();
        txtBitis.setBounds(20, 170, 150, 25);
        add(txtBitis);

        add(new JLabel("Sebep:")).setBounds(20, 200, 150, 20);
        txtSebep = new JTextField();
        txtSebep.setBounds(20, 220, 250, 25);
        add(txtSebep);

        JButton btnGonder = new JButton("Talep Gönder");
        btnGonder.setBounds(20, 260, 120, 30);
        btnGonder.setBackground(new Color(60, 179, 113));
        btnGonder.setForeground(Color.WHITE);
        add(btnGonder);
        btnGonder.addActionListener(e -> sendPermissionRequest());

        // --- 3. ORTA PANEL (ODA ARKADAŞLARI) ---
        JLabel lblMates = new JLabel("ODA ARKADAŞLARIM");
        lblMates.setBounds(300, 70, 200, 20);
        lblMates.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblMates);

        String[] colMates = {"Ad", "Soyad", "İletişim"};
        // Tabloyu KİLİTLE (Editable False)
        modelRoommates = new DefaultTableModel(colMates, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblRoommates = new JTable(modelRoommates);
        JScrollPane scrollMates = new JScrollPane(tblRoommates);
        scrollMates.setBounds(300, 100, 300, 400);
        add(scrollMates);

        // --- 4. SAĞ PANEL (İZİN GEÇMİŞİ) ---
        JLabel lblGecmis = new JLabel("İZİN GEÇMİŞİM");
        lblGecmis.setBounds(650, 70, 200, 20);
        lblGecmis.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblGecmis);

        String[] colPerms = {"Tarih Aralığı", "Sebep", "Durum"};
        // Tabloyu KİLİTLE
        modelPermissions = new DefaultTableModel(colPerms, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblPermissions = new JTable(modelPermissions);

        // SÜTUN GENİŞLİĞİ AYARI
        tblPermissions.getColumnModel().getColumn(0).setPreferredWidth(160);

        JScrollPane scrollPerms = new JScrollPane(tblPermissions);
        scrollPerms.setBounds(650, 100, 300, 400);
        add(scrollPerms);
    }

    // --- PROFİL GÜNCELLEME PENCERESİ ---
    private void showUpdateProfileDialog() {
        JDialog dialog = new JDialog(this, "Profil Bilgilerini Güncelle", true);
        dialog.setSize(400, 350);
        dialog.setLayout(null);
        dialog.setLocationRelativeTo(this);

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
        } catch (Exception ex) { ex.printStackTrace(); }

        dialog.add(new JLabel("Email:")).setBounds(30, 30, 100, 20);
        JTextField txtEmail = new JTextField(currentEmail); txtEmail.setBounds(140, 30, 200, 25); dialog.add(txtEmail);

        dialog.add(new JLabel("Telefon:")).setBounds(30, 70, 100, 20);
        JTextField txtTel = new JTextField(currentTel); txtTel.setBounds(140, 70, 200, 25); dialog.add(txtTel);

        dialog.add(new JLabel("Adres:")).setBounds(30, 110, 100, 20);
        JTextArea txtAdres = new JTextArea(currentAdres); txtAdres.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtAdres.setBounds(140, 110, 200, 60); dialog.add(txtAdres);

        dialog.add(new JLabel("Yeni Şifre:")).setBounds(30, 190, 100, 20);
        JPasswordField txtPass = new JPasswordField(currentUser.getSifre()); txtPass.setBounds(140, 190, 200, 25); dialog.add(txtPass);

        JButton btnSave = new JButton("Kaydet");
        btnSave.setBounds(140, 240, 100, 30);
        btnSave.setBackground(new Color(60, 179, 113));
        btnSave.setForeground(Color.WHITE);
        dialog.add(btnSave);

        btnSave.addActionListener(e -> {
            updateProfileInDB(txtEmail.getText(), txtTel.getText(), txtAdres.getText(), new String(txtPass.getPassword()));
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void updateProfileInDB(String mail, String tel, String adr, String pass) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            String sqlUser = "UPDATE users SET email = ?, sifre = ? WHERE id = ?";
            PreparedStatement psUser = conn.prepareStatement(sqlUser);
            psUser.setString(1, mail); psUser.setString(2, pass); psUser.setInt(3, currentUser.getId());
            psUser.executeUpdate();

            ResultSet rsCheck = conn.createStatement().executeQuery("SELECT * FROM student_details WHERE user_id=" + currentUser.getId());
            if (rsCheck.next()) {
                String sqlDet = "UPDATE student_details SET telefon = ?, adres = ? WHERE user_id = ?";
                PreparedStatement psDet = conn.prepareStatement(sqlDet);
                psDet.setString(1, tel); psDet.setString(2, adr); psDet.setInt(3, currentUser.getId());
                psDet.executeUpdate();
            } else {
                String sqlIns = "INSERT INTO student_details (user_id, telefon, adres) VALUES (?, ?, ?)";
                PreparedStatement psIns = conn.prepareStatement(sqlIns);
                psIns.setInt(1, currentUser.getId()); psIns.setString(2, tel); psIns.setString(3, adr);
                psIns.executeUpdate();
            }

            currentUser.setEmail(mail); currentUser.setSifre(pass);
            JOptionPane.showMessageDialog(this, "Bilgileriniz başarıyla güncellendi!");

        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
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
            JOptionPane.showMessageDialog(this, "Tarih formatı GG.AA.YYYY olmalı! (Örn: 15.05.2025)", "Format Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DateUtils.isReasonableDate(baslangic) || !DateUtils.isReasonableDate(bitis)) {
            JOptionPane.showMessageDialog(this, "Lütfen 2024-2026 arası bir yıl giriniz.", "Tarih Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DateUtils.isFutureOrPresent(baslangic)) {
            JOptionPane.showMessageDialog(this, "Geçmiş bir tarihe izin alamazsınız!", "Tarih Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DateUtils.isStartBeforeEnd(baslangic, bitis)) {
            JOptionPane.showMessageDialog(this, "Bitiş tarihi, başlangıçtan önce olamaz!", "Mantık Hatası", JOptionPane.ERROR_MESSAGE);
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
            txtBaslangic.setText(""); txtBitis.setText(""); txtSebep.setText("");
            loadPermissions();
        } catch (SQLException ex) { ex.printStackTrace(); }
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
                while(rsMates.next()){
                    String tel = rsMates.getString("telefon");
                    modelRoommates.addRow(new Object[]{rsMates.getString("ad"), rsMates.getString("soyad"), (tel != null && !tel.isEmpty()) ? tel : "Girmedi"});
                }
            } else {
                lblOdaBilgisi.setText("Kaldığınız Oda: Henüz Atanmadı");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPermissions() {
        modelPermissions.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM permissions WHERE ogrenci_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUser.getId());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                modelPermissions.addRow(new Object[]{rs.getString("baslangic") + " - " + rs.getString("bitis"), rs.getString("sebep"), rs.getString("durum")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}