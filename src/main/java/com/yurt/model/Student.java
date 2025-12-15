package com.yurt.model;

import com.yurt.patterns.observer.Observer;
import javax.swing.JOptionPane;

public class Student extends User implements Observer {
    // Öğrenciye özel ekstra alanlar (Veritabanında student_details tablosunda tutuluyor)
    private String telefon;
    private String adres;
    private int odaId;

    public Student() {
        this.rol = "OGRENCI"; // Varsayılan rol
    }

    @Override
    public void showRoleInfo() {
        System.out.println("Ben bir ÖĞRENCİYİM.");
    }

    // Observer Deseni: Bildirim Geldiğinde Çalışır
    @Override
    public void update(String message) {
        JOptionPane.showMessageDialog(null, "SAYIN " + getAdSoyad() + ":\n" + message);
    }

    // --- Getter ve Setterlar ---
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public int getOdaId() { return odaId; }
    public void setOdaId(int odaId) { this.odaId = odaId; }
}