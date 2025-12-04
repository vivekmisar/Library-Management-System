import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

/**
 * A complete, standalone Library Management System in a single Java file.
 * This application uses Java Swing for the GUI and Object Serialization for data persistence.
 *
 * To run:
 * 1. Save this code as LibraryManagementSystem.java
 * 2. Compile and run it.
 * 3. In IntelliJ IDEA, just paste this into the file and click the 'Run' button.
 */
public class LibraryManagementSystem extends JFrame {

    // --- Data Storage ---
    // Using static lists to be accessible by all static inner classes.
    private static ArrayList<Book> books = new ArrayList<>();
    private static ArrayList<Member> members = new ArrayList<>();
    private static ArrayList<IssueRecord> issueRecords = new ArrayList<>();

    // --- File Paths for Persistence ---
    private static final String BOOKS_FILE = "books.ser";
    private static final String MEMBERS_FILE = "members.ser";
    private static final String ISSUES_FILE = "issues.ser";

    // --- GUI Components ---
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel;
    private final WelcomePanel welcomePanel;
    private final ViewBooksPanel viewBooksPanel;
    private final ViewMembersPanel viewMembersPanel;
    private final IssueReturnPanel issueReturnPanel;

    // --- Main Entry Point ---
    public static void main(String[] args) {
        // Set Nimbus Look and Feel for a modern UI
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load data from files at startup
        loadData();

        // Run the GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LibraryManagementSystem().setVisible(true));
    }

    // --- Main Frame Constructor ---
    public LibraryManagementSystem() {
        // --- Frame Setup ---
        setTitle("📚 Library Management System");
        setSize(900, 700);
        setLocationRelativeTo(null); // Center the window

        // Use a custom close operation to trigger data saving
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        setLayout(new BorderLayout(10, 10));

        // --- 1. Top Title Panel ---
        JLabel titleLabel = new JLabel("Library Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // --- 2. West Navigation Panel ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        navPanel.add(Box.createVerticalStrut(20)); // Spacing

        String[] navOptions = {"Home", "View Books", "View Members", "Issue/Return"};
        for (String opt : navOptions) {
            JButton btn = createNavButton(opt);
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(10)); // Spacing between buttons
        }

        // Add action buttons
        navPanel.add(Box.createVerticalGlue()); // Push buttons below to the bottom
        JButton saveBtn = createNavButton("Save Data");
        saveBtn.addActionListener(e -> {
            saveData();
            JOptionPane.showMessageDialog(this, "Data saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        navPanel.add(saveBtn);
        navPanel.add(Box.createVerticalStrut(10));

        JButton exitBtn = createNavButton("Exit");
        exitBtn.addActionListener(e -> handleExit());
        navPanel.add(exitBtn);

        add(navPanel, BorderLayout.WEST);

        // --- 3. Center Main Panel (with CardLayout) ---
        mainPanel = new JPanel(cardLayout);

        // Add all functional panels to the CardLayout
        welcomePanel = new WelcomePanel();
        viewBooksPanel = new ViewBooksPanel();
        viewMembersPanel = new ViewMembersPanel();
        issueReturnPanel = new IssueReturnPanel();

        mainPanel.add(welcomePanel, "Home");
        mainPanel.add(viewBooksPanel, "View Books");
        mainPanel.add(viewMembersPanel, "View Members");
        mainPanel.add(issueReturnPanel, "Issue/Return");

        add(mainPanel, BorderLayout.CENTER);
    }

    // --- Helper Methods ---

    /**
     * Creates a styled navigation button.
     */
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setMaximumSize(new Dimension(150, 40));
        button.setMinimumSize(new Dimension(150, 40));
        button.setPreferredSize(new Dimension(150, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add action listeners to switch cards
        switch (text) {
            case "Home":
                button.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
                break;
            case "View Books":
                button.addActionListener(e -> cardLayout.show(mainPanel, "View Books"));
                break;
            case "View Members":
                button.addActionListener(e -> cardLayout.show(mainPanel, "View Members"));
                break;
            case "Issue/Return":
                button.addActionListener(e -> cardLayout.show(mainPanel, "Issue/Return"));
                break;
        }
        return button;
    }

    /**
     * Handles the exit operation, prompting user to save data.
     */
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to save your changes before exiting?",
                "Confirm Exit",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            saveData();
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        // If CANCEL, do nothing and stay in the app.
    }

    // =================================================================================
    // --- Data Model Classes (Static Inner Classes) ---
    // =================================================================================

    /**
     * Represents a Book. Implements Serializable for data persistence.
     */
    static class Book implements Serializable {
        private static final long serialVersionUID = 1L; // For serialization
        private final String id;
        private String title;
        private String author;
        private String publisher;
        private int quantity; // Total quantity owned
        private int issued; // Number of copies currently issued

        public Book(String id, String title, String author, String publisher, int quantity) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.quantity = quantity;
            this.issued = 0; // Initially, 0 are issued
        }

        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getPublisher() { return publisher; }
        public int getQuantity() { return quantity; }
        public int getIssued() { return issued; }

        /**
         * Returns the number of copies available to be issued.
         */
        public int getAvailableQuantity() {
            return quantity - issued;
        }

        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setAuthor(String author) { this.author = author; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        /**
         * Call when a book is issued. Returns false if no copies are available.
         */
        public boolean issueBook() {
            if (getAvailableQuantity() > 0) {
                this.issued++;
                return true;
            }
            return false;
        }

        /**
         * Call when a book is returned.
         */
        public void returnBook() {
            if (this.issued > 0) {
                this.issued--;
            }
        }

        @Override
        public String toString() {
            return title + " by " + author; // Used in JComboBox
        }
    }

    /**
     * Represents a Library Member. Implements Serializable.
     */
    static class Member implements Serializable {
        private static final long serialVersionUID = 2L;
        private final String id;
        private String name;
        private String email;
        private String contact;

        public Member(String id, String name, String email, String contact) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.contact = contact;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getContact() { return contact; }

        // Setters
        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setContact(String contact) { this.contact = contact; }

        @Override
        public String toString() {
            return name + " (" + id + ")"; // Used in JComboBox
        }
    }

    /**
     * Represents a record of a book being issued. Implements Serializable.
     */
    static class IssueRecord implements Serializable {
        private static final long serialVersionUID = 3L;
        private final String issueId;
        private final String bookId;
        private final String memberId;
        private final Date issueDate;
        private Date returnDate;

        public IssueRecord(String bookId, String memberId) {
            this.issueId = "I-" + System.currentTimeMillis(); // Unique ID
            this.bookId = bookId;
            this.memberId = memberId;
            this.issueDate = new Date(); // Set to current date/time
            this.returnDate = null; // Not returned yet
        }

        // Getters
        public String getIssueId() { return issueId; }
        public String getBookId() { return bookId; }
        public String getMemberId() { return memberId; }
        public Date getIssueDate() { return issueDate; }
        public Date getReturnDate() { return returnDate; }

        public boolean isReturned() {
            return returnDate != null;
        }

        // Setters
        public void setReturnDate(Date returnDate) {
            this.returnDate = returnDate;
        }
    }

    // =================================================================================
    // --- GUI Panels (Static Inner Classes) ---
    // =================================================================================

    /**
     * The default panel shown on startup.
     */
    static class WelcomePanel extends JPanel {
        public WelcomePanel() {
            setLayout(new BorderLayout());
            JLabel welcomeLabel = new JLabel("Welcome to the Library!", JLabel.CENTER);
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 32));
            add(welcomeLabel, BorderLayout.CENTER);

            // Add some stats
            JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
            statsPanel.add(new JLabel("Total Books: " + books.size()));
            statsPanel.add(new JLabel("Total Members: " + members.size()));
            statsPanel.add(new JLabel("Books Issued: " + issueRecords.stream().filter(r -> !r.isReturned()).count()));

            add(statsPanel, BorderLayout.SOUTH);

            // Add component listener to refresh stats when shown
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    // This is a bit simplistic, but demonstrates the idea
                    // For a real app, you'd have a more robust way to update stats
                    statsPanel.removeAll();
                    statsPanel.add(new JLabel("Total Books: " + books.size()));
                    statsPanel.add(new JLabel("Total Members: " + members.size()));
                    statsPanel.add(new JLabel("Books Issued: " + issueRecords.stream().filter(r -> !r.isReturned()).count()));
                    statsPanel.revalidate();
                    statsPanel.repaint();
                }
            });
        }
    }

    /**
     * Panel for Viewing, Adding, Updating, and Deleting Books.
     */
    static class ViewBooksPanel extends JPanel {
        private final DefaultTableModel tableModel;
        private final JTable table;

        public ViewBooksPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            // --- Table ---
            String[] columnNames = {"Book ID", "Title", "Author", "Publisher", "Quantity", "Issued", "Available"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                // Make cells non-editable
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            add(new JScrollPane(table), BorderLayout.CENTER);

            // --- Button Panel ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

            JButton addBtn = new JButton("Add New Book");
            addBtn.addActionListener(e -> {
                AddBookDialog dialog = new AddBookDialog();
                dialog.setVisible(true);
                // If book was added, refresh the table
                if (dialog.isBookAdded()) {
                    refreshTable();
                }
            });
            buttonPanel.add(addBtn);

            JButton updateBtn = new JButton("Update Selected Book");
            updateBtn.addActionListener(e -> handleUpdateBook());
            buttonPanel.add(updateBtn);

            JButton deleteBtn = new JButton("Delete Selected Book");
            deleteBtn.addActionListener(e -> handleDeleteBook());
            buttonPanel.add(deleteBtn);

            add(buttonPanel, BorderLayout.SOUTH);

            // Refresh table when panel is shown
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    refreshTable();
                }
            });
        }

        /**
         * Repopulates the JTable with the latest data from the 'books' list.
         */
        public void refreshTable() {
            // Clear existing rows
            tableModel.setRowCount(0);
            // Add new rows from the list
            for (Book b : books) {
                tableModel.addRow(new Object[]{
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getPublisher(),
                        b.getQuantity(),
                        b.getIssued(),
                        b.getAvailableQuantity()
                });
            }
        }

        private void handleUpdateBook() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String bookId = (String) tableModel.getValueAt(selectedRow, 0);
            // Find the book object
            Book bookToUpdate = books.stream()
                    .filter(b -> b.getId().equals(bookId))
                    .findFirst()
                    .orElse(null);

            if (bookToUpdate == null) {
                JOptionPane.showMessageDialog(this, "Error: Could not find selected book.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Open the AddBookDialog, pre-filled with the book's data
            AddBookDialog dialog = new AddBookDialog(bookToUpdate);
            dialog.setVisible(true);

            // If data was updated, refresh
            if (dialog.isBookAdded()) { // "isBookAdded" flag is used for both add and update
                refreshTable();
            }
        }

        private void handleDeleteBook() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String bookId = (String) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this book? ID: " + bookId,
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Check if book is currently issued
                boolean isIssued = issueRecords.stream().anyMatch(r -> r.getBookId().equals(bookId) && !r.isReturned());
                if (isIssued) {
                    JOptionPane.showMessageDialog(this, "Cannot delete book. It is currently issued to a member.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Remove the book
                books.removeIf(b -> b.getId().equals(bookId));
                refreshTable(); // Refresh table to show removal
                JOptionPane.showMessageDialog(this, "Book deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Panel for Viewing, Adding, Updating, and Deleting Members.
     */
    static class ViewMembersPanel extends JPanel {
        private final DefaultTableModel tableModel;
        private final JTable table;

        public ViewMembersPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            // --- Table ---
            String[] columnNames = {"Member ID", "Name", "Email", "Contact No."};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            add(new JScrollPane(table), BorderLayout.CENTER);

            // --- Button Panel ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

            JButton addBtn = new JButton("Add New Member");
            addBtn.addActionListener(e -> {
                AddMemberDialog dialog = new AddMemberDialog();
                dialog.setVisible(true);
                if (dialog.isMemberAdded()) {
                    refreshTable();
                }
            });
            buttonPanel.add(addBtn);

            JButton updateBtn = new JButton("Update Selected Member");
            updateBtn.addActionListener(e -> handleUpdateMember());
            buttonPanel.add(updateBtn);

            JButton deleteBtn = new JButton("Delete Selected Member");
            deleteBtn.addActionListener(e -> handleDeleteMember());
            buttonPanel.add(deleteBtn);

            add(buttonPanel, BorderLayout.SOUTH);

            // Refresh table when panel is shown
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    refreshTable();
                }
            });
        }

        /**
         * Repopulates the JTable with the latest data from the 'members' list.
         */
        public void refreshTable() {
            tableModel.setRowCount(0);
            for (Member m : members) {
                tableModel.addRow(new Object[]{
                        m.getId(),
                        m.getName(),
                        m.getEmail(),
                        m.getContact()
                });
            }
        }

        private void handleUpdateMember() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a member to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String memberId = (String) tableModel.getValueAt(selectedRow, 0);
            Member memberToUpdate = members.stream()
                    .filter(m -> m.getId().equals(memberId))
                    .findFirst()
                    .orElse(null);

            if (memberToUpdate == null) {
                JOptionPane.showMessageDialog(this, "Error: Could not find selected member.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AddMemberDialog dialog = new AddMemberDialog(memberToUpdate);
            dialog.setVisible(true);

            if (dialog.isMemberAdded()) {
                refreshTable();
            }
        }

        private void handleDeleteMember() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a member to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String memberId = (String) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this member? ID: " + memberId,
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Check if member has outstanding books
                boolean hasBooks = issueRecords.stream().anyMatch(r -> r.getMemberId().equals(memberId) && !r.isReturned());
                if (hasBooks) {
                    JOptionPane.showMessageDialog(this, "Cannot delete member. They have books currently issued.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                members.removeIf(m -> m.getId().equals(memberId));
                refreshTable();
                JOptionPane.showMessageDialog(this, "Member deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Panel for Issuing books to members and handling returns.
     */
    static class IssueReturnPanel extends JPanel {
        private final JComboBox<Book> bookComboBox;
        private final JComboBox<Member> memberComboBox;
        private final DefaultTableModel issueTableModel;
        private final JTable issueTable;

        public IssueReturnPanel() {
            setLayout(new GridLayout(2, 1, 10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            // --- 1. Issue Book Panel ---
            JPanel issuePanel = new JPanel(new GridBagLayout());
            issuePanel.setBorder(new TitledBorder("Issue New Book"));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Row 0: Select Book
            gbc.gridx = 0; gbc.gridy = 0;
            issuePanel.add(new JLabel("Select Book:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
            bookComboBox = new JComboBox<>();
            issuePanel.add(bookComboBox, gbc);

            // Row 1: Select Member
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            issuePanel.add(new JLabel("Select Member:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
            memberComboBox = new JComboBox<>();
            issuePanel.add(memberComboBox, gbc);

            // Row 2: Issue Button
            gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
            JButton issueButton = new JButton("Issue Book");
            issueButton.addActionListener(e -> handleIssueBook());
            issuePanel.add(issueButton, gbc);

            add(issuePanel);

            // --- 2. Return Book Panel (Active Issues) ---
            JPanel returnPanel = new JPanel(new BorderLayout(10, 10));
            returnPanel.setBorder(new TitledBorder("Return Book (View Active Issues)"));

            String[] columnNames = {"Issue ID", "Book Title", "Member Name", "Issue Date"};
            issueTableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            issueTable = new JTable(issueTableModel);
            issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            returnPanel.add(new JScrollPane(issueTable), BorderLayout.CENTER);

            JButton returnButton = new JButton("Return Selected Book");
            returnButton.addActionListener(e -> handleReturnBook());
            returnPanel.add(returnButton, BorderLayout.SOUTH);

            add(returnPanel);

            // Add listener to refresh data when panel is shown
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    refreshPanelData();
                }
            });
        }

        /**
         * Refreshes both the ComboBoxes and the outstanding issues table.
         */
        public void refreshPanelData() {
            // Refresh Book ComboBox
            bookComboBox.removeAllItems();
            books.stream()
                    .filter(b -> b.getAvailableQuantity() > 0) // Only show books that are available
                    .forEach(bookComboBox::addItem);

            // Refresh Member ComboBox
            memberComboBox.removeAllItems();
            members.forEach(memberComboBox::addItem);

            // Refresh Active Issues Table
            issueTableModel.setRowCount(0);
            for (IssueRecord record : issueRecords) {
                if (!record.isReturned()) {
                    // Find book and member names from their IDs
                    String bookTitle = books.stream()
                            .filter(b -> b.getId().equals(record.getBookId()))
                            .map(Book::getTitle)
                            .findFirst()
                            .orElse("N/A");
                    String memberName = members.stream()
                            .filter(m -> m.getId().equals(record.getMemberId()))
                            .map(Member::getName)
                            .findFirst()
                            .orElse("N/A");

                    issueTableModel.addRow(new Object[]{
                            record.getIssueId(),
                            bookTitle,
                            memberName,
                            record.getIssueDate().toString()
                    });
                }
            }
        }

        private void handleIssueBook() {
            Book selectedBook = (Book) bookComboBox.getSelectedItem();
            Member selectedMember = (Member) memberComboBox.getSelectedItem();

            if (selectedBook == null || selectedMember == null) {
                JOptionPane.showMessageDialog(this, "Please select both a book and a member.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Update book availability
            if (selectedBook.issueBook()) {
                // 2. Create new issue record
                IssueRecord newRecord = new IssueRecord(selectedBook.getId(), selectedMember.getId());
                issueRecords.add(newRecord);

                // 3. Refresh this panel
                refreshPanelData();
                JOptionPane.showMessageDialog(this, "Book issued successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error: No available copies of this book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void handleReturnBook() {
            int selectedRow = issueTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an issued book to return.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String issueId = (String) issueTableModel.getValueAt(selectedRow, 0);

            // Find the IssueRecord
            Optional<IssueRecord> recordOpt = issueRecords.stream()
                    .filter(r -> r.getIssueId().equals(issueId))
                    .findFirst();

            if (recordOpt.isPresent()) {
                IssueRecord record = recordOpt.get();

                // 1. Mark as returned
                record.setReturnDate(new Date());

                // 2. Update book availability
                books.stream()
                        .filter(b -> b.getId().equals(record.getBookId()))
                        .findFirst()
                        .ifPresent(Book::returnBook); // Tell the book it was returned

                // 3. Refresh panel
                refreshPanelData();
                JOptionPane.showMessageDialog(this, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error: Could not find issue record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =================================================================================
    // --- GUI Dialogs (Static Inner Classes) ---
    // =================================================================================

    /**
     * A modal dialog for adding a new book or updating an existing one.
     */
    static class AddBookDialog extends JDialog {
        private final JTextField idField, titleField, authorField, publisherField, quantityField;
        private boolean bookAdded = false;
        private final Book bookToUpdate; // Null if adding new, non-null if updating

        // Constructor for Adding a new book
        public AddBookDialog() {
            this(null); // Call the update constructor with null
        }

        // Constructor for Updating an existing book
        public AddBookDialog(Book book) {
            this.bookToUpdate = book;

            setTitle(book == null ? "Add New Book" : "Update Book");
            setSize(450, 300);
            setLocationRelativeTo(null); // Center
            setModal(true); // Block other windows

            setLayout(new BorderLayout(10, 10));

            // Form Panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // ID
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Book ID (e.g., B-101):"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
            idField = new JTextField(20);
            formPanel.add(idField, gbc);

            // Title
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            formPanel.add(new JLabel("Title:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            titleField = new JTextField(20);
            formPanel.add(titleField, gbc);

            // Author
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Author:"), gbc);
            gbc.gridx = 1; gbc.gridy = 2;
            authorField = new JTextField(20);
            formPanel.add(authorField, gbc);

            // Publisher
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Publisher:"), gbc);
            gbc.gridx = 1; gbc.gridy = 3;
            publisherField = new JTextField(20);
            formPanel.add(publisherField, gbc);

            // Quantity
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Quantity:"), gbc);
            gbc.gridx = 1; gbc.gridy = 4;
            quantityField = new JTextField(20);
            formPanel.add(quantityField, gbc);

            add(formPanel, BorderLayout.CENTER);

            // --- Pre-fill data if updating ---
            if (bookToUpdate != null) {
                idField.setText(bookToUpdate.getId());
                idField.setEditable(false); // Can't change ID
                titleField.setText(bookToUpdate.getTitle());
                authorField.setText(bookToUpdate.getAuthor());
                publisherField.setText(bookToUpdate.getPublisher());
                quantityField.setText(String.valueOf(bookToUpdate.getQuantity()));
            }

            // Button Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> saveBook());
            buttonPanel.add(saveButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);

            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void saveBook() {
            // --- 1. Get Data ---
            String id = idField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            String quantityStr = quantityField.getText().trim();

            // --- 2. Validate Data ---
            if (id.isEmpty() || title.isEmpty() || author.isEmpty() || quantityStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Quantity must be a valid positive number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // --- 3. Save Data (Add vs Update) ---
            if (bookToUpdate == null) {
                // --- ADD NEW BOOK ---
                // Check for duplicate ID
                if (books.stream().anyMatch(b -> b.getId().equalsIgnoreCase(id))) {
                    JOptionPane.showMessageDialog(this, "Book ID already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Book newBook = new Book(id, title, author, publisher, quantity);
                books.add(newBook);

            } else {
                // --- UPDATE EXISTING BOOK ---
                // Can't update quantity if it's less than currently issued
                if (quantity < bookToUpdate.getIssued()) {
                    JOptionPane.showMessageDialog(this, "Quantity cannot be set lower than the number of currently issued books (" + bookToUpdate.getIssued() + ").", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                bookToUpdate.setTitle(title);
                bookToUpdate.setAuthor(author);
                bookToUpdate.setPublisher(publisher);
                bookToUpdate.setQuantity(quantity);
            }

            bookAdded = true;
            JOptionPane.showMessageDialog(this, "Book saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close the dialog
        }

        /**
         * Used by the parent panel to check if it needs to refresh.
         */
        public boolean isBookAdded() {
            return bookAdded;
        }
    }

    /**
     * A modal dialog for adding a new member or updating an existing one.
     */
    static class AddMemberDialog extends JDialog {
        private final JTextField idField, nameField, emailField, contactField;
        private boolean memberAdded = false;
        private final Member memberToUpdate; // Null if adding new

        // Constructor for Adding
        public AddMemberDialog() {
            this(null);
        }

        // Constructor for Updating
        public AddMemberDialog(Member member) {
            this.memberToUpdate = member;

            setTitle(member == null ? "Add New Member" : "Update Member");
            setSize(450, 250);
            setLocationRelativeTo(null);
            setModal(true);

            setLayout(new BorderLayout(10, 10));

            // Form Panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // ID
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Member ID (e.g., M-101):"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
            idField = new JTextField(20);
            formPanel.add(idField, gbc);

            // Name
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            formPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            nameField = new JTextField(20);
            formPanel.add(nameField, gbc);

            // Email
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.gridy = 2;
            emailField = new JTextField(20);
            formPanel.add(emailField, gbc);

            // Contact
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Contact No.:"), gbc);
            gbc.gridx = 1; gbc.gridy = 3;
            contactField = new JTextField(20);
            formPanel.add(contactField, gbc);

            add(formPanel, BorderLayout.CENTER);

            // Pre-fill data if updating
            if (memberToUpdate != null) {
                idField.setText(memberToUpdate.getId());
                idField.setEditable(false);
                nameField.setText(memberToUpdate.getName());
                emailField.setText(memberToUpdate.getEmail());
                contactField.setText(memberToUpdate.getContact());
            }

            // Button Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> saveMember());
            buttonPanel.add(saveButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);

            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void saveMember() {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String contact = contactField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // TODO: Add email validation

            if (memberToUpdate == null) {
                // --- ADD NEW ---
                if (members.stream().anyMatch(m -> m.getId().equalsIgnoreCase(id))) {
                    JOptionPane.showMessageDialog(this, "Member ID already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Member newMember = new Member(id, name, email, contact);
                members.add(newMember);

            } else {
                // --- UPDATE ---
                memberToUpdate.setName(name);
                memberToUpdate.setEmail(email);
                memberToUpdate.setContact(contact);
            }

            memberAdded = true;
            JOptionPane.showMessageDialog(this, "Member saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }

        public boolean isMemberAdded() {
            return memberAdded;
        }
    }


    // =================================================================================
    // --- Data Persistence (Serialization) ---
    // =================================================================================

    /**
     * Saves all data lists to their respective .ser files.
     */
    public static void saveData() {
        try (ObjectOutputStream oosBooks = new ObjectOutputStream(new FileOutputStream(BOOKS_FILE));
             ObjectOutputStream oosMembers = new ObjectOutputStream(new FileOutputStream(MEMBERS_FILE));
             ObjectOutputStream oosIssues = new ObjectOutputStream(new FileOutputStream(ISSUES_FILE))) {

            oosBooks.writeObject(books);
            oosMembers.writeObject(members);
            oosIssues.writeObject(issueRecords);

            System.out.println("Data saved successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving data: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads all data lists from their .ser files.
     * If files are not found (e.g., first run), it does nothing.
     */
    @SuppressWarnings("unchecked")
    public static void loadData() {
        try (ObjectInputStream oisBooks = new ObjectInputStream(new FileInputStream(BOOKS_FILE));
             ObjectInputStream oisMembers = new ObjectInputStream(new FileInputStream(MEMBERS_FILE));
             ObjectInputStream oisIssues = new ObjectInputStream(new FileInputStream(ISSUES_FILE))) {

            books = (ArrayList<Book>) oisBooks.readObject();
            members = (ArrayList<Member>) oisMembers.readObject();
            issueRecords = (ArrayList<IssueRecord>) oisIssues.readObject();

            System.out.println("Data loaded successfully.");

        } catch (FileNotFoundException e) {
            System.out.println("No save data found. Starting with empty lists.");
            // This is normal on first run, so we just initialize empty lists (already done)
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage() + "\nStarting with empty lists.", "Load Error", JOptionPane.ERROR_MESSAGE);
            // On corruption, restart with empty lists
            books = new ArrayList<>();
            members = new ArrayList<>();
            issueRecords = new ArrayList<>();
        }
    }
}