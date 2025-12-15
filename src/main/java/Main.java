import com.yurt.database.DatabaseConnection;
import com.yurt.view.LoginView;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Uygulama Başlatılıyor...
        System.out.println("Yurt Otomasyon Sistemi Başlatılıyor...");

        // 1. Veritabanı bağlantısını başlat (Singleton Deseni)
        DatabaseConnection.getInstance();

        // 2. Arayüzü Başlat
        SwingUtilities.invokeLater(() -> {
            new LoginView();
        });
    }
}