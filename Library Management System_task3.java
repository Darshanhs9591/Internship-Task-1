// Project: Mini Library Management System (OOP) - Java
// Deliverables: Multi-class Java project. Tools: Java, VS Code, Terminal
// Files included below. Save each section as its own .java file in the same folder.

/*
README
------
Files:
 - Book.java
 - User.java
 - Library.java
 - LibraryApp.java  (contains main())

How to compile (terminal):
  javac *.java
  java LibraryApp

How to run in VS Code:
 - Open folder containing the .java files.
 - Install Java Extension Pack (if not installed).
 - Use Run|Run Without Debugging or use the terminal to compile + run.

Features implemented:
 - Add / Remove / Search books
 - Register / Remove users
 - Borrow / Return books with simple availability checks
 - Lists: all books, available books, borrowed books, users
 - Simple command-line menu for demonstration
 - Uses OOP: encapsulation, single-responsibility, basic exception handling

*/

// -------------------- Book.java --------------------
public class Book {
    private final String isbn;
    private String title;
    private String author;
    private boolean isBorrowed;

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.isBorrowed = false;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public boolean isBorrowed() { return isBorrowed; }

    public void borrow() {
        if (isBorrowed) throw new IllegalStateException("Book already borrowed");
        isBorrowed = true;
    }

    public void returned() {
        if (!isBorrowed) throw new IllegalStateException("Book is not borrowed");
        isBorrowed = false;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s %s", isbn, title, author, isBorrowed ? "(borrowed)" : "(available)");
    }
}

// -------------------- User.java --------------------
import java.util.ArrayList;
import java.util.List;

public class User {
    private final String userId;
    private String name;
    private final List<String> borrowedIsbns; // store ISBNs of borrowed books

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.borrowedIsbns = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getBorrowedIsbns() { return borrowedIsbns; }

    public void borrowBook(String isbn) {
        borrowedIsbns.add(isbn);
    }

    public boolean returnBook(String isbn) {
        return borrowedIsbns.remove(isbn);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - borrowed: %d", name, userId, borrowedIsbns.size());
    }
}

// -------------------- Library.java --------------------
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Library {
    private final List<Book> books = new ArrayList<>();
    private final List<User> users = new ArrayList<>();

    // Book operations
    public void addBook(Book b) {
        if (findBookByIsbn(b.getIsbn()) != null) {
            throw new IllegalArgumentException("A book with same ISBN already exists: " + b.getIsbn());
        }
        books.add(b);
    }

    public boolean removeBook(String isbn) {
        Book b = findBookByIsbn(isbn);
        if (b == null) return false;
        if (b.isBorrowed()) throw new IllegalStateException("Cannot remove a borrowed book");
        return books.remove(b);
    }

    public Book findBookByIsbn(String isbn) {
        for (Book b : books) if (b.getIsbn().equals(isbn)) return b;
        return null;
    }

    public List<Book> searchByTitle(String q) {
        String lower = q.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Book> getAllBooks() { return new ArrayList<>(books); }
    public List<Book> getAvailableBooks() {
        return books.stream().filter(b -> !b.isBorrowed()).collect(Collectors.toList());
    }
    public List<Book> getBorrowedBooks() {
        return books.stream().filter(Book::isBorrowed).collect(Collectors.toList());
    }

    // User operations
    public void registerUser(User u) {
        if (findUserById(u.getUserId()) != null) throw new IllegalArgumentException("User already exists: " + u.getUserId());
        users.add(u);
    }

    public boolean removeUser(String userId) {
        User u = findUserById(userId);
        if (u == null) return false;
        if (!u.getBorrowedIsbns().isEmpty()) throw new IllegalStateException("User has borrowed books");
        return users.remove(u);
    }

    public User findUserById(String userId) {
        for (User u : users) if (u.getUserId().equals(userId)) return u;
        return null;
    }

    public List<User> getAllUsers() { return new ArrayList<>(users); }

    // Borrow/Return
    public void borrowBook(String userId, String isbn) {
        User u = findUserById(userId);
        if (u == null) throw new IllegalArgumentException("User not found: " + userId);
        Book b = findBookByIsbn(isbn);
        if (b == null) throw new IllegalArgumentException("Book not found: " + isbn);
        if (b.isBorrowed()) throw new IllegalStateException("Book already borrowed: " + isbn);

        b.borrow();
        u.borrowBook(isbn);
    }

    public void returnBook(String userId, String isbn) {
        User u = findUserById(userId);
        if (u == null) throw new IllegalArgumentException("User not found: " + userId);
        Book b = findBookByIsbn(isbn);
        if (b == null) throw new IllegalArgumentException("Book not found: " + isbn);
        if (!b.isBorrowed()) throw new IllegalStateException("Book is not borrowed: " + isbn);

        boolean removed = u.returnBook(isbn);
        if (!removed) throw new IllegalStateException("This user didn't borrow that book");
        b.returned();
    }
}

// -------------------- LibraryApp.java --------------------
import java.util.List;
import java.util.Scanner;

public class LibraryApp {
    private static final Library library = new Library();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        seedSampleData();
        showMenu();
    }

    private static void seedSampleData() {
        library.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch"));
        library.addBook(new Book("978-0596009205", "Head First Java", "Kathy Sierra"));
        library.addBook(new Book("978-1617291999", "Java 8 in Action", "Raoul-Gabriel Urma"));

        library.registerUser(new User("u1001", "Alice"));
        library.registerUser(new User("u1002", "Bob"));
    }

    private static void showMenu() {
        while (true) {
            System.out.println("\n--- Mini Library Management ---");
            System.out.println("1. List all books");
            System.out.println("2. List available books");
            System.out.println("3. Search book by title");
            System.out.println("4. Add book");
            System.out.println("5. Register user");
            System.out.println("6. Borrow book");
            System.out.println("7. Return book");
            System.out.println("8. List users");
            System.out.println("9. Exit");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": listAllBooks(); break;
                    case "2": listAvailableBooks(); break;
                    case "3": searchByTitle(); break;
                    case "4": addBook(); break;
                    case "5": registerUser(); break;
                    case "6": borrowBook(); break;
                    case "7": returnBook(); break;
                    case "8": listUsers(); break;
                    case "9": System.out.println("Bye!"); return;
                    default: System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void listAllBooks() {
        List<Book> books = library.getAllBooks();
        if (books.isEmpty()) System.out.println("No books in library");
        else books.forEach(b -> System.out.println(b));
    }

    private static void listAvailableBooks() {
        List<Book> books = library.getAvailableBooks();
        if (books.isEmpty()) System.out.println("No available books");
        else books.forEach(b -> System.out.println(b));
    }

    private static void searchByTitle() {
        System.out.print("Enter title query: ");
        String q = sc.nextLine();
        List<Book> res = library.searchByTitle(q);
        if (res.isEmpty()) System.out.println("No matches found");
        else res.forEach(System.out::println);
    }

    private static void addBook() {
        System.out.print("ISBN: ");
        String isbn = sc.nextLine().trim();
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Author: ");
        String author = sc.nextLine().trim();
        library.addBook(new Book(isbn, title, author));
        System.out.println("Book added.");
    }

    private static void registerUser() {
        System.out.print("User ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        library.registerUser(new User(id, name));
        System.out.println("User registered.");
    }

    private static void borrowBook() {
        System.out.print("User ID: ");
        String uid = sc.nextLine().trim();
        System.out.print("Book ISBN: ");
        String isbn = sc.nextLine().trim();
        library.borrowBook(uid, isbn);
        System.out.println("Book borrowed.");
    }

    private static void returnBook() {
        System.out.print("User ID: ");
        String uid = sc.nextLine().trim();
        System.out.print("Book ISBN: ");
        String isbn = sc.nextLine().trim();
        library.returnBook(uid, isbn);
        System.out.println("Book returned.");
    }

    private static void listUsers() {
        List<User> users = library.getAllUsers();
        if (users.isEmpty()) System.out.println("No users");
        else users.forEach(u -> System.out.println(u));
    }
}
