package com.yurt.view;

import javax.swing.*;
import java.awt.*;

// İKİNCİ ABSTRACT CLASS (Zorunlu Şartı Sağlıyoruz)
// Tüm ekranlar bu sınıftan türeyecek.
public abstract class BasePage extends JFrame {

    public BasePage(String title, int width, int height) {
        setTitle(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Çarpıya basınca program kapansın
        setLayout(null); // Elemanları koordinatla (x,y) yerleştireceğiz
        setLocationRelativeTo(null); // Ekranın tam ortasında açılsın
    }

    // Her sayfa kendi bileşenlerini (buton, text vs.) bu metodun içinde oluşturmak zorunda.
    public abstract void initializeComponents();
}