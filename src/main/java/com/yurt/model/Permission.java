package com.yurt.model;

import com.yurt.patterns.state.PermissionState;
import com.yurt.patterns.state.PendingState;
import com.yurt.patterns.state.ApprovedState;
import com.yurt.patterns.state.RejectedState;

public class Permission {
    private int id;
    private int ogrenciId;
    private String baslangic;
    private String bitis;
    private String sebep;

    // State deseni burada kullanılıyor
    private PermissionState state;

    public Permission(int ogrenciId, String baslangic, String bitis, String sebep) {
        this.ogrenciId = ogrenciId;
        this.baslangic = baslangic;
        this.bitis = bitis;
        this.sebep = sebep;
        // Varsayılan durum: Beklemede
        this.state = new PendingState();
    }

    // Durumu değiştirme metodu
    public void setState(PermissionState newState) {
        this.state = newState;
        // Yeni durumun gereğini yap (Ekrana yazdır)
        this.state.handle(this);
    }

    // Veritabanından okurken String gelen durumu State nesnesine çevirmek için yardımcı metot
    public void setDurumFromString(String durumText) {
        if (durumText.equals("ONAYLANDI")) {
            this.state = new ApprovedState();
        } else if (durumText.equals("REDDEDILDI")) {
            this.state = new RejectedState();
        } else {
            this.state = new PendingState();
        }
    }

    public String getDurumText() {
        return state.getStateName();
    }

    // Getter ve Setterlar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOgrenciId() { return ogrenciId; }

    public String getBaslangic() { return baslangic; }
    public String getBitis() { return bitis; }
    public String getSebep() { return sebep; }
}