package com.yurt.patterns.state;

import com.yurt.model.Permission;

public interface PermissionState {
    // Duruma göre yapılacak işlemi belirtir
    void handle(Permission permission);

    // Durumun adını metin olarak döndürür (Veritabanına yazmak için)
    String getStateName();
}