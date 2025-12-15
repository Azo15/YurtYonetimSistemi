package com.yurt.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            // Veritabanı dosya adı
            String url = "jdbc:sqlite:yurt_v2.db";
            connection = DriverManager.getConnection(url);
            createTables();
            createDefaultAdmin(); // Admin yoksa oluştur
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {

            // Users Tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tc_no TEXT UNIQUE NOT NULL," +
                    "kullanici_adi TEXT," +
                    "ad TEXT NOT NULL," +
                    "soyad TEXT NOT NULL," +
                    "email TEXT," +
                    "sifre TEXT NOT NULL," +
                    "rol TEXT NOT NULL)");

            // Odalar
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "oda_no TEXT UNIQUE NOT NULL," +
                    "kapasite INTEGER NOT NULL," +
                    "mevcut_kisi INTEGER DEFAULT 0," +
                    "durum TEXT)");

            // Öğrenci Detayları
            stmt.execute("CREATE TABLE IF NOT EXISTS student_details (" +
                    "user_id INTEGER PRIMARY KEY," +
                    "oda_id INTEGER," +
                    "adres TEXT," +
                    "telefon TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(oda_id) REFERENCES rooms(id))");

            // İzinler
            stmt.execute("CREATE TABLE IF NOT EXISTS permissions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "ogrenci_id INTEGER," +
                    "baslangic TEXT," +
                    "bitis TEXT," +
                    "sebep TEXT," +
                    "durum TEXT DEFAULT 'BEKLEMEDE'," +
                    "FOREIGN KEY(ogrenci_id) REFERENCES users(id))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- ADMIN OLUŞTURMA (TC: 11 HANE, ŞİFRE: 1453) ---
    private void createDefaultAdmin() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE rol = 'PERSONEL'");
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO users (tc_no, kullanici_adi, ad, soyad, email, sifre, rol) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(sql);

                ps.setString(1, "10000000000");   // TC Kimlik (11 Hane)
                ps.setString(2, "admin");         // Kullanıcı Adı
                ps.setString(3, "Sistem");
                ps.setString(4, "Yöneticisi");
                ps.setString(5, "admin@yurt.com");
                ps.setString(6, "1453");

                ps.setString(7, "PERSONEL");

                ps.executeUpdate();
                System.out.println("Varsayılan YÖNETİCİ oluşturuldu.");
                System.out.println("TC: 10000000000");
                System.out.println("K.Adı: admin");
                System.out.println("Email: admin@yurt.com");
                System.out.println("Şifre: 1453");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}