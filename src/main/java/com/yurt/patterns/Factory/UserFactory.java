package com.yurt.patterns.Factory;

import com.yurt.model.User;
import com.yurt.model.Student;
import com.yurt.model.Personnel;

public class UserFactory {

    // Bu metot, kendisine verilen tipe göre doğru nesneyi üretip döndürür.
    public static User createUser(String type) {
        if (type.equalsIgnoreCase("OGRENCI")) {
            return new Student();
        } else if (type.equalsIgnoreCase("PERSONEL")) {
            return new Personnel();
        } else {
            return null; // Tanımsız tip
        }
    }
}