package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.patterns.Factory.UserFactory;
import com.yurt.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginView extends BasePage {

    private JTextField txtGirisBilgisi;
    private JPasswordField txtSifre;
    private JButton btnGiris;

    public LoginView() {
        super("Giriş Yap - Yurt Otomasyonu", 500, 450);
        initializeComponents();
        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        // --- ARKA PLAN: DİNAMİK GRADIENT ---
        // Timer ile renkleri yavaşça değiştirebiliriz ama şimdilik sabit güzel bir
        // geçiş yapalım.
        // Mavi -> Mor geçişi
        Color color1 = new Color(135, 206, 250); // Açık Mavi
        Color color2 = new Color(123, 104, 238); // Medium Slate Blue

        JPanel bgPanel = UIHelper.createGradientPanel(color1, color2);
        bgPanel.setLayout(new GridBagLayout()); // Kartı ortalamak için
        add(bgPanel, BorderLayout.CENTER);

        // --- LOGIN KARTI ---
        JPanel cardPanel = UIHelper.createCardPanel();
        cardPanel.setLayout(new GridBagLayout());
        // Kart Boyutu
        cardPanel.setPreferredSize(new Dimension(380, 450));

        // Kartı ekle
        bgPanel.add(cardPanel);

        // --- İÇERİK ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. Logo / İkon (Unicode kullanarak basit bir ev ikonu)
        JLabel lblIcon = new JLabel("🏠", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        gbc.gridy = 0;
        cardPanel.add(lblIcon, gbc);

        // 2. Başlık
        JLabel lblBaslik = new JLabel("Yurt Sistemi", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBaslik.setForeground(new Color(60, 60, 60));
        gbc.gridy = 1;
        cardPanel.add(lblBaslik, gbc);

        JLabel lblAlt = new JLabel("Hoşgeldiniz, lütfen giriş yapın", SwingConstants.CENTER);
        lblAlt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblAlt.setForeground(Color.GRAY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 20, 10);
        cardPanel.add(lblAlt, gbc);

        // 3. Form Inputları
        gbc.insets = new Insets(5, 10, 5, 10);

        gbc.gridy = 3;
        cardPanel.add(new JLabel("Kullanıcı Bilgisi"), gbc);
        txtGirisBilgisi = new JTextField();
        txtGirisBilgisi.putClientProperty("JTextField.placeholderText", "TC / Email / Kullanıcı Adı");
        txtGirisBilgisi.putClientProperty("FlatLaf.style", "arc: 10; padding: 5,5,5,5");

        gbc.gridy = 4;
        cardPanel.add(txtGirisBilgisi, gbc);

        gbc.gridy = 5;
        cardPanel.add(new JLabel("Şifre"), gbc);
        txtSifre = new JPasswordField();
        txtSifre.putClientProperty("JTextField.placeholderText", "••••••");
        txtSifre.putClientProperty("FlatLaf.style", "arc: 10; padding: 5,5,5,5");
        txtSifre.putClientProperty("JPasswordField.showRevealButton", true);

        gbc.gridy = 6;
        cardPanel.add(txtSifre, gbc);

        // 4. Giriş Butonu (Custom Modern Button)
        gbc.gridy = 7;
        gbc.insets = new Insets(25, 10, 10, 10);
        btnGiris = UIHelper.createModernButton("GÜVENLİ GİRİŞ", new Color(75, 110, 230));
        btnGiris.setPreferredSize(new Dimension(100, 40));
        cardPanel.add(btnGiris, gbc);

        getRootPane().setDefaultButton(btnGiris);
        btnGiris.addActionListener(e -> loginIslemi());
    }

    private void loginIslemi() {
        String girisBilgisi = txtGirisBilgisi.getText().trim();
        String sifre = new String(txtSifre.getPassword());

        if (girisBilgisi.isEmpty() || sifre.isEmpty()) {
            shakeWindow(); // Hata animasyonu
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.", "Eksik Bilgi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
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
                shakeWindow();
                JOptionPane.showMessageDialog(this, "Hatalı Giriş Bilgisi veya Şifre!", "Giriş Başarısız",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı bağlantı hatası!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Küçük bir ekran titreşim animasyonu
    private void shakeWindow() {
        Point p = getLocation();
        try {
            for (int i = 0; i < 3; i++) {
                setLocation(p.x + 10, p.y);
                Thread.sleep(20);
                setLocation(p.x - 10, p.y);
                Thread.sleep(20);
                setLocation(p.x + 10, p.y);
                Thread.sleep(20);
                setLocation(p.x - 10, p.y);
                Thread.sleep(20);
                setLocation(p.x, p.y);
            }
        } catch (InterruptedException ignored) {
        }
    }
}