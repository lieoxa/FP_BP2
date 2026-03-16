
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class App {

    private static final Scanner scanner = new Scanner(System.in);
    private static final MenuSearcher menuSearcher = new MenuSearcher();
    private static final DatabaseManager db = new DatabaseManager();

    public static void main(String[] args) {
        System.out.println("=== Warung Tegal Kasir ===");
        boolean running = true;
        while (running) {
            showMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 ->
                    KategoriManager.crudKategori(scanner);
                case 2 ->
                    MenuManager.crudMenu(scanner);
                case 3 ->
                    MemberManager.crudMembers(scanner);
                case 4 ->
                    transaksi();
                case 5 ->
                    lihatRiwayatTransaksi();
                case 6 -> {
                    System.out.println("Terima kasih!");
                    running = false;
                }
                default ->
                    System.out.println("Pilihan tidak valid. Coba lagi.");
            }
        }
        scanner.close();
    }

    private static void showMenu() {
        System.out.println("\n1. CRUD Kategori");
        System.out.println("2. CRUD Menu");
        System.out.println("3. CRUD Tingkat Member");
        System.out.println("4. Transaksi Pesanan");
        System.out.println("5. Riwayat Transaksi");
        System.out.println("6. Keluar");
        System.out.print("Pilih: ");
    }

    private static void transaksi() {
        // Show full menu list first
        List<Map<String, Object>> allMenus = menuSearcher.search("");
        menuSearcher.displaySearchResults(allMenus, "semua menu");

        Map<Integer, Integer> cart = new HashMap<>();
        Map<Integer, Integer> initialStocks = new HashMap<>();
        double subtotal = 0;
        while (true) {
            System.out.print("Cari menu (nama atau Enter untuk daftar semua lagi, 'selesai' untuk checkout): ");
            String query = scanner.nextLine().trim();
            if (query.equalsIgnoreCase("selesai")) {
                break;
            }

            List<Map<String, Object>> results = menuSearcher.search(query);
            menuSearcher.displaySearchResults(results, query);
            if (results.isEmpty()) {
                continue;
            }

            System.out.print("ID menu (0 skip): ");
            int id = scanner.nextInt();
            scanner.nextLine();
            if (id == 0) {
                continue;
            }

            System.out.print("Jumlah: ");
            int qty = scanner.nextInt();
            scanner.nextLine();

            Map<String, Object> menuItem = db.readMenuById(id);
            if (menuItem == null) {
                System.out.println("Menu tidak ditemukan.");
                continue;
            }

            int currentTotalQty = cart.getOrDefault(id, 0) + qty;
            if (!initialStocks.containsKey(id)) {
                initialStocks.put(id, (Integer) menuItem.get("stok"));
            }
            if (currentTotalQty > initialStocks.get(id)) {
                String stokMsg = initialStocks.get(id) == 0 ? "Habis" : String.valueOf(initialStocks.get(id));
                System.out.println("Total pesanan melebihi stok awal. Tersedia: " + stokMsg);
                continue;
            }
            int currStok = (Integer) menuItem.get("stok");
            if (currStok < qty) {
                String stokMsg = currStok == 0 ? "Habis" : String.valueOf(currStok);
                System.out.println("Stok tidak cukup. Tersedia: " + stokMsg);
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

            Map<Integer, Double> cartDetails = new HashMap<>(); // menuId -> harga_unit
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int mid = entry.getKey();
                int q = entry.getValue();
                Map<String, Object> m = db.readMenuById(mid);
                double harga = (Double) m.get("harga");
                double itemTotal = harga * q;
                cartDetails.put(mid, harga);
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
                            memberName, disc * 100, "Rp " + String.format("%,.0f", subtotal * disc));
                } else {
                    System.out.println("Tingkat member tidak ditemukan.");
                }
            }
            System.out.printf("TOTAL BAYAR: %52s%n", "Rp " + String.format("%,.0f", finalTotal));
            System.out.println("=========================================================");

            // Update stock FIRST
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                db.updateStok(entry.getKey(), entry.getValue());
            }

            // SAVE TRANSAKSI
            int pesananId = db.createPesanan(finalTotal, memberName);
            if (pesananId != -1) {
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    int mid = entry.getKey();
                    int q = entry.getValue();
                    double harga = cartDetails.get(mid);
                    double sub = harga * q;
                    db.addDetailPesanan(pesananId, mid, q, harga, sub);
                }
                System.out.println("✅ Transaksi disimpan dengan ID: " + pesananId);
            }

            // ⭐ BUKTI TRANSAKSI ⭐
            System.out.println("\n" + "═".repeat(50));
            System.out.println("                    BUKTI TRANSAKSI");
            System.out.println("                    WARUNG TEGAL MK");
            System.out.println("═".repeat(50));
            System.out.printf("No. Transaksi : #%04d%n", pesananId);
            System.out.printf("Tanggal       : %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.printf("Kasir         : %s%n", System.getProperty("user.name").toUpperCase());
            System.out.println("═".repeat(50));
            System.out.printf("%-25s %5s %10s %12s%n", "Nama Menu", "Qty", "@Harga", "Total");
            System.out.println("-".repeat(50));

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int mid = entry.getKey();
                int q = entry.getValue();
                Map<String, Object> m = db.readMenuById(mid);
                double harga = (Double) m.get("harga");
                double itemTotal = harga * q;
                System.out.printf("%-25s %5d %10s %,12.0f%n",
                        m.get("nama"), q, "Rp" + String.format("%,.0f", harga), itemTotal);
            }

            System.out.println("═".repeat(50));
            System.out.printf("%42s: Rp%,8.0f%n", "SUBTOTAL", subtotal);
            if (disc > 0) {
                System.out.printf("%42s: -Rp%,8.0f%n", memberName, subtotal * disc);
            }
            System.out.printf("%42s: Rp%,8.0f%n", "TOTAL BAYAR", finalTotal);
            System.out.println("═".repeat(50));
            System.out.println("Terima kasih telah berbelanja!");
            System.out.println("       Barang tidak dapat ditukar");
            System.out.println("       atau dikembalikan.");
            System.out.println("       Simpan No. Transaksi untuk bukti.");
            System.out.println("═".repeat(50));
        } else {
            System.out.println("Keranjang kosong.");
        }
    }

    private static void lihatRiwayatTransaksi() {
        System.out.println("\n=== RIWAYAT TRANSAKSI TERAKHIR ===");
        List<Map<String, Object>> riwayat = db.riwayatPesanan();
        if (riwayat.isEmpty()) {
            System.out.println("Belum ada transaksi.");
            return;
        }

        System.out.printf("%-5s %-19s %15s %10s%n", "ID", "Tanggal", "Total", "Member");
        System.out.println("-".repeat(55));

        for (Map<String, Object> r : riwayat) {
            List<Map<String, Object>> detailsCount = db.detailPesanan((Integer) r.get("id"));
            System.out.printf("%-5d %-19s Rp%,12.0f %-10s %d item%n",
                    r.get("id"),
                    ((java.sql.Timestamp) r.get("wkt")).toString().substring(0, 16),
                    r.get("total"),
                    r.get("member"),
                    detailsCount.size());
        }

        System.out.print("\nLihat detail ID (0 batal): ");
        int id = scanner.nextInt();
        scanner.nextLine();
        if (id == 0) {
            return;
        }

        List<Map<String, Object>> details = db.detailPesanan(id);
        if (details.isEmpty()) {
            System.out.println("Transaksi tidak ditemukan.");
            return;
        }

        Map<String, Object> pesanan = riwayat.stream().filter(p -> ((Integer) p.get("id")) == id).findFirst().orElse(null);
        System.out.println("\n=== DETAIL TRANSAKSI #" + id + " ===");
        System.out.printf("Tanggal: %s%n", ((java.sql.Timestamp) pesanan.get("wkt")).toString());
        System.out.printf("Member: %s%n", pesanan.get("member"));
        System.out.printf("TOTAL: Rp %, .0f%n", pesanan.get("total"));
        System.out.println("\nItem:");
        System.out.printf("%-25s %5s %10s %12s%n", "Menu", "Qty", "@Harga", "Subtotal");
        System.out.println("-".repeat(55));

        for (Map<String, Object> d : details) {
            System.out.printf("%-25s %5d Rp%,10.0f Rp%,12.0f%n",
                    d.get("menu_nama"),
                    d.get("qty"),
                    d.get("harga_unit"),
                    d.get("subtotal"));
        }
        System.out.println("-".repeat(55));
    }
}
