package com.yurt.patterns.state;

import com.yurt.model.Permission;

public class ApprovedState implements PermissionState {
    @Override
    public void handle(Permission permission) {
        System.out.println("İzin ONAYLANDI! İyi yolculuklar.");
        // Burada Observer devreye girecek ve öğrenciye bildirim gidecek
    }

    @Override
    public String getStateName() {
        return "ONAYLANDI";
    }
}