package com.yurt.view;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.User;
import com.yurt.patterns.builder.StudentBuilder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PersonnelView extends BasePage {

    private User currentUser;

    // Tablolar
    private JTable tblRequests, tblRooms, tblStudents;
    private DefaultTableModel modelRequests, modelRooms, modelStudents;

    // Form Elemanları
    private JTextField txtOdaBaslangic, txtOdaBitis, txtKapasite;
    private JTextField txtOgrenciTc, txtAtanacakOda;

    // Öğrenci Ekleme Formu
    private JTextField txtYeniAd, txtYeniSoyad, txtYeniTc, txtYeniSifre, txtYeniEmail, txtYeniTel, txtYeniKadi;

    // Arama ve Raporlama
    private JTextField txtOgrenciAra;
    private JTextField txtRaporBaslangic;

    public PersonnelView(User user) {
        // Ekranı genişlettik ki yazılar sığsın
        super("Yurt Yönetim Paneli - " + user.getAdSoyad(), 1280, 750);
        this.currentUser = user;
        initializeComponents();

        loadPermissionRequests("BEKLEMEDE");
        loadRooms();
        loadStudents("");

        setVisible(true);
    }

    @Override
    public void initializeComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(10, 10, 1240, 680);

        // ==========================================
        // SEKME 1: İZİN YÖNETİMİ
        // ==========================================
        JPanel pnlPermissions = new JPanel(null);
        JLabel lblBaslik1 = new JLabel("İZİN TALEPLERİ VE RAPORLAMA");
        lblBaslik1.setFont(new Font("Arial", Font.BOLD, 14));
        lblBaslik1.setBounds(20, 20, 300, 20);
        pnlPermissions.add(lblBaslik1);

        // Butonlar
        JButton btnBekleyenler = new JButton("Onay Bekleyenleri Göster");
        btnBekleyenler.setBounds(20, 50, 220, 30);
        btnBekleyenler.setBackground(Color.ORANGE);
        pnlPermissions.add(btnBekleyenler);

        JButton btnGecmis = new JButton("Geçmiş İşlemleri Listele");
        btnGecmis.setBounds(250, 50, 220, 30);
        btnGecmis.setBackground(new Color(100, 149, 237));
        btnGecmis.setForeground(Color.WHITE);
        pnlPermissions.add(btnGecmis);

        JLabel lblTarih = new JLabel("Yıla Göre Ara (Örn: 2025):");
        lblTarih.setBounds(500, 50, 180, 30);
        pnlPermissions.add(lblTarih);
        txtRaporBaslangic = new JTextField();
        txtRaporBaslangic.setBounds(680, 50, 100, 30);
        pnlPermissions.add(txtRaporBaslangic);
        JButton btnAraTarih = new JButton("Ara");
        btnAraTarih.setBounds(790, 50, 80, 30);
        pnlPermissions.add(btnAraTarih);

        // TABLO (Hücre Düzenleme Kapatıldı)
        String[] colPerms = {"ID", "Öğrenci Adı", "TC No", "Başlangıç", "Bitiş", "Sebep", "Durum"};
        modelRequests = new DefaultTableModel(colPerms, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRequests = new JTable(modelRequests);
        JScrollPane scrollPerms = new JScrollPane(tblRequests);
        scrollPerms.setBounds(20, 90, 1200, 350);
        pnlPermissions.add(scrollPerms);

        // İşlem Butonları
        JButton btnOnayla = new JButton("SEÇİLENİ ONAYLA");
        btnOnayla.setBounds(350, 460, 150, 40);
        btnOnayla.setBackground(new Color(60, 179, 113)); btnOnayla.setForeground(Color.WHITE);
        pnlPermissions.add(btnOnayla);

        JButton btnReddet = new JButton("SEÇİLENİ REDDET");
        btnReddet.setBounds(520, 460, 150, 40);
        btnReddet.setBackground(new Color(220, 20, 60)); btnReddet.setForeground(Color.WHITE);
        pnlPermissions.add(btnReddet);

        JButton btnIzinSil = new JButton("KAYDI SİL");
        btnIzinSil.setBounds(690, 460, 150, 40);
        btnIzinSil.setBackground(Color.BLACK); btnIzinSil.setForeground(Color.WHITE);
        pnlPermissions.add(btnIzinSil);

        btnBekleyenler.addActionListener(e -> loadPermissionRequests("BEKLEMEDE"));
        btnGecmis.addActionListener(e -> loadPermissionHistory());
        btnAraTarih.addActionListener(e -> searchPermissionsByDate(txtRaporBaslangic.getText()));
        btnOnayla.addActionListener(e -> updatePermissionStatus("ONAYLANDI"));
        btnReddet.addActionListener(e -> updatePermissionStatus("REDDEDILDI"));
        btnIzinSil.addActionListener(e -> deletePermission());

        tabbedPane.addTab("İzin Yönetimi", pnlPermissions);

        // ==========================================
        // SEKME 2: ODA VE KAYIT
        // ==========================================
        JPanel pnlRooms = new JPanel(null);
        int rightX = 580; // Alanı genişlettik

        JLabel lblBaslik2 = new JLabel("MEVCUT ODALAR");
        lblBaslik2.setFont(new Font("Arial", Font.BOLD, 14));
        lblBaslik2.setBounds(20, 20, 200, 20);
        pnlRooms.add(lblBaslik2);

        String[] colRooms = {"ID", "Oda No", "Kapasite", "Mevcut", "Durum"};
        modelRooms = new DefaultTableModel(colRooms, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRooms = new JTable(modelRooms);
        JScrollPane scrollRooms = new JScrollPane(tblRooms);
        scrollRooms.setBounds(20, 50, 500, 500);
        pnlRooms.add(scrollRooms);

        JButton btnOdaSil = new JButton("Seçili Odayı Sil");
        btnOdaSil.setBounds(20, 560, 500, 30);
        btnOdaSil.setBackground(Color.RED); btnOdaSil.setForeground(Color.WHITE);
        pnlRooms.add(btnOdaSil);

        // --- SAĞ TARAF FORM (Uzun İsimler) ---

        JLabel lblOdaEkle = new JLabel("--- TOPLU ODA OLUŞTURMA ---");
        lblOdaEkle.setBounds(rightX, 20, 300, 20); lblOdaEkle.setForeground(Color.BLUE);
        pnlRooms.add(lblOdaEkle);

        pnlRooms.add(new JLabel("Başlangıç Numarası:")).setBounds(rightX, 50, 140, 25);
        txtOdaBaslangic = new JTextField(); txtOdaBaslangic.setBounds(rightX + 140, 50, 60, 25); pnlRooms.add(txtOdaBaslangic);

        pnlRooms.add(new JLabel("Bitiş Numarası:")).setBounds(rightX + 210, 50, 120, 25);
        txtOdaBitis = new JTextField(); txtOdaBitis.setBounds(rightX + 310, 50, 60, 25); pnlRooms.add(txtOdaBitis);

        pnlRooms.add(new JLabel("Kapasite:")).setBounds(rightX + 380, 50, 80, 25);
        txtKapasite = new JTextField(); txtKapasite.setBounds(rightX + 450, 50, 50, 25); pnlRooms.add(txtKapasite);

        JButton btnOdaEkle = new JButton("Oda Ekle");
        btnOdaEkle.setBounds(rightX + 510, 50, 110, 25);
        pnlRooms.add(btnOdaEkle);

        JLabel lblOgrEkle = new JLabel("--- YENİ ÖĞRENCİ KAYIT FORMU ---");
        lblOgrEkle.setBounds(rightX, 100, 300, 20); lblOgrEkle.setForeground(Color.BLUE);
        pnlRooms.add(lblOgrEkle);

        // Satır 1
        pnlRooms.add(new JLabel("Ad:")).setBounds(rightX, 130, 80, 25);
        txtYeniAd = new JTextField(); txtYeniAd.setBounds(rightX + 80, 130, 150, 25); pnlRooms.add(txtYeniAd);

        pnlRooms.add(new JLabel("Soyad:")).setBounds(rightX + 250, 130, 80, 25);
        txtYeniSoyad = new JTextField(); txtYeniSoyad.setBounds(rightX + 330, 130, 150, 25); pnlRooms.add(txtYeniSoyad);

        // Satır 2
        pnlRooms.add(new JLabel("TC Kimlik:")).setBounds(rightX, 170, 80, 25);
        txtYeniTc = new JTextField(); txtYeniTc.setBounds(rightX + 80, 170, 150, 25); pnlRooms.add(txtYeniTc);

        txtYeniTc.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (!txtYeniTc.getText().matches("\\d*")) txtYeniTc.setText(txtYeniTc.getText().replaceAll("[^\\d]", ""));
                if (txtYeniTc.getText().length() != 11) txtYeniTc.setBackground(new Color(255, 200, 200)); else txtYeniTc.setBackground(Color.WHITE);
            }
            public void keyTyped(KeyEvent e) { if (txtYeniTc.getText().length() >= 11) e.consume(); }
        });

        pnlRooms.add(new JLabel("Şifre:")).setBounds(rightX + 250, 170, 80, 25);
        txtYeniSifre = new JTextField(); txtYeniSifre.setBounds(rightX + 330, 170, 150, 25); pnlRooms.add(txtYeniSifre);

        // Satır 3
        pnlRooms.add(new JLabel("Kullanıcı Adı:")).setBounds(rightX, 210, 100, 25);
        txtYeniKadi = new JTextField(); txtYeniKadi.setBounds(rightX + 100, 210, 130, 25); pnlRooms.add(txtYeniKadi);

        pnlRooms.add(new JLabel("E-Posta:")).setBounds(rightX + 250, 210, 80, 25);
        txtYeniEmail = new JTextField(); txtYeniEmail.setBounds(rightX + 330, 210, 150, 25); pnlRooms.add(txtYeniEmail);

        // Satır 4
        pnlRooms.add(new JLabel("Telefon:")).setBounds(rightX, 250, 80, 25);
        txtYeniTel = new JTextField(); txtYeniTel.setBounds(rightX + 80, 250, 150, 25); pnlRooms.add(txtYeniTel);

        JButton btnOgrKaydet = new JButton("Öğrenciyi Sisteme Kaydet");
        btnOgrKaydet.setBounds(rightX + 250, 250, 230, 30);
        btnOgrKaydet.setBackground(new Color(255, 140, 0)); btnOgrKaydet.setForeground(Color.WHITE);
        pnlRooms.add(btnOgrKaydet);

        JLabel lblAtama = new JLabel("--- ÖĞRENCİ ODA YERLEŞTİRME ---");
        lblAtama.setBounds(rightX, 320, 300, 20); lblAtama.setForeground(Color.BLUE);
        pnlRooms.add(lblAtama);

        pnlRooms.add(new JLabel("Öğrenci TC:")).setBounds(rightX, 350, 100, 25);
        txtOgrenciTc = new JTextField(); txtOgrenciTc.setBounds(rightX + 100, 350, 150, 25); pnlRooms.add(txtOgrenciTc);

        pnlRooms.add(new JLabel("Oda No:")).setBounds(rightX + 270, 350, 80, 25);
        txtAtanacakOda = new JTextField(); txtAtanacakOda.setBounds(rightX + 330, 350, 100, 25); pnlRooms.add(txtAtanacakOda);

        JButton btnAta = new JButton("Yerleştir / Transfer Et");
        btnAta.setBounds(rightX + 100, 390, 250, 30);
        pnlRooms.add(btnAta);

        btnOdaEkle.addActionListener(e -> addBulkRooms());
        btnOgrKaydet.addActionListener(e -> addNewStudent());
        btnAta.addActionListener(e -> assignStudentToRoom());
        btnOdaSil.addActionListener(e -> deleteRoom());

        tabbedPane.addTab("Oda ve Kayıt İşlemleri", pnlRooms);

        // --- SEKME 3: ÖĞRENCİ LİSTESİ ---
        JPanel pnlStudentList = new JPanel(null);
        JLabel lblListeBaslik = new JLabel("SİSTEMDE KAYITLI ÖĞRENCİLER LİSTESİ");
        lblListeBaslik.setFont(new Font("Arial", Font.BOLD, 14));
        lblListeBaslik.setBounds(20, 20, 400, 20);
        pnlStudentList.add(lblListeBaslik);

        pnlStudentList.add(new JLabel("Ara (Ad/Soyad/Oda No):")).setBounds(20, 50, 150, 25);
        txtOgrenciAra = new JTextField(); txtOgrenciAra.setBounds(170, 50, 200, 25); pnlStudentList.add(txtOgrenciAra);

        JButton btnAra = new JButton("Ara"); btnAra.setBounds(380, 50, 80, 25); pnlStudentList.add(btnAra);
        JButton btnYenile = new JButton("Listeyi Yenile"); btnYenile.setBounds(470, 50, 120, 25); pnlStudentList.add(btnYenile);

        JButton btnOgrSil = new JButton("Seçili Öğrenciyi Sil");
        btnOgrSil.setBounds(950, 50, 150, 25);
        btnOgrSil.setBackground(Color.RED); btnOgrSil.setForeground(Color.WHITE);
        pnlStudentList.add(btnOgrSil);

        String[] colStudents = {"ID", "TC No", "Ad", "Soyad", "Kullanıcı Adı", "Kaldığı Oda"};
        modelStudents = new DefaultTableModel(colStudents, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblStudents = new JTable(modelStudents);
        JScrollPane scrollStudents = new JScrollPane(tblStudents);
        scrollStudents.setBounds(20, 90, 1200, 450);
        pnlStudentList.add(scrollStudents);

        btnAra.addActionListener(e -> loadStudents(txtOgrenciAra.getText()));
        btnYenile.addActionListener(e -> { txtOgrenciAra.setText(""); loadStudents(""); });
        btnOgrSil.addActionListener(e -> deleteStudent());

        tabbedPane.addTab("Öğrenci Listesi", pnlStudentList);

        add(tabbedPane);
    }

    // --- METOTLAR ---

    // GÜNCELLENMİŞ KAYIT METODU (UNIQUE KONTROLLER)
    private void addNewStudent() {
        String ad = txtYeniAd.getText(); String soyad = txtYeniSoyad.getText(); String tc = txtYeniTc.getText();
        String sifre = txtYeniSifre.getText(); String email = txtYeniEmail.getText(); String tel = txtYeniTel.getText();
        String kadi = txtYeniKadi.getText();

        // 1. ZORUNLU ALANLAR (Email artık zorunlu)
        if(ad.isEmpty() || soyad.isEmpty() || tc.isEmpty() || email.isEmpty() || tel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ad, Soyad, TC, Email ve Telefon zorunludur!", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE); return;
        }
        if (tc.length() != 11) { JOptionPane.showMessageDialog(this, "TC 11 hane olmalı!", "Format Hatası", JOptionPane.WARNING_MESSAGE); return; }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            // 2. MÜKERRER KONTROLLER (Telefon, Email, Kullanıcı Adı)
            if (checkIfExists(conn, "student_details", "telefon", tel)) {
                JOptionPane.showMessageDialog(this, "Bu telefon numarası zaten kayıtlı!", "Mükerrer Kayıt", JOptionPane.ERROR_MESSAGE); return;
            }
            if (checkIfExists(conn, "users", "email", email)) {
                JOptionPane.showMessageDialog(this, "Bu E-posta adresi zaten kayıtlı!", "Mükerrer Kayıt", JOptionPane.ERROR_MESSAGE); return;
            }
            // Eğer kullanıcı adı boşsa otomatik üretilecek, o yüzden kontrolü aşağıda yapıyoruz.

            String finalSifre = sifre.isEmpty() ? tc : sifre;
            String finalKadi = kadi.isEmpty() ? ad.toLowerCase() + tc.substring(0,3) : kadi;

            // Otomatik üretilen veya girilen kullanıcı adı kontrolü
            if (checkIfExists(conn, "users", "kullanici_adi", finalKadi)) {
                JOptionPane.showMessageDialog(this, "Bu kullanıcı adı alınmış (" + finalKadi + ")! Lütfen değiştirin.", "Mükerrer Kayıt", JOptionPane.ERROR_MESSAGE); return;
            }

            StudentBuilder builder = new StudentBuilder();
            User yeniOgrenci = builder.setAd(ad).setSoyad(soyad).setTcNo(tc).setSifre(finalSifre)
                    .setEmail(email).setTelefon(tel).setKullaniciAdi(finalKadi).build();

            String sqlUser = "INSERT INTO users (tc_no, ad, soyad, email, sifre, rol, kullanici_adi) VALUES (?, ?, ?, ?, ?, 'OGRENCI', ?)";
            PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            psUser.setString(1, yeniOgrenci.getTcNo()); psUser.setString(2, yeniOgrenci.getAd()); psUser.setString(3, yeniOgrenci.getSoyad());
            psUser.setString(4, yeniOgrenci.getEmail()); psUser.setString(5, yeniOgrenci.getSifre()); psUser.setString(6, builder.getKullaniciAdi());
            psUser.executeUpdate();

            int userId = -1; ResultSet rsKeys = psUser.getGeneratedKeys();
            if (rsKeys.next()) userId = rsKeys.getInt(1);
            else { ResultSet rsId = conn.createStatement().executeQuery("SELECT last_insert_rowid()"); if(rsId.next()) userId = rsId.getInt(1); }

            if (userId != -1) {
                PreparedStatement psDetail = conn.prepareStatement("INSERT INTO student_details (user_id, adres, telefon) VALUES (?, ?, ?)");
                psDetail.setInt(1, userId); psDetail.setString(2, "Girilmedi"); psDetail.setString(3, tel);
                psDetail.executeUpdate();
                JOptionPane.showMessageDialog(this, "Kayıt Başarılı! K.Adı: " + finalKadi);

                // Temizleme
                txtYeniAd.setText(""); txtYeniSoyad.setText(""); txtYeniTc.setText("");
                txtYeniSifre.setText(""); txtYeniEmail.setText(""); txtYeniTel.setText(""); txtYeniKadi.setText("");
                loadStudents("");
            }
        } catch (SQLException e) {
            if(e.getMessage().contains("UNIQUE")) JOptionPane.showMessageDialog(this, "TC zaten kayıtlı!"); else JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    // Yardımcı Metot: Veritabanında var mı yok mu kontrolü
    private boolean checkIfExists(Connection conn, String table, String column, String value) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM " + table + " WHERE " + column + " = ?");
        ps.setString(1, value);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private void loadRooms() {
        modelRooms.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM rooms");
            while(rs.next()) {
                int kapasite = rs.getInt("kapasite");
                int mevcut = rs.getInt("mevcut_kisi");
                String durum = (mevcut >= kapasite) ? "DOLU" : "MUSAIT";
                modelRooms.addRow(new Object[]{rs.getInt("id"), rs.getString("oda_no"), kapasite, mevcut, durum });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPermissionRequests(String durumFiltresi) {
        modelRequests.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT p.id, u.ad, u.soyad, u.tc_no, p.baslangic, p.bitis, p.sebep, p.durum FROM permissions p JOIN users u ON p.ogrenci_id = u.id WHERE p.durum = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, durumFiltresi);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelRequests.addRow(new Object[]{rs.getInt("id"), rs.getString("ad") + " " + rs.getString("soyad"), rs.getString("tc_no"), rs.getString("baslangic"), rs.getString("bitis"), rs.getString("sebep"), rs.getString("durum")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadPermissionHistory() {
        modelRequests.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT p.id, u.ad, u.soyad, u.tc_no, p.baslangic, p.bitis, p.sebep, p.durum FROM permissions p JOIN users u ON p.ogrenci_id = u.id WHERE p.durum != 'BEKLEMEDE'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                modelRequests.addRow(new Object[]{rs.getInt("id"), rs.getString("ad") + " " + rs.getString("soyad"), rs.getString("tc_no"), rs.getString("baslangic"), rs.getString("bitis"), rs.getString("sebep"), rs.getString("durum")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void searchPermissionsByDate(String dateText) {
        if (dateText.isEmpty()) { loadPermissionRequests("ONAYLANDI"); return; }
        modelRequests.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT p.id, u.ad, u.soyad, u.tc_no, p.baslangic, p.bitis, p.sebep, p.durum FROM permissions p JOIN users u ON p.ogrenci_id = u.id WHERE (p.durum = 'ONAYLANDI' OR p.durum = 'REDDEDILDI') AND (p.baslangic LIKE ? OR p.bitis LIKE ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + dateText + "%"); ps.setString(2, "%" + dateText + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelRequests.addRow(new Object[]{rs.getInt("id"), rs.getString("ad") + " " + rs.getString("soyad"), rs.getString("tc_no"), rs.getString("baslangic"), rs.getString("bitis"), rs.getString("sebep"), rs.getString("durum")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void updatePermissionStatus(String yeniDurum) {
        int selectedRow = tblRequests.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Lütfen listeden bir talep seçin!"); return; }
        try {
            int id = (int) modelRequests.getValueAt(selectedRow, 0);
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE permissions SET durum = ? WHERE id = ?");
            ps.setString(1, yeniDurum); ps.setInt(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "İşlem Tamam: " + yeniDurum);
            String mevcutDurum = (String) modelRequests.getValueAt(selectedRow, 6);
            if (mevcutDurum.equals("BEKLEMEDE")) loadPermissionRequests("BEKLEMEDE"); else loadPermissionHistory();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void deleteStudent() {
        int row = tblStudents.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "Silinecek öğrenciyi seçin!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Öğrenciyi silmek istediğinize emin misiniz?", "Onay", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int userId = (int) modelStudents.getValueAt(row, 0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rsOda = conn.createStatement().executeQuery("SELECT oda_id FROM student_details WHERE user_id=" + userId);
            if (rsOda.next()) {
                int odaId = rsOda.getInt(1);
                if (odaId > 0) conn.createStatement().executeUpdate("UPDATE rooms SET mevcut_kisi = mevcut_kisi - 1, durum='MUSAIT' WHERE id=" + odaId);
            }
            conn.createStatement().executeUpdate("DELETE FROM permissions WHERE ogrenci_id=" + userId);
            conn.createStatement().executeUpdate("DELETE FROM student_details WHERE user_id=" + userId);
            conn.createStatement().executeUpdate("DELETE FROM users WHERE id=" + userId);
            JOptionPane.showMessageDialog(this, "Öğrenci silindi.");
            loadStudents(""); loadRooms();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage()); }
    }

    private void deleteRoom() {
        int row = tblRooms.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "Silinecek odayı seçin!"); return; }
        int mevcut = (int) modelRooms.getValueAt(row, 3);
        if (mevcut > 0) { JOptionPane.showMessageDialog(this, "İçinde öğrenci olan oda silinemez!", "Hata", JOptionPane.ERROR_MESSAGE); return; }
        int odaId = (int) modelRooms.getValueAt(row, 0);
        try {
            DatabaseConnection.getInstance().getConnection().createStatement().executeUpdate("DELETE FROM rooms WHERE id=" + odaId);
            JOptionPane.showMessageDialog(this, "Oda silindi.");
            loadRooms();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage()); }
    }

    private void deletePermission() {
        int row = tblRequests.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "Silinecek kaydı seçin!"); return; }
        int permId = (int) modelRequests.getValueAt(row, 0);
        try {
            DatabaseConnection.getInstance().getConnection().createStatement().executeUpdate("DELETE FROM permissions WHERE id=" + permId);
            JOptionPane.showMessageDialog(this, "Kayıt silindi.");
            loadPermissionRequests("BEKLEMEDE");
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage()); }
    }

    private void loadStudents(String searchText) {
        modelStudents.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT u.id, u.tc_no, u.ad, u.soyad, u.kullanici_adi, r.oda_no FROM users u LEFT JOIN student_details sd ON u.id = sd.user_id LEFT JOIN rooms r ON sd.oda_id = r.id WHERE u.rol = 'OGRENCI'";
            if (!searchText.isEmpty()) sql += " AND (lower(u.ad) LIKE ? OR lower(u.soyad) LIKE ? OR r.oda_no LIKE ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            if (!searchText.isEmpty()) { String p = "%" + searchText.toLowerCase() + "%"; ps.setString(1, p); ps.setString(2, p); ps.setString(3, p); }
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String oda = rs.getString("oda_no");
                modelStudents.addRow(new Object[]{rs.getInt("id"), rs.getString("tc_no"), rs.getString("ad"), rs.getString("soyad"), rs.getString("kullanici_adi"), (oda==null?"-":oda)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addBulkRooms() {
        try {
            int start = Integer.parseInt(txtOdaBaslangic.getText());
            int end = Integer.parseInt(txtOdaBitis.getText());
            int kap = Integer.parseInt(txtKapasite.getText());
            if (start > end) return;
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO rooms (oda_no, kapasite, mevcut_kisi, durum) VALUES (?, ?, 0, 'MUSAIT')");
            for (int i = start; i <= end; i++) { try { ps.setString(1, String.valueOf(i)); ps.setInt(2, kap); ps.executeUpdate(); } catch (SQLException ignored) {} }
            JOptionPane.showMessageDialog(this, "Odalar eklendi."); loadRooms();
        } catch (Exception e) {}
    }

    private void assignStudentToRoom() {
        String tc = txtOgrenciTc.getText(); String odaNo = txtAtanacakOda.getText();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement psOda = conn.prepareStatement("SELECT id, kapasite, mevcut_kisi FROM rooms WHERE oda_no = ?");
            psOda.setString(1, odaNo); ResultSet rsOda = psOda.executeQuery();
            if (!rsOda.next()) { JOptionPane.showMessageDialog(this, "Oda yok"); return; }
            int odaId = rsOda.getInt("id");
            if (rsOda.getInt("mevcut_kisi") >= rsOda.getInt("kapasite")) { JOptionPane.showMessageDialog(this, "Dolu"); return; }
            PreparedStatement psOgr = conn.prepareStatement("SELECT id FROM users WHERE tc_no = ?");
            psOgr.setString(1, tc); ResultSet rsOgr = psOgr.executeQuery();
            if (!rsOgr.next()) { JOptionPane.showMessageDialog(this, "Öğrenci yok"); return; }
            int userId = rsOgr.getInt("id");

            ResultSet rsCheck = conn.createStatement().executeQuery("SELECT oda_id FROM student_details WHERE user_id=" + userId);
            int eskiOdaId = -1;
            boolean kayitVar = false;
            if(rsCheck.next()) { kayitVar = true; eskiOdaId = rsCheck.getInt(1); }
            if(eskiOdaId == odaId) { JOptionPane.showMessageDialog(this, "Zaten burada!"); return; }

            if(eskiOdaId > 0) conn.createStatement().executeUpdate("UPDATE rooms SET mevcut_kisi = mevcut_kisi - 1, durum='MUSAIT' WHERE id=" + eskiOdaId);

            if(kayitVar) conn.createStatement().executeUpdate("UPDATE student_details SET oda_id=" + odaId + " WHERE user_id=" + userId);
            else conn.createStatement().executeUpdate("INSERT INTO student_details (user_id, oda_id) VALUES (" + userId + ", " + odaId + ")");

            conn.createStatement().executeUpdate("UPDATE rooms SET mevcut_kisi = mevcut_kisi + 1 WHERE id=" + odaId);

            PreparedStatement psCheckFull = conn.prepareStatement("SELECT kapasite, mevcut_kisi FROM rooms WHERE id = ?");
            psCheckFull.setInt(1, odaId);
            ResultSet rsFull = psCheckFull.executeQuery();
            if(rsFull.next() && rsFull.getInt("mevcut_kisi") >= rsFull.getInt("kapasite")) {
                conn.createStatement().executeUpdate("UPDATE rooms SET durum='DOLU' WHERE id=" + odaId);
            }
            JOptionPane.showMessageDialog(this, "Atandı"); loadRooms(); loadStudents("");
        } catch (Exception e) {}
    }
}