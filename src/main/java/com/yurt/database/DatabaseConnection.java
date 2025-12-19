package com.yurt.database;

import com.yurt.patterns.bridge.IDatabaseBridge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private IDatabaseBridge bridge;
    private Connection connection;

    private DatabaseConnection() {
        // Varsayılan olarak SQLite ile başla
        this.bridge = new com.yurt.patterns.bridge.SQLiteBridge();
        connect();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Köprü (Bridge) Değiştirme Metodu
    public void setBridge(IDatabaseBridge bridge) {
        this.bridge = bridge;
        // Mevcut bağlantı varsa kapatılabilir (basitlik için direkt yeni connect
        // yapıyoruz)
        connect();
    }

    private void connect() {
        try {
            this.connection = bridge.connect();
            bridge.ensureTablesExist(this.connection);
            createDefaultAdmin(); // Admin kontrolünü her zaman yap
            System.out.println("Veritabanı Bağlantısı Başarılı: " + bridge.getClass().getSimpleName());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Veritabanı bağlantı hatası!");
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    // --- ADMIN OLUŞTURMA (Her veritabanı için geçerli) ---
    private void createDefaultAdmin() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE rol = 'PERSONEL'");
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO users (tc_no, kullanici_adi, ad, soyad, email, sifre, rol) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(sql);

                ps.setString(1, "10000000000");
                ps.setString(2, "admin");
                ps.setString(3, "Sistem");
                ps.setString(4, "Yöneticisi");
                ps.setString(5, "admin@yurt.com");
                ps.setString(6, "1453");
                ps.setString(7, "PERSONEL");

                ps.executeUpdate();
                System.out.println("Varsayılan YÖNETİCİ oluşturuldu.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}