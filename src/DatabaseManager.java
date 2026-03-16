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
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                    Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS students;");
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS kategori (id INT AUTO_INCREMENT PRIMARY KEY, nama VARCHAR(100) NOT NULL)");
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS menu (id INT AUTO_INCREMENT PRIMARY KEY, nama VARCHAR(100) NOT NULL, kategori_id INT, harga DECIMAL(10,2) NOT NULL, stok INT DEFAULT 0, FOREIGN KEY (kategori_id) REFERENCES kategori(id))");
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS member (id INT AUTO_INCREMENT PRIMARY KEY, nama VARCHAR(100) NOT NULL, discount DECIMAL(5,4) DEFAULT 0.10)");
                stmt.execute("INSERT IGNORE INTO member (nama, discount) VALUES ('Silver', 0.05), ('Gold', 0.10), ('Platinum', 0.15)");
                // System.out.println("MySQL Database Connected");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("DB init error: " + e.getMessage());
        }
    }

    // Kategori methods
    public void createKategori(String nama) {
        String sql = "INSERT INTO kategori(nama) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.executeUpdate();
            System.out.println("Kategori dibuat.");
        } catch (SQLException e) {
            System.err.println("Error buat kategori: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> readAllKategori() {
        List<Map<String, Object>> kategori = new ArrayList<>();
        String sql = "SELECT * FROM kategori";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> kat = new HashMap<>();
                kat.put("id", rs.getInt("id"));
                kat.put("nama", rs.getString("nama"));
                kategori.add(kat);
            }
        } catch (SQLException e) {
            System.err.println("Error baca kategori: " + e.getMessage());
        }
        return kategori;
    }

    public void updateKategori(int id, String nama) {
        String sql = "UPDATE kategori SET nama = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Kategori diperbarui.");
            } else {
                System.out.println("Kategori tidak ditemukan ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error update kategori: " + e.getMessage());
        }
    }

    public void deleteKategori(int id) {
        String sql = "DELETE FROM kategori WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Kategori dihapus.");
            } else {
                System.out.println("Kategori tidak ditemukan ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error hapus kategori: " + e.getMessage());
        }
    }

    // Menu methods
    public void createMenu(String nama, int kategoriId, double harga, int stok) {
        String sql = "INSERT INTO menu(nama, kategori_id, harga, stok) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setInt(2, kategoriId);
            pstmt.setDouble(3, harga);
            pstmt.setInt(4, stok);
            pstmt.executeUpdate();
            System.out.println("Menu dibuat.");
        } catch (SQLException e) {
            System.err.println("Error buat menu: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> readAllMenu() {
        List<Map<String, Object>> menus = new ArrayList<>();
        String sql = "SELECT m.*, k.nama as kat_nama FROM menu m LEFT JOIN kategori k ON m.kategori_id = k.id";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> menu = new HashMap<>();
                menu.put("id", rs.getInt("id"));
                menu.put("nama", rs.getString("nama"));
                menu.put("kategori_id", rs.getInt("kategori_id"));
                menu.put("kat_nama", rs.getString("kat_nama"));
                menu.put("harga", rs.getDouble("harga"));
                menu.put("stok", rs.getInt("stok"));
                menus.add(menu);
            }
        } catch (SQLException e) {
            System.err.println("Error baca menu: " + e.getMessage());
        }
        return menus;
    }

    public Map<String, Object> readMenuById(int id) {
        Map<String, Object> menu = null;
        String sql = "SELECT m.*, k.nama as kat_nama FROM menu m LEFT JOIN kategori k ON m.kategori_id = k.id WHERE m.id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    menu = new HashMap<>();
                    menu.put("id", rs.getInt("id"));
                    menu.put("nama", rs.getString("nama"));
                    menu.put("kategori_id", rs.getInt("kategori_id"));
                    menu.put("kat_nama", rs.getString("kat_nama"));
                    menu.put("harga", rs.getDouble("harga"));
                    menu.put("stok", rs.getInt("stok"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error baca menu ID " + id + ": " + e.getMessage());
        }
        return menu;
    }

    public void updateMenu(int id, String nama, int kategoriId, double harga, int stok) {
        String sql = "UPDATE menu SET nama = ?, kategori_id = ?, harga = ?, stok = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setInt(2, kategoriId);
            pstmt.setDouble(3, harga);
            pstmt.setInt(4, stok);
            pstmt.setInt(5, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Menu diperbarui.");
            } else {
                System.out.println("Menu tidak ditemukan ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error update menu: " + e.getMessage());
        }
    }

    public void deleteMenu(int id) {
        String sql = "DELETE FROM menu WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Menu dihapus.");
            } else {
                System.out.println("Menu tidak ditemukan ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error hapus menu: " + e.getMessage());
        }
    }

    public void updateStok(int menuId, int qty) {
        String sql = "UPDATE menu SET stok = stok - ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, qty);
            pstmt.setInt(2, menuId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Stok menu ID " + menuId + " dikurangi " + qty);
            } else {
                System.out.println("Menu ID " + menuId + " tidak ditemukan");
            }
        } catch (SQLException e) {
            System.err.println("Error update stok: " + e.getMessage());
        }
    }

    // Member methods (tingkat member)
    public void createMember(String nama, double discount) {
        String sql = "INSERT INTO member(nama, discount) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setDouble(2, discount);
            pstmt.executeUpdate();
            System.out.println("Tingkat member dibuat.");
        } catch (SQLException e) {
            System.err.println("Error buat tingkat member: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> readAllMembers() {
        List<Map<String, Object>> members = new ArrayList<>();
        String sql = "SELECT * FROM member";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> mem = new HashMap<>();
                mem.put("id", rs.getInt("id"));
                mem.put("nama", rs.getString("nama"));
                mem.put("discount", rs.getDouble("discount"));
                members.add(mem);
            }
        } catch (SQLException e) {
            System.err.println("Error baca tingkat member: " + e.getMessage());
        }
        return members;
    }

    public Map<String, Object> readMemberById(int id) {
        Map<String, Object> member = null;
        String sql = "SELECT * FROM member WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            System.err.println("Error baca tingkat member ID " + id + ": " + e.getMessage());
        }
        return member;
    }

    public void updateMember(int id, String nama, double discount) {
        String sql = "UPDATE member SET nama = ?, discount = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nama);
            pstmt.setDouble(2, discount);
            pstmt.setInt(3, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Tingkat member diperbarui.");
            } else {
                System.out.println("Tingkat member tidak ditemukan ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error update tingkat member: " + e.getMessage());
        }
    }

    public void deleteMember(int id) {
        String sql = "DELETE FROM member WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Tingkat member dihapus.");
            } else {
                System.out.println("Tingkat member tidak ditemukan ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error hapus tingkat member: " + e.getMessage());
        }
    }

    // Search menus
    public List<Map<String, Object>> searchMenus(String query) {
        List<Map<String, Object>> menus = new ArrayList<>();
        String sql = "SELECT m.*, k.nama as kat_nama FROM menu m LEFT JOIN kategori k ON m.kategori_id = k.id WHERE m.nama LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> menu = new HashMap<>();
                    menu.put("id", rs.getInt("id"));
                    menu.put("nama", rs.getString("nama"));
                    menu.put("kategori_id", rs.getInt("kategori_id"));
                    menu.put("kat_nama", rs.getString("kat_nama"));
                    menu.put("harga", rs.getDouble("harga"));
                    menu.put("stok", rs.getInt("stok"));
                    menus.add(menu);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error search menu '" + query + "': " + e.getMessage());
        }
        return menus;
    }
}

