package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.patterns.Factory.UserFactory;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginView extends BasePage {

    private JTextField txtGirisBilgisi; // Adı değişti (TC/Email/Username olabilir)
    private JPasswordField txtSifre;
    private JButton btnGiris;

    public LoginView() {
        super("Yurt Otomasyonu - Giriş", 400, 300);
        initializeComponents();
        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        // Label Güncellemesi
        JLabel lblInfo = new JLabel("TC / Email / Kullanıcı Adı:");
        lblInfo.setBounds(50, 50, 200, 25);
        add(lblInfo);

        txtGirisBilgisi = new JTextField();
        txtGirisBilgisi.setBounds(50, 75, 250, 25);
        add(txtGirisBilgisi);

        JLabel lblPass = new JLabel("Şifre:");
        lblPass.setBounds(50, 110, 100, 25);
        add(lblPass);

        txtSifre = new JPasswordField();
        txtSifre.setBounds(50, 135, 250, 25);
        add(txtSifre);

        btnGiris = new JButton("Giriş Yap");
        btnGiris.setBounds(125, 180, 100, 30);
        add(btnGiris);

        btnGiris.addActionListener(e -> loginIslemi());
    }

    private void loginIslemi() {
        String girisBilgisi = txtGirisBilgisi.getText();
        String sifre = new String(txtSifre.getPassword());

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
                JOptionPane.showMessageDialog(this, "Hatalı Giriş Bilgisi veya Şifre!", "Hata", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı hatası!");
        }
    }
}