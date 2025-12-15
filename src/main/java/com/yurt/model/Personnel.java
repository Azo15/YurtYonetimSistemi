package com.yurt.model;

public class Personnel extends User {

    public Personnel() {
        this.rol = "PERSONEL";
    }

    @Override
    public void showRoleInfo() {
        System.out.println("Ben bir PERSONELİM. Oda ekleyebilir ve izin onaylayabilirim.");
    }
}