import java.util.*;
import java.util.Scanner;

public class App {
    private static Scanner scanner = new Scanner(System.in);
    private static MenuSearcher menuSearcher = new MenuSearcher();
    private static DatabaseManager db = new DatabaseManager();

    public static void main(String[] args) {
        System.out.println("=== Warung Tegal Kasir ===");
        boolean running = true;
        while (running) {
            showMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
case 1 -> KategoriManager.crudKategori(scanner);
                case 2 -> MenuManager.crudMenu(scanner);
                case 3 -> System.out.println("Diskon member otomatis 10% di transaksi (sistem, no DB needed).");
                case 4 -> transaksi();
                case 5 -> {
                    System.out.println("Terima kasih!");
                    running = false;
                }
                default -> System.out.println("Pilihan tidak valid. Coba lagi.");
            }
        }
        scanner.close();
    }

    private static void showMenu() {
        System.out.println("\n1. CRUD Kategori");
        System.out.println("2. CRUD Menu");
        System.out.println("3. Info Diskon");
        System.out.println("4. Transaksi Pesanan");
        System.out.println("5. Keluar");
        System.out.print("Pilih: ");
    }

    private static void transaksi() {
        // Show full menu list first
        List<Map<String, Object>> allMenus = menuSearcher.search("");
        menuSearcher.displaySearchResults(allMenus, "semua menu");

        Map<Integer, Integer> cart = new HashMap<>();
        double total = 0;
        while (true) {
            System.out.print("Cari menu (nama atau Enter untuk daftar semua lagi, 'selesai' untuk checkout): ");
            String query = scanner.nextLine().trim();
            if (query.equalsIgnoreCase("selesai")) break;

            List<Map<String, Object>> results = menuSearcher.search(query);
            menuSearcher.displaySearchResults(results, query);
            if (results.isEmpty()) continue;

            System.out.print("ID menu (0 skip): ");
            int id = scanner.nextInt();
            scanner.nextLine();
            if (id == 0) continue;

            System.out.print("Jumlah: ");
            int qty = scanner.nextInt();
            scanner.nextLine();

            Map<String, Object> menuItem = db.readMenuById(id);
            if (menuItem == null) {
                System.out.println("Menu tidak ditemukan.");
                continue;
            }
            if ((Integer) menuItem.get("stok") < qty) {
                System.out.println("Stok tidak cukup. Tersedia: " + menuItem.get("stok"));
                continue;
            }
            cart.put(id, cart.getOrDefault(id, 0) + qty);
            total += ((Double) menuItem.get("harga")) * qty;
            System.out.printf("Total sementara: Rp %,.0f%n", total);
        }

        if (!cart.isEmpty()) {
            System.out.println("\n--- Keranjang Belanja ---");
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int mid = entry.getKey();
                int q = entry.getValue();
                Map<String, Object> m = db.readMenuById(mid);
                System.out.printf("%s x %d = Rp %,.0f%n", m.get("nama"), q, ((Double) m.get("harga")) * q);
            }
            System.out.printf("Subtotal: Rp %,.0f%n", total);

            // Fixed 10% discount system if member (no DB)
            System.out.print("Apakah pembeli member? (y/n): ");
            String isMember = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(isMember)) {
                double disc = 0.10;
                double oldTotal = total;
                total *= (1 - disc);
                System.out.printf("Diskon member 10%%: Rp %,.0f (dari Rp %,.0f)%n", total, oldTotal);
            }
            System.out.printf("TOTAL AKHIR: Rp %,.0f%n", total);

            // Update stock
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                db.updateStok(entry.getKey(), entry.getValue());
            }
            System.out.println("Terima kasih! Stok otomatis berkurang.");
        } else {
            System.out.println("Keranjang kosong.");
        }
    }
}

