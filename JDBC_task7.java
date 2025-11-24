import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EmployeeDBApp {

    // TODO: Change these according to your DB setup
    private static final String URL = "jdbc:mysql://localhost:3306/employee_db";
    // For PostgreSQL use: jdbc:postgresql://localhost:5432/employee_db
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    private static final Scanner sc = new Scanner(System.in);

    // ----- Employee MODEL -----
    static class Employee {
        private int id;
        private String name;
        private double salary;

        public Employee(int id, String name, double salary) {
            this.id = id;
            this.name = name;
            this.salary = salary;
        }

        public Employee(String name, double salary) { // for insert (no id yet)
            this.name = name;
            this.salary = salary;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getSalary() { return salary; }

        public void setName(String name) { this.name = name; }
        public void setSalary(double salary) { this.salary = salary; }

        @Override
        public String toString() {
            return String.format("ID: %d | Name: %s | Salary: %.2f", id, name, salary);
        }
    }

    // ----- DAO (Data Access Object) -----
    static class EmployeeDAO {

        public void addEmployee(Employee e) {
            String sql = "INSERT INTO employees (name, salary) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, e.getName());
                ps.setDouble(2, e.getSalary());
                int rows = ps.executeUpdate();
                System.out.println(rows + " employee(s) inserted.");

            } catch (SQLException ex) {
                System.out.println("Error inserting employee: " + ex.getMessage());
            }
        }

        public List<Employee> getAllEmployees() {
            List<Employee> list = new ArrayList<>();
            String sql = "SELECT id, name, salary FROM employees";

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double salary = rs.getDouble("salary");
                    list.add(new Employee(id, name, salary));
                }

            } catch (SQLException ex) {
                System.out.println("Error fetching employees: " + ex.getMessage());
            }
            return list;
        }

        public void updateEmployee(Employee e) {
            String sql = "UPDATE employees SET name = ?, salary = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, e.getName());
                ps.setDouble(2, e.getSalary());
                ps.setInt(3, e.getId());
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println("Employee updated successfully.");
                } else {
                    System.out.println("Employee with ID " + e.getId() + " not found.");
                }

            } catch (SQLException ex) {
                System.out.println("Error updating employee: " + ex.getMessage());
            }
        }

        public void deleteEmployee(int id) {
            String sql = "DELETE FROM employees WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println("Employee deleted successfully.");
                } else {
                    System.out.println("Employee with ID " + id + " not found.");
                }

            } catch (SQLException ex) {
                System.out.println("Error deleting employee: " + ex.getMessage());
            }
        }
    }

    // ----- MAIN APP / CLI -----
    public static void main(String[] args) {

        // Optional: load JDBC driver explicitly (for older Java)
        // For MySQL: com.mysql.cj.jdbc.Driver
        // For PostgreSQL: org.postgresql.Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // For PostgreSQL, use:
            // Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
        }

        EmployeeDAO dao = new EmployeeDAO();
        int choice;

        do {
            showMenu();
            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> addEmployeeCLI(dao);
                case 2 -> viewEmployeesCLI(dao);
                case 3 -> updateEmployeeCLI(dao);
                case 4 -> deleteEmployeeCLI(dao);
                case 5 -> System.out.println("Exiting... Goodbye!");
                default -> System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 5);

        sc.close();
    }

    private static void showMenu() {
        System.out.println("\n=== EMPLOYEE DATABASE APP (JDBC) ===");
        System.out.println("1. Add Employee");
        System.out.println("2. View All Employees");
        System.out.println("3. Update Employee");
        System.out.println("4. Delete Employee");
        System.out.println("5. Exit");
    }

    // --- CLI Helper Methods ---
    private static void addEmployeeCLI(EmployeeDAO dao) {
        System.out.println("\n--- Add Employee ---");
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        double salary = readDouble("Enter salary: ");
        Employee e = new Employee(name, salary);
        dao.addEmployee(e);
    }

    private static void viewEmployeesCLI(EmployeeDAO dao) {
        System.out.println("\n--- All Employees ---");
        var list = dao.getAllEmployees();
        if (list.isEmpty()) {
            System.out.println("No employees found.");
        } else {
            for (Employee e : list) {
                System.out.println(e);
            }
        }
    }

    private static void updateEmployeeCLI(EmployeeDAO dao) {
        System.out.println("\n--- Update Employee ---");
        int id = readInt("Enter employee ID to update: ");
        System.out.print("Enter new name: ");
        String name = sc.nextLine();
        double salary = readDouble("Enter new salary: ");
        Employee e = new Employee(id, name, salary);
        dao.updateEmployee(e);
    }

    private static void deleteEmployeeCLI(EmployeeDAO dao) {
        System.out.println("\n--- Delete Employee ---");
        int id = readInt("Enter employee ID to delete: ");
        dao.deleteEmployee(id);
    }

    // --- Input helpers ---
    private static int readInt(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                String input = sc.nextLine();
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                String input = sc.nextLine();
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
