package com.yurt.patterns.state;

import com.yurt.model.Permission;

public class RejectedState implements PermissionState {
    @Override
    public void handle(Permission permission) {
        System.out.println("İzin REDDEDİLDİ! Yurt yönetimiyle görüşünüz.");
    }

    @Override
    public String getStateName() {
        return "REDDEDILDI";
    }
}