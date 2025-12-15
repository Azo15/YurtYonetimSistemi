package com.yurt.patterns.builder;

import com.yurt.model.Student;
import com.yurt.model.User;

public class StudentBuilder {
    private String ad;
    private String soyad;
    private String tcNo;
    private String sifre;
    private String email;
    private String telefon;
    private String adres;
    private String kullaniciAdi;

    public StudentBuilder setAd(String ad) { this.ad = ad; return this; }
    public StudentBuilder setSoyad(String soyad) { this.soyad = soyad; return this; }
    public StudentBuilder setTcNo(String tcNo) { this.tcNo = tcNo; return this; }
    public StudentBuilder setSifre(String sifre) { this.sifre = sifre; return this; }
    public StudentBuilder setEmail(String email) { this.email = email; return this; }
    public StudentBuilder setTelefon(String telefon) { this.telefon = telefon; return this; }
    public StudentBuilder setAdres(String adres) { this.adres = adres; return this; }

    // YENİ METOT
    public StudentBuilder setKullaniciAdi(String ka) { this.kullaniciAdi = ka; return this; }

    public User build() {
        Student s = new Student();
        s.setAd(ad);
        s.setSoyad(soyad);
        s.setTcNo(tcNo);
        s.setSifre(sifre);
        s.setEmail(email);

        // Kullanıcı adını User sınıfına aktarmak için setter lazım ama User'da yoksa
        // Şimdilik sadece veritabanına yazarken kullanacağız, o yüzden bu değişkende tutuyoruz.
        // Ancak User sınıfına da eklemek en doğrusudur. Hızlı çözüm için PersonnelView içinde kullanacağız.

        // Not: User sınıfında setKullaniciAdi olmadığı için buraya eklemiyoruz,
        // PersonnelView içinde insert ederken builder.kullaniciAdi'ni alacağız.

        s.setTelefon(telefon);
        s.setAdres(adres);
        s.setRol("OGRENCI");
        return s;
    }

    // Getter (PersonnelView'de kullanmak için)
    public String getKullaniciAdi() { return kullaniciAdi; }
}