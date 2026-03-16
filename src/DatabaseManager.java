
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/crud_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "";

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement()) {
// Clean tables
                stmt.execute("DROP TABLE IF EXISTS detail_pesanan");
                stmt.execute("DROP TABLE IF EXISTS pesanan");
                stmt.execute("DROP TABLE IF EXISTS menu");
                stmt.execute("DROP TABLE IF EXISTS kategori");
                stmt.execute("DROP TABLE IF EXISTS member");

                // Create tables
                stmt.execute("CREATE TABLE kategori (id INT AUTO_INCREMENT PRIMARY KEY, nama VARCHAR(100) NOT NULL)");
                stmt.execute("CREATE TABLE menu (id INT AUTO_INCREMENT PRIMARY KEY, nama VARCHAR(100) NOT NULL, kategori_id INT, harga DECIMAL(10,2) NOT NULL, stok INT DEFAULT 0, FOREIGN KEY (kategori_id) REFERENCES kategori(id))");
                stmt.execute("CREATE TABLE member (id INT AUTO_INCREMENT PRIMARY KEY, nama VARCHAR(100) NOT NULL, discount DECIMAL(5,4) DEFAULT 0.10)");
                stmt.execute("CREATE TABLE pesanan (id INT AUTO_INCREMENT PRIMARY KEY, wkt DATETIME DEFAULT CURRENT_TIMESTAMP, total DECIMAL(10,2), member VARCHAR(50))");
                stmt.execute("CREATE TABLE detail_pesanan (id INT AUTO_INCREMENT PRIMARY KEY, pesanan_id INT, menu_id INT, qty INT, harga_unit DECIMAL(10,2), subtotal DECIMAL(10,2), FOREIGN KEY (pesanan_id) REFERENCES pesanan(id), FOREIGN KEY (menu_id) REFERENCES menu(id))");

                // SAMPLE DATA - FIX FK ERROR
                stmt.execute("INSERT INTO kategori (nama) VALUES ('Makanan')");
                stmt.execute("INSERT INTO kategori (nama) VALUES ('Minuman')");
                stmt.execute("INSERT INTO kategori (nama) VALUES ('Cemilan')");
                stmt.execute("INSERT INTO menu (nama, kategori_id, harga, stok) VALUES ('Nasi Goreng', 1, 15000, 50)");
                stmt.execute("INSERT INTO menu (nama, kategori_id, harga, stok) VALUES ('Mie Goreng', 1, 12000, 40)");
                stmt.execute("INSERT INTO menu (nama, kategori_id, harga, stok) VALUES ('Es Teh', 2, 5000, 100)");
                stmt.execute("INSERT INTO member (nama, discount) VALUES ('Silver', 0.05)");
                stmt.execute("INSERT INTO member (nama, discount) VALUES ('Gold', 0.10)");
                stmt.execute("INSERT INTO member (nama, discount) VALUES ('Platinum', 0.15)");

                // System.out.println("✅ Database + sample data ready!");
            }
        } catch (Exception e) {
            System.err.println("DB Error: " + e.getMessage());
        }
    }

    // KATEGORI CRUD
    public void createKategori(String nama) {
        String sql = "INSERT INTO kategori(nama) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.executeUpdate();
            System.out.println("✅ Kategori '" + nama + "' dibuat");
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> readAllKategori() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM kategori";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("nama", rs.getString("nama"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Read kategori error: " + e.getMessage());
        }
        return list;
    }

    public void updateKategori(int id, String nama) {
        String sql = "UPDATE kategori SET nama = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Kategori updated");
            }
        } catch (SQLException e) {
            System.err.println("Update kategori error: " + e.getMessage());
        }
    }

    public void deleteKategori(int id) {
        String sql = "DELETE FROM kategori WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Kategori deleted");
            }
        } catch (SQLException e) {
            System.err.println("Delete kategori error: " + e.getMessage());
        }
    }

    // MENU CRUD + FK VALIDATION
    public void createMenu(String nama, int kategoriId, double harga, int stok) {
        if (!kategoriExists(kategoriId)) {
            System.err.println("❌ Kategori ID " + kategoriId + " tidak ada!");
            return;
        }
        String sql = "INSERT INTO menu(nama, kategori_id, harga, stok) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setInt(2, kategoriId);
            pstmt.setDouble(3, harga);
            pstmt.setInt(4, stok);
            pstmt.executeUpdate();
            System.out.println("✅ Menu '" + nama + "' dibuat");
        } catch (SQLException e) {
            System.err.println("❌ Menu error: " + e.getMessage());
        }
    }

    private boolean kategoriExists(int id) {
        String sql = "SELECT 1 FROM kategori WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Map<String, Object>> readAllMenu() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT m.*, k.nama as kat_nama FROM menu m LEFT JOIN kategori k ON m.kategori_id = k.id ORDER BY m.id";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("nama", rs.getString("nama"));
                row.put("kat_nama", rs.getString("kat_nama"));
                row.put("harga", rs.getDouble("harga"));
                row.put("stok", rs.getInt("stok"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Read menu error: " + e.getMessage());
        }
        return list;
    }

    public Map<String, Object> readMenuById(int id) {
        Map<String, Object> menu = null;
        String sql = "SELECT m.*, k.nama as kat_nama FROM menu m LEFT JOIN kategori k ON m.kategori_id = k.id WHERE m.id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    menu = new HashMap<>();
                    menu.put("id", rs.getInt("id"));
                    menu.put("nama", rs.getString("nama"));
                    menu.put("kat_nama", rs.getString("kat_nama"));
                    menu.put("harga", rs.getDouble("harga"));
                    menu.put("stok", rs.getInt("stok"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Menu ID error: " + e.getMessage());
        }
        return menu;
    }

    public void updateMenu(int id, String nama, int kategoriId, double harga, int stok) {
        if (!kategoriExists(kategoriId)) {
            System.err.println("❌ Kategori ID " + kategoriId + " tidak ada!");
            return;
        }
        String sql = "UPDATE menu SET nama = ?, kategori_id = ?, harga = ?, stok = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setInt(2, kategoriId);
            pstmt.setDouble(3, harga);
            pstmt.setInt(4, stok);
            pstmt.setInt(5, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Menu updated");
            }
        } catch (SQLException e) {
            System.err.println("Update menu error: " + e.getMessage());
        }
    }

    public void deleteMenu(int id) {
        String sql = "DELETE FROM menu WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Menu deleted");
            }
        } catch (SQLException e) {
            System.err.println("Delete menu error: " + e.getMessage());
        }
    }

    public void updateStok(int id, int qty) {
        String sql = "UPDATE menu SET stok = stok - ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, qty);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update stok error: " + e.getMessage());
        }
    }

    // MEMBER CRUD
    public void createMember(String nama, double discount) {
        String sql = "INSERT INTO member(nama, discount) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setDouble(2, discount);
            pstmt.executeUpdate();
            System.out.println("✅ Member '" + nama + "' dibuat");
        } catch (SQLException e) {
            System.err.println("Create member error: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> readAllMembers() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY id";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("nama", rs.getString("nama"));
                row.put("discount", rs.getDouble("discount"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Read members error: " + e.getMessage());
        }
        return list;
    }

    public void updateMember(int id, String nama, double discount) {
        String sql = "UPDATE member SET nama = ?, discount = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setDouble(2, discount);
            pstmt.setInt(3, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Member updated");
            }
        } catch (SQLException e) {
            System.err.println("Update member error: " + e.getMessage());
        }
    }

    public void deleteMember(int id) {
        String sql = "DELETE FROM member WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Member deleted");
            }
        } catch (SQLException e) {
            System.err.println("Delete member error: " + e.getMessage());
        }
    }

// TRANSAKSI RIWAYAT
    public int createPesanan(double total, String member) {
        String sql = "INSERT INTO pesanan (total, member) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, total);
            pstmt.setString(2, member);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Create pesanan error: " + e.getMessage());
        }
        return -1;
    }

    public void addDetailPesanan(int pesananId, int menuId, int qty, double hargaUnit, double subtotal) {
        String sql = "INSERT INTO detail_pesanan (pesanan_id, menu_id, qty, harga_unit, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pesananId);
            pstmt.setInt(2, menuId);
            pstmt.setInt(3, qty);
            pstmt.setDouble(4, hargaUnit);
            pstmt.setDouble(5, subtotal);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Add detail error: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> riwayatPesanan() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM pesanan ORDER BY id DESC LIMIT 20";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("wkt", rs.getTimestamp("wkt"));
                row.put("total", rs.getDouble("total"));
                row.put("member", rs.getString("member"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Riwayat error: " + e.getMessage());
        }
        return list;
    }

    public List<Map<String, Object>> detailPesanan(int id) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT d.*, m.nama as menu_nama FROM detail_pesanan d LEFT JOIN menu m ON d.menu_id = m.id WHERE d.pesanan_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("qty", rs.getInt("qty"));
                    row.put("harga_unit", rs.getDouble("harga_unit"));
                    row.put("subtotal", rs.getDouble("subtotal"));
                    row.put("menu_nama", rs.getString("menu_nama"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Detail error: " + e.getMessage());
        }
        return list;
    }

    // SEARCH MENU
    public List<Map<String, Object>> searchMenus(String query) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT m.*, k.nama as kat_nama FROM menu m LEFT JOIN kategori k ON m.kategori_id = k.id WHERE m.nama LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("nama", rs.getString("nama"));
                    row.put("kat_nama", rs.getString("kat_nama"));
                    row.put("harga", rs.getDouble("harga"));
                    row.put("stok", rs.getInt("stok"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return list;
    }

    public static Map<String, Object> readMemberByIdStatic(int id) {
        return new DatabaseManager().readMemberById(id);
    }

    public static Map<String, Object> readMemberByNameStatic(String name) {
        return new DatabaseManager().readMemberByName(name);
    }

    public Map<String, Object> readMemberById(int id) {
        Map<String, Object> member = null;
        String sql = "SELECT * FROM member WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    member = new HashMap<>();
                    member.put("id", rs.getInt("id"));
                    member.put("nama", rs.getString("nama"));
                    member.put("discount", rs.getDouble("discount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Member ID " + id + " error: " + e.getMessage());
        }
        return member;
    }

    public Map<String, Object> readMemberByName(String name) {
        Map<String, Object> member = null;
        String sql = "SELECT * FROM member WHERE nama LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    member = new HashMap<>();
                    member.put("id", rs.getInt("id"));
                    member.put("nama", rs.getString("nama"));
                    member.put("discount", rs.getDouble("discount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Member name error: " + e.getMessage());
        }
        return member;
    }
}
