import java.util.*;

public class MenuSearcher {
    private DatabaseManager db = new DatabaseManager();

    public List<Map<String, Object>> search(String query) {
        if (query.trim().isEmpty()) {
            return db.readAllMenu();
        }
        return db.searchMenus(query);
    }

    public void displaySearchResults(List<Map<String, Object>> results, String title) {
        if (results.isEmpty()) {
            System.out.println("Tidak ada hasil untuk '" + title + "'");
            return;
        }
        System.out.println("\n--- Hasil Pencarian '" + title + "' ---");
        System.out.printf("%-5s %-20s %-15s %-12s %-5s%n",
                "ID", "Nama Menu", "Kategori", "Harga", "Stok");
        System.out.println("-----------------------------------------------------");

        for (Map<String, Object> m : results) {
            System.out.printf("%-5d %-20s %-15s Rp %,10.0f %-5d%n",
                    m.get("id"),
                    m.get("nama"),
                    m.get("kat_nama"),
                    m.get("harga"),
                    m.get("stok"));
        }
        System.out.println("-----------------------------------------------------");
    }
}

