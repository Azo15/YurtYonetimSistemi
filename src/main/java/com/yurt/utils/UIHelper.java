package com.yurt.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIHelper {

    // --- RENK PALETİ ---
    public static final Color PRIMARY_COLOR = new Color(66, 133, 244); // Google Blue
    public static final Color ACCENT_COLOR = new Color(255, 110, 64); // Orange Red
    public static final Color BG_LIGHT = new Color(245, 247, 250);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(50, 50, 50);

    // --- GRADIENT PANEL ---
    public static JPanel createGradientPanel(Color color1, Color color2) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                // Diyagonal Gradient
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
    }

    // --- MODERN KART PANELİ (GÖLGELİ GİBİ) ---
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        // FlatLaf gölgesi ve yuvarlaklığı
        panel.putClientProperty("FlatLaf.style", "arc: 20; border: 1,1,1,1, #E0E0E0, 1, 10");
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    // --- KART BAŞLIĞI ---
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(TEXT_DARK);
        return label;
    }

    // --- HOVER EFEKTLİ BUTON ---
    public static JButton createModernButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(baseColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Yuvarlak Köşeler
        btn.putClientProperty("FlatLaf.style", "arc: 15");

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(baseColor.darker());
            }

            public void mouseExited(MouseEvent evt) {
                btn.setBackground(baseColor);
            }
        });
        return btn;
    }

    // --- TABLO GÜZELLEŞTİRİCİ ---
    public static void decorateTable(JTable table) {
        // Genel Ayarlar
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 240, 254)); // Soft Blue Selection
        table.setSelectionForeground(TEXT_DARK);

        // Header Ayarları
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(100, 100, 100)); // Dark Gray Header Text
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230))); // Bottom Line

        // FlatLaf özel ayarları
        table.putClientProperty("FlatLaf.style", "selectionInactiveBackground: #F0F0F0");
    }
}
