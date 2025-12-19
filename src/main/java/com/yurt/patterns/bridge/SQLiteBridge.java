package com.yurt.patterns.bridge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteBridge implements IDatabaseBridge {

    @Override
    public Connection connect() throws SQLException {
        String url = "jdbc:sqlite:yurt_v2.db";
        return DriverManager.getConnection(url);
    }

    @Override
    public void ensureTablesExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tc_no TEXT UNIQUE NOT NULL," +
                    "kullanici_adi TEXT," +
                    "ad TEXT NOT NULL," +
                    "soyad TEXT NOT NULL," +
                    "email TEXT," +
                    "sifre TEXT NOT NULL," +
                    "rol TEXT NOT NULL)");

            // Rooms
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "oda_no TEXT UNIQUE NOT NULL," +
                    "kapasite INTEGER NOT NULL," +
                    "mevcut_kisi INTEGER DEFAULT 0," +
                    "durum TEXT)");

            // Student Details
            stmt.execute("CREATE TABLE IF NOT EXISTS student_details (" +
                    "user_id INTEGER PRIMARY KEY," +
                    "oda_id INTEGER," +
                    "adres TEXT," +
                    "telefon TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(oda_id) REFERENCES rooms(id))");

            // Permissions
            stmt.execute("CREATE TABLE IF NOT EXISTS permissions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "ogrenci_id INTEGER," +
                    "baslangic TEXT," +
                    "bitis TEXT," +
                    "sebep TEXT," +
                    "durum TEXT DEFAULT 'BEKLEMEDE'," +
                    "FOREIGN KEY(ogrenci_id) REFERENCES users(id))");
        }
    }
}
