package com.yurt.patterns.bridge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLBridge implements IDatabaseBridge {

    @Override
    public Connection connect() throws SQLException {
        // Localhost default ayarlarıyla bağlanmayı dener
        // Eğer şifre varsa burası güncellenmeli. Genelde root şifresiz veya basittir.
        String url = "jdbc:mysql://localhost:3306/yurt_yönetim?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, "root", ""); // Varsayılan: root, şifre yok
    }

    @Override
    public void ensureTablesExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // MySQL Syntax: AUTO_INCREMENT (vs SQLite AUTOINCREMENT)

            // Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "tc_no VARCHAR(11) UNIQUE NOT NULL," +
                    "kullanici_adi VARCHAR(50)," +
                    "ad VARCHAR(50) NOT NULL," +
                    "soyad VARCHAR(50) NOT NULL," +
                    "email VARCHAR(100)," +
                    "sifre VARCHAR(100) NOT NULL," +
                    "rol VARCHAR(20) NOT NULL)");

            // Rooms
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "oda_no VARCHAR(10) UNIQUE NOT NULL," +
                    "kapasite INT NOT NULL," +
                    "mevcut_kisi INT DEFAULT 0," +
                    "durum VARCHAR(20))");

            // Student Details
            stmt.execute("CREATE TABLE IF NOT EXISTS student_details (" +
                    "user_id INT PRIMARY KEY," +
                    "oda_id INT," +
                    "adres TEXT," +
                    "telefon VARCHAR(20)," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(oda_id) REFERENCES rooms(id))");

            // Permissions
            stmt.execute("CREATE TABLE IF NOT EXISTS permissions (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "ogrenci_id INT," +
                    "baslangic VARCHAR(20)," +
                    "bitis VARCHAR(20)," +
                    "sebep TEXT," +
                    "durum VARCHAR(20) DEFAULT 'BEKLEMEDE'," +
                    "FOREIGN KEY(ogrenci_id) REFERENCES users(id))");
        }
    }
}
