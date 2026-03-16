import java.util.*;
import java.util.Scanner;

public class MemberManager {
    private static DatabaseManager db = new DatabaseManager();
    private static Scanner scanner;

    public static void crudMembers(Scanner sc) {
        scanner = sc;
        boolean back = false;
        while (!back) {
            System.out.println("\n--- CRUD Tingkat Member ---");
            System.out.println("1. Tambah Tingkat");
            System.out.println("2. Lihat Semua");
            System.out.println("3. Ubah");
            System.out.println("4. Hapus");
            System.out.println("5. Kembali");
            System.out.print("Pilih: ");
            int sub = scanner.nextInt();
            scanner.nextLine();

            switch (sub) {
                case 1 -> addTingkat();
                case 2 -> listTingkat();
                case 3 -> updateTingkat();
                case 4 -> deleteTingkat();
                case 5 -> back = true;
                default -> System.out.println("Tidak valid.");
            }
        }
    }

    private static void addTingkat() {
        System.out.print("Nama tingkat (e.g. Silver): ");
        String nama = scanner.nextLine();
        System.out.print("Diskon (e.g. 0.10): ");
        double disc = scanner.nextDouble();
        scanner.nextLine();
        db.createMember(nama, disc);
    }

    private static void listTingkat() {
        List<Map<String, Object>> members = db.readAllMembers();
        if (members.isEmpty()) {
            System.out.println("Belum ada tingkat member.");
            return;
        }
        System.out.println("\n============ TINGKAT MEMBER ============");
        System.out.printf("%-5s %-20s %-10s%n", "ID", "Tingkat", "Diskon");
        System.out.println("--------------------------------------");

        for (Map<String, Object> m : members) {
            System.out.printf("%-5d %-20s %.0f%%%n",
                    m.get("id"),
                    m.get("nama"),
                    ((Double) m.get("discount")) * 100);
        }
        System.out.println("======================================");
    }

    private static void updateTingkat() {
        System.out.print("ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Nama tingkat baru: ");
        String nama = scanner.nextLine();
        System.out.print("Diskon baru (e.g. 0.10): ");
        double disc = scanner.nextDouble();
        scanner.nextLine();
        db.updateMember(id, nama, disc);
    }

    private static void deleteTingkat() {
        System.out.print("ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        db.deleteMember(id);
    }

    public static double getDiscountById(int id) {
        Map<String, Object> member = db.readMemberById(id);
        return member != null ? (Double) member.get("discount") : 0.0;
    }

    public static Map<String, Object> getMemberById(int id) {
        return db.readMemberById(id);
    }
}

