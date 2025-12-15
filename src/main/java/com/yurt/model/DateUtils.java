package com.yurt.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // 1. Format Kontrolü (GG.AA.YYYY formatında mı?)
    public static boolean isValidFormat(String dateStr) {
        try {
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // 2. Mantıklı Tarih Kontrolü (2024-2026 arası mı?)
    public static boolean isReasonableDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, formatter);
            int year = date.getYear();
            // Sadece 2024, 2025 ve 2026 yıllarına izin verelim
            return year >= 2024 && year <= 2026;
        } catch (Exception e) {
            return false;
        }
    }

    // 3. Başlangıç < Bitiş Kontrolü
    public static boolean isStartBeforeEnd(String startStr, String endStr) {
        try {
            LocalDate start = LocalDate.parse(startStr, formatter);
            LocalDate end = LocalDate.parse(endStr, formatter);
            // Başlangıç tarihi, bitiş tarihinden önce veya eşit olmalı
            return !start.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }

    // 4. Geçmiş Tarih Kontrolü (Bugünden eski tarih girilemesin)
    public static boolean isFutureOrPresent(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, formatter);
            return !date.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}