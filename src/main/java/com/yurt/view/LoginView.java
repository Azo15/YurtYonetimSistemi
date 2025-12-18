package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.patterns.Factory.UserFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginView extends BasePage {

    private JTextField txtGirisBilgisi;
    private JPasswordField txtSifre;
    private JButton btnGiris;

    public LoginView() {
        super("Giriş Yap - Yurt Otomasyonu", 450, 400); // Biraz daha karemsi, modern boyut
        initializeComponents();
        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        // Arka planı kullanarak ortala
        JPanel mainPanel = new JPanel(new GridBagLayout());
        add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- BAŞLIK ---
        JLabel lblHeader = new JLabel("Yurt Yönetim Sistemi", SwingConstants.CENTER);
        lblHeader.putClientProperty("FlatLaf.style", "font: bold +10");
        lblHeader.setForeground(new Color(50, 50, 50));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(lblHeader, gbc);

        JLabel lblSubHeader = new JLabel("Lütfen kimliğinizi doğrulayın", SwingConstants.CENTER);
        lblSubHeader.putClientProperty("FlatLaf.style", "font: +2");
        lblSubHeader.setForeground(Color.GRAY);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 30, 10); // Başlık ile form arası boşluk
        mainPanel.add(lblSubHeader, gbc);

        // --- FORM ---
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Kullanıcı
        JLabel lblInfo = new JLabel("TC / Email / Kullanıcı Adı:");
        lblInfo.putClientProperty("FlatLaf.style", "font: bold");
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(lblInfo, gbc);

        txtGirisBilgisi = new JTextField(20);
        txtGirisBilgisi.putClientProperty("JTextField.placeholderText", "Örn: admin veya 12345678901");
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(txtGirisBilgisi, gbc);

        // Şifre
        JLabel lblPass = new JLabel("Şifre:");
        lblPass.putClientProperty("FlatLaf.style", "font: bold");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 10, 5, 10); // Biraz ayır
        mainPanel.add(lblPass, gbc);

        txtSifre = new JPasswordField(20);
        txtSifre.putClientProperty("JTextField.placeholderText", "********");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 5, 10);
        mainPanel.add(txtSifre, gbc);

        // Buton
        btnGiris = new JButton("Giriş Yap");
        btnGiris.putClientProperty("FlatLaf.style", "font: bold +2");
        btnGiris.setBackground(new Color(30, 136, 229)); // Modern Mavi
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setFocusPainted(false);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(25, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Buton tüm satırı kaplasın
        gbc.ipady = 10; // Butonu biraz yüksek yap
        mainPanel.add(btnGiris, gbc);

        // ENTER TUŞU ÇALIŞSIN
        getRootPane().setDefaultButton(btnGiris);

        btnGiris.addActionListener(e -> loginIslemi());
    }

    private void loginIslemi() {
        String girisBilgisi = txtGirisBilgisi.getText().trim();
        String sifre = new String(txtSifre.getPassword());

        if (girisBilgisi.isEmpty() || sifre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.", "Eksik Bilgi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            // ÇOKLU GİRİŞ SORGUSU
            String sql = "SELECT * FROM users WHERE (tc_no = ? OR email = ? OR kullanici_adi = ?) AND sifre = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, girisBilgisi);
            ps.setString(2, girisBilgisi);
            ps.setString(3, girisBilgisi);
            ps.setString(4, sifre);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String rol = rs.getString("rol");
                User user = UserFactory.createUser(rol);

                if (user != null) {
                    user.setId(rs.getInt("id"));
                    user.setTcNo(rs.getString("tc_no"));
                    user.setAd(rs.getString("ad"));
                    user.setSoyad(rs.getString("soyad"));
                    user.setEmail(rs.getString("email"));
                    user.setSifre(rs.getString("sifre"));

                    this.dispose();

                    if (rol.equalsIgnoreCase("OGRENCI")) {
                        new StudentView(user);
                    } else if (rol.equalsIgnoreCase("PERSONEL")) {
                        new PersonnelView(user);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı Giriş Bilgisi veya Şifre!", "Giriş Başarısız",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı bağlantı hatası!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}