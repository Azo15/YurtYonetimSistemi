package com.yurt.view;

import javax.swing.border.EmptyBorder;
import javax.swing.*;
import java.awt.*;

// İKİNCİ ABSTRACT CLASS
// Tüm ekranlar bu sınıftan türeyecek.
public abstract class BasePage extends JFrame {

    public BasePage(String title, int width, int height) {
        setTitle(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Modern Ayarlar
        setLayout(new BorderLayout());
        // Kenarlardan 10px boşluk bırak
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        setLocationRelativeTo(null); // Ekranın tam ortasında açılsın
    }

    // Her sayfa kendi bileşenlerini (buton, text vs.) bu metodun içinde oluşturmak
    // zorunda.
    public abstract void initializeComponents();
}