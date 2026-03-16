import java.util.*;
import java.util.Scanner;

public class KategoriManager {
    private static DatabaseManager db = new DatabaseManager();

    public static void crudKategori(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- CRUD Kategori ---");
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
                    System.out.print("Nama kategori: ");
                    String nama = scanner.nextLine();
                    db.createKategori(nama);
                }
                case 2 -> {
                    List<Map<String, Object>> kats = db.readAllKategori();
                    if (kats.isEmpty()) {
                        System.out.println("Belum ada kategori.");
                        break;
                    }
                    System.out.println("\n=========== DAFTAR KATEGORI ===========");
                    System.out.printf("%-5s %-20s%n", "ID", "Nama Kategori");
                    System.out.println("---------------------------------------");

                    for (Map<String, Object> kat : kats) {
                        System.out.printf("%-5d %-20s%n",
                                kat.get("id"),
                                kat.get("nama"));
                    }
                    System.out.println("=======================================");
                }
                case 3 -> {
                    System.out.print("ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nama baru: ");
                    String nama = scanner.nextLine();
                    db.updateKategori(id, nama);
                }
                case 4 -> {
                    System.out.print("ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    db.deleteKategori(id);
                }
                case 5 -> back = true;
                default -> System.out.println("Tidak valid.");
            }
        }
    }
}

