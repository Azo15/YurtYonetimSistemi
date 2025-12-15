package com.yurt.patterns.state;

import com.yurt.model.Permission;

public class PendingState implements PermissionState {
    @Override
    public void handle(Permission permission) {
        System.out.println("İzin talebi alındı, personel onayı bekleniyor.");
    }

    @Override
    public String getStateName() {
        return "BEKLEMEDE";
    }
}