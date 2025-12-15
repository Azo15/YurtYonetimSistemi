package com.yurt.model;

// Abstract Class (Soyut Sınıf)
public abstract class User {
    protected int id;
    protected String tcNo;
    protected String ad;
    protected String soyad;
    protected String email;
    protected String sifre;
    protected String rol; // "OGRENCI" veya "PERSONEL"

    // Abstract metod (Her alt sınıf bunu kendine göre dolduracak)
    public abstract void showRoleInfo();

    // --- GETTER VE SETTER METODLARI (Builder için bunlar ŞART) ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTcNo() { return tcNo; }
    public void setTcNo(String tcNo) { this.tcNo = tcNo; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    // Yardımcı metod: Tam ad döndürür
    public String getAdSoyad() {
        return ad + " " + soyad;
    }
}