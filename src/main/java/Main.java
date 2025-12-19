import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.yurt.database.DatabaseConnection;
import com.yurt.view.LoginView;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // İsteğe bağlı: Menü çubuğunu pencere içine göm (Windows 10/11'de bütünleşik
        // header hissi verir)
        System.setProperty("flatlaf.useWindowDecorations", "true");
        System.setProperty("flatlaf.menuBarEmbedded", "true");

        // Tema Kurulumu
        try {
            FlatMacLightLaf.setup();
            UIManager.put("Button.arc", 10); // Yuvarlatılmış butonlar
            UIManager.put("Component.arc", 10); // Yuvarlatılmış inputlar
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception e) {
            System.err.println("Tema yüklenemedi!");
        }

        // Uygulama Başlatılıyor...
        System.out.println("Yurt Otomasyon Sistemi Başlatılıyor...");

        // 1. Veritabanı bağlantısını başlat (Singleton Deseni)
        // Varsayılan: SQLite
        DatabaseConnection db = DatabaseConnection.getInstance();

        // ⚠️ MYSQL'E GEÇMEK İÇİN AŞAĞIDAKİ YORUMU AÇ:
        db.setBridge(new com.yurt.patterns.bridge.MySQLBridge());

        // 2. Arayüzü Başlat
        SwingUtilities.invokeLater(() -> {
            new LoginView();
        });
    }
}