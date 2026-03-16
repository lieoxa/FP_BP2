import java.util.*;
import java.util.Scanner;

public class App {
    private static Scanner scanner = new Scanner(System.in);
    private static MenuSearcher menuSearcher = new MenuSearcher();
    private static DatabaseManager db = new DatabaseManager();
    private static MemberManager memberMgr = new MemberManager();

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
                case 3 -> MemberManager.crudMembers(scanner);
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
        System.out.println("3. CRUD Tingkat Member");
        System.out.println("4. Transaksi Pesanan");
        System.out.println("5. Keluar");
        System.out.print("Pilih: ");
    }

    private static void transaksi() {
        // Show full menu list first
        List<Map<String, Object>> allMenus = menuSearcher.search("");
        menuSearcher.displaySearchResults(allMenus, "semua menu");

        Map<Integer, Integer> cart = new HashMap<>();
        double subtotal = 0;
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
            subtotal += ((Double) menuItem.get("harga")) * qty;
            System.out.printf("Subtotal sementara: Rp %,.0f%n", subtotal);
        }

        if (!cart.isEmpty()) {
            double finalTotal = subtotal;
            String memberName = "Non-Member";

            // Keranjang belanja table
            System.out.println("\n================== KERANJANG BELANJA ==================");
            System.out.printf("%-25s %5s %10s %12s%n", "Nama Menu", "Qty", "Harga", "Total");
            System.out.println("--------------------------------------------------------");

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int mid = entry.getKey();
                int q = entry.getValue();
                Map<String, Object> m = db.readMenuById(mid);
                double harga = (Double) m.get("harga");
                double itemTotal = harga * q;
                System.out.printf("%-25s %5d Rp%,10.0f Rp%,12.0f%n", 
                    m.get("nama"), q, harga, itemTotal);
            }

            System.out.println("=========================================================");
            System.out.printf("SUBTOTAL: %52s%n", "Rp " + String.format("%,.0f", subtotal));

            // Tiered member discount from DB
            System.out.println("\n--- Pilih Tingkat Member ---");
            MemberManager.listTingkat();
            System.out.print("Tingkat member (ID/nama, Enter untuk non-member): ");
            String memberInput = scanner.nextLine().trim();
            double disc = 0;
            if (!memberInput.isEmpty()) {
                Map<String, Object> member = MemberManager.findMember(memberInput);
                if (member != null) {
                    disc = (Double) member.get("discount");
                    memberName = (String) member.get("nama");
                    finalTotal = subtotal * (1 - disc);
                    System.out.printf("Diskon %s (%.0f%%): %50s%n", 
                        memberName, disc*100, "Rp " + String.format("%,.0f", subtotal * disc));
                } else {
                    System.out.println("Tingkat member tidak ditemukan.");
                }
            }
            System.out.printf("TOTAL BAYAR: %52s%n", "Rp " + String.format("%,.0f", finalTotal));
            System.out.println("=========================================================");

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

