import java.util.*;
import java.util.Scanner;

public class MenuManager {
    private static DatabaseManager db = new DatabaseManager();

    public static void crudMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- CRUD Menu ---");
            System.out.println("1. Tambah");
            System.out.println("2. Lihat Semua");
            System.out.println("3. Ubah");
            System.out.println("4. Hapus");
            System.out.println("5. Kembali");
            System.out.print("Pilih: ");
            int sub = scanner.nextInt();
            scanner.nextLine();

            switch (sub) {
case 1 -> {
                    System.out.print("Nama menu: ");
                    String nama = scanner.nextLine();
                    
                    // AUTO SELECT KATEGORI
                    List<Map<String, Object>> kategories = db.readAllKategori();
                    if (kategories.isEmpty()) {
                        System.out.println("❌ Tambah kategori dulu!");
                        break;
                    }
                    System.out.println("\n--- Pilih Kategori ---");
                    for (int i = 0; i < kategories.size(); i++) {
                        Map<String, Object> kat = kategories.get(i);
                        System.out.printf("%d. %s (ID: %d)%n", i+1, kat.get("nama"), kat.get("id"));
                    }
                    System.out.print("Nomor kategori: ");
                    int katIndex = scanner.nextInt() - 1;
                    scanner.nextLine();
                    if (katIndex < 0 || katIndex >= kategories.size()) {
                        System.out.println("❌ Pilihan tidak valid!");
                        break;
                    }
                    int katId = (Integer) kategories.get(katIndex).get("id");
                    
                    System.out.print("Harga: ");
                    double harga = scanner.nextDouble();
                    System.out.print("Stok: ");
                    int stok = scanner.nextInt();
                    scanner.nextLine();
                    db.createMenu(nama, katId, harga, stok);
                }

                case 2 -> {
                    List<Map<String, Object>> menus = db.readAllMenu();
                    if (menus.isEmpty()) {
                        System.out.println("Belum ada menu.");
                        break;
                    }
                    System.out.println("\n==================== DAFTAR MENU ====================");
                    System.out.printf("%-5s %-20s %-15s %-12s %-5s%n",
                            "ID", "Nama Menu", "Kategori", "Harga", "Stok");
                    System.out.println("-----------------------------------------------------");

                    for (Map<String, Object> m : menus) {
                        System.out.printf("%-5d %-20s %-15s Rp %,10.0f %-5d%n",
                                m.get("id"),
                                m.get("nama"),
                                m.get("kat_nama"),
                                m.get("harga"),
                                m.get("stok"));
                    }
                    System.out.println("=====================================================");
                }
                case 3 -> {
                    System.out.print("ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nama baru: ");
                    String nama = scanner.nextLine();
                    System.out.print("ID Kategori baru: ");
                    int katId = scanner.nextInt();
                    System.out.print("Harga baru: ");
                    double harga = scanner.nextDouble();
                    System.out.print("Stok baru: ");
                    int stok = scanner.nextInt();
                    scanner.nextLine();
                    db.updateMenu(id, nama, katId, harga, stok);
                }
                case 4 -> {
                    System.out.print("ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    db.deleteMenu(id);
                }
                case 5 -> back = true;
                default -> System.out.println("Tidak valid.");
            }
        }
    }
}

