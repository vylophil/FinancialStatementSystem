import java.util.*;
import java.io.*;

class Transaction {
    String date;
    String description;
    String debitAccount;
    String creditAccount;
    double amount;

    public Transaction(String date, String description, String debitAccount, String creditAccount, double amount) {
        this.date = date;
        this.description = description;
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
        this.amount = amount;
    }

    // Convert transaction to string for file storage
    public String toFileString() {
        return date + "|" + description + "|" + debitAccount + "|" + creditAccount + "|" + amount;
    }

    // Create transaction from string (for loading from file)
    public static Transaction fromFileString(String fileString) {
        String[] parts = fileString.split("\\|");
        if (parts.length == 5) {
            return new Transaction(parts[0], parts[1], parts[2], parts[3], Double.parseDouble(parts[4]));
        }
        return null;
    }
}

class Account {
    String name;
    ArrayList<Double> debits = new ArrayList<Double>();
    ArrayList<Double> credits = new ArrayList<Double>();

    public Account(String name) {
        this.name = name;
    }

    public void postDebit(double amount) {
        debits.add(amount);
    }

    public void postCredit(double amount) {
        credits.add(amount);
    }

    public double getDebitTotal() {
        double total = 0;
        for (int i = 0; i < debits.size(); i++) {
            total += debits.get(i);
        }
        return total;
    }

    public double getCreditTotal() {
        double total = 0;
        for (int i = 0; i < credits.size(); i++) {
            total += credits.get(i);
        }
        return total;
    }

    public double getDebitBalance() {
        double totalDebits = getDebitTotal();
        double totalCredits = getCreditTotal();
        String type = getType();

        if (type.equals("Asset") || type.equals("Expense")) {
            return totalDebits - totalCredits;
        }
        return 0;
    }

    public double getCreditBalance() {
        double totalDebits = getDebitTotal();
        double totalCredits = getCreditTotal();
        String type = getType();

        if (type.equals("Liability") || type.equals("Equity") || type.equals("Revenue")) {
            return totalCredits - totalDebits;
        }
        return 0;
    }

    public String getType() {
        String n = name.toLowerCase().trim();

        // Assets
        if (n.contains("cash") || n.contains("resources") || n.contains("receivable") ||
            n.contains("supplies") || n.contains("equipment"))
            return "Asset";

        // Liabilities
        else if (n.contains("payable") || n.contains("unearned") || n.contains("loan"))
            return "Liability";

        // Equity
        else if (n.contains("capital") || n.contains("owner") || n.contains("equity"))
            return "Equity";

        // Revenue
        else if (n.contains("revenue") || n.contains("income") || n.contains("sales"))
            return "Revenue";

        // Expense
        else if (n.contains("expense") || n.contains("utilities") || n.contains("rent") ||
                 n.contains("salary") || n.contains("supplies expense"))
            return "Expense";

        else
            return "Unknown";
    }
}

public class FinancialStatementSystem {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Transaction> journal = new ArrayList<Transaction>();
    static LinkedHashMap<String, Account> ledger = new LinkedHashMap<String, Account>();
    static String currentDataFile = "accounting_data.txt"; // Default file

    public static void main(String[] args) {
        // Load existing data when program starts
        loadData();
        
        int choice = 0;

        do {
            System.out.println("\n==============================================");
            System.out.println("   ACCT 1001 - FINANCIAL STATEMENT SYSTEM");
            System.out.println("==============================================");
            System.out.println("1. Add Transaction");
            System.out.println("2. View Journal Entries");
            System.out.println("3. Edit Transaction");
            System.out.println("4. Remove Transaction");
            System.out.println("5. Post to Ledger");
            System.out.println("6. View Trial Balance");
            System.out.println("7. View Financial Statements");
            System.out.println("8. Save Data to File");
            System.out.println("9. Load Data from File");
            System.out.println("10. Change File");
            System.out.println("11. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1)
                addTransaction();
            else if (choice == 2)
                viewJournal();
            else if (choice == 3)
                editTransaction();
            else if (choice == 4)
                removeTransaction();
            else if (choice == 5)
                postToLedger();
            else if (choice == 6)
                viewTrialBalance();
            else if (choice == 7)
                viewFinancialStatements();
            else if (choice == 8)
                saveData();
            else if (choice == 9)
                loadData();
            else if (choice == 10)
                changeFile();
            else if (choice == 11) {
                // Auto-save when exiting (only if there's data)
                if (!journal.isEmpty()) {
                    saveData();
                    System.out.println("Data saved to " + currentDataFile + ". Exiting program...");
                } else {
                    System.out.println("No data to save. Exiting program...");
                }
            }
            else
                System.out.println("Invalid choice. Try again.");
        } while (choice != 11);
    }

    static void changeFile() {
        System.out.println("\n=== CHANGE FILE ===");
        System.out.println("Current file: " + currentDataFile);
        System.out.print("Enter new filename (e.g., company1_data.txt, project_data.txt): ");
        String newFile = sc.nextLine().trim();
        
        if (newFile.isEmpty()) {
            System.out.println("Filename cannot be empty. Operation cancelled.");
            return;
        }
        
        // Check if we have unsaved data
        if (!journal.isEmpty()) {
            System.out.print("You have unsaved data. Save current data before switching? (yes/no): ");
            String saveChoice = sc.nextLine().trim();
            if (saveChoice.equalsIgnoreCase("yes")) {
                saveData();
            }
        }
        
        // Clear current data
        journal.clear();
        ledger.clear();
        
        // Set new file
        currentDataFile = newFile;
        System.out.println("Switched to file: " + currentDataFile);
        
        // Ask if user wants to load data from the new file
        System.out.print("Load data from the new file? (yes/no): ");
        String loadChoice = sc.nextLine().trim();
        if (loadChoice.equalsIgnoreCase("yes")) {
            loadData();
        }
    }

    static void saveData() {
        // Check if there's any data to save
        if (journal.isEmpty()) {
            System.out.println("No transactions to save. Please add some transactions first.");
            return;
        }
        
        System.out.println("Saving to file: " + currentDataFile);
        
        try {
            FileWriter writer = new FileWriter(currentDataFile);
            
            // Save all transactions
            for (Transaction t : journal) {
                writer.write(t.toFileString() + "\n");
            }
            
            writer.close();
            System.out.println("Data successfully saved to " + currentDataFile);
            System.out.println("Total transactions saved: " + journal.size());
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    static void loadData() {
        try {
            File file = new File(currentDataFile);
            if (!file.exists()) {
                System.out.println("No saved data found in " + currentDataFile + ". Starting with empty records.");
                return;
            }
            
            // Check if file is empty
            if (file.length() == 0) {
                System.out.println("Data file is empty. Starting with empty records.");
                return;
            }
            
            // Check if we have unsaved data
            if (!journal.isEmpty()) {
                System.out.print("You have unsaved data. Save current data before loading? (yes/no): ");
                String saveChoice = sc.nextLine().trim();
                if (saveChoice.equalsIgnoreCase("yes")) {
                    saveData();
                }
            }
            
            Scanner fileScanner = new Scanner(file);
            journal.clear(); // Clear existing data
            ledger.clear(); // Clear ledger too
            
            int loadedCount = 0;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Transaction t = Transaction.fromFileString(line);
                    if (t != null) {
                        journal.add(t);
                        loadedCount++;
                    }
                }
            }
            
            fileScanner.close();
            
            if (loadedCount == 0) {
                System.out.println("No valid transactions found in the data file.");
            } else {
                System.out.println("Successfully loaded " + loadedCount + " transactions from " + currentDataFile);
                
                // Auto-post to ledger after loading
                System.out.print("Would you like to post transactions to ledger? (yes/no): ");
                String response = sc.nextLine();
                if (response.equalsIgnoreCase("yes")) {
                    postToLedger();
                }
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("Data file not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    static void addTransaction() {
        System.out.println("\n=== ADD NEW TRANSACTION ===");
        System.out.println("Type 'cancel' at any time to cancel this operation.");

        String date = "";
        boolean validDate = false;
        boolean cancelled = false;
        
        while (!validDate && !cancelled) {
            System.out.print("Date (e.g., Oct 1, 2024): ");
            date = sc.nextLine().trim();
            
            if (date.equalsIgnoreCase("cancel")) {
                cancelled = true;
                break;
            }
            
            String[] parts = date.split(" ");
            if (parts.length == 3) {
                String month = parts[0].toLowerCase();
                String dayStr = parts[1].replace(",", "");
                String yearStr = parts[2];
                
                int day = 0;
                int year = 0;

                try {
                    day = Integer.parseInt(dayStr);
                    year = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid day or year number. Try again.");
                    continue;
                }

                String[] months = {"jan","feb","mar","apr","may","jun","jul","aug","sep","sept","oct","nov","dec"};
                boolean validMonth = false;
                for (int i = 0; i < months.length; i++) {
                    if (month.startsWith(months[i])) {
                        validMonth = true;
                        break;
                    }
                }

                if (!validMonth) {
                    System.out.println("Invalid month. Please use Jan–Dec.");
                } else if (day < 1 || day > 31) {
                    System.out.println("Invalid day. Must be 1–31.");
                } else if (year < 1900 || year > 2100) {
                    System.out.println("Invalid year. Must be between 1900-2100.");
                } else {
                    validDate = true;
                }
            } else {
                System.out.println("Format: Mon DD, YYYY (e.g., Oct 1, 2024)");
            }
        }

        if (cancelled) {
            System.out.println("Transaction addition cancelled.");
            return;
        }

        System.out.print("Description: ");
        String desc = sc.nextLine();
        if (desc.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction addition cancelled.");
            return;
        }

        System.out.print("Debit Account: ");
        String debit = sc.nextLine();
        if (debit.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction addition cancelled.");
            return;
        }

        System.out.print("Credit Account: ");
        String credit = sc.nextLine();
        if (credit.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction addition cancelled.");
            return;
        }

        System.out.print("Amount: ");
        String amountInput = sc.nextLine();
        if (amountInput.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction addition cancelled.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Transaction addition cancelled.");
            return;
        }

        journal.add(new Transaction(date, desc, debit, credit, amount));
        System.out.println("Transaction added successfully.");
    }

    static void viewJournal() {
        if (journal.size() == 0) {
            System.out.println("\nNo journal entries found.");
            return;
        }

        System.out.println("\n=== JOURNAL ENTRIES ===");
        System.out.println("Current file: " + currentDataFile);
        for (int i = 0; i < journal.size(); i++) {
            Transaction t = journal.get(i);
            System.out.println((i + 1) + ". " + t.date + " " + t.description);
            System.out.println("   DR " + t.debitAccount + " " + t.amount);
            System.out.println("   CR " + t.creditAccount + " " + t.amount);
        }
    }

    static void editTransaction() {
        viewJournal();
        if (journal.size() == 0) return;

        System.out.print("\nEnter the number of the transaction to edit (or type 'cancel' to cancel): ");
        String input = sc.nextLine();
        
        if (input.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction edit cancelled.");
            return;
        }

        int num;
        try {
            num = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid transaction number. Edit cancelled.");
            return;
        }

        if (num < 1 || num > journal.size()) {
            System.out.println("Invalid transaction number. Edit cancelled.");
            return;
        }

        Transaction t = journal.get(num - 1);
        System.out.println("\nEditing Transaction #" + num);
        System.out.println("Type 'cancel' at any time to cancel editing, or leave blank to keep current value.");

        // Edit Date
        System.out.print("Current Date: " + t.date + " | New Date: ");
        String newDate = sc.nextLine();
        if (newDate.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction edit cancelled.");
            return;
        }
        if (!newDate.trim().isEmpty()) {
            boolean validDate = false;
            while (!validDate && !newDate.trim().isEmpty()) {
                String[] parts = newDate.split(" ");
                if (parts.length == 3) {
                    String month = parts[0].toLowerCase();
                    String dayStr = parts[1].replace(",", "");
                    String yearStr = parts[2];
                    
                    int day = 0;
                    int year = 0;

                    try {
                        day = Integer.parseInt(dayStr);
                        year = Integer.parseInt(yearStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid day or year number. Try again.");
                        System.out.print("New Date: ");
                        newDate = sc.nextLine();
                        if (newDate.equalsIgnoreCase("cancel")) {
                            System.out.println("Transaction edit cancelled.");
                            return;
                        }
                        continue;
                    }

                    String[] months = {"jan","feb","mar","apr","may","jun","jul","aug","sep","sept","oct","nov","dec"};
                    boolean validMonth = false;
                    for (int i = 0; i < months.length; i++) {
                        if (month.startsWith(months[i])) {
                            validMonth = true;
                            break;
                        }
                    }

                    if (!validMonth) {
                        System.out.println("Invalid month. Please use Jan–Dec.");
                        System.out.print("New Date: ");
                        newDate = sc.nextLine();
                        if (newDate.equalsIgnoreCase("cancel")) {
                            System.out.println("Transaction edit cancelled.");
                            return;
                        }
                    } else if (day < 1 || day > 31) {
                        System.out.println("Invalid day. Must be 1–31.");
                        System.out.print("New Date: ");
                        newDate = sc.nextLine();
                        if (newDate.equalsIgnoreCase("cancel")) {
                            System.out.println("Transaction edit cancelled.");
                            return;
                        }
                    } else if (year < 1900 || year > 2100) {
                        System.out.println("Invalid year. Must be between 1900-2100.");
                        System.out.print("New Date: ");
                        newDate = sc.nextLine();
                        if (newDate.equalsIgnoreCase("cancel")) {
                            System.out.println("Transaction edit cancelled.");
                            return;
                        }
                    } else {
                        validDate = true;
                        t.date = newDate;
                    }
                } else {
                    System.out.println("Format: Mon DD, YYYY (e.g., Oct 1, 2024)");
                    System.out.print("New Date: ");
                    newDate = sc.nextLine();
                    if (newDate.equalsIgnoreCase("cancel")) {
                        System.out.println("Transaction edit cancelled.");
                        return;
                    }
                }
            }
        }

        // Edit Description
        System.out.print("Current Description: " + t.description + " | New Description: ");
        String newDesc = sc.nextLine();
        if (newDesc.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction edit cancelled.");
            return;
        }
        if (!newDesc.trim().isEmpty()) t.description = newDesc;

        // Edit Debit Account
        System.out.print("Current Debit Account: " + t.debitAccount + " | New Debit Account: ");
        String newDebit = sc.nextLine();
        if (newDebit.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction edit cancelled.");
            return;
        }
        if (!newDebit.trim().isEmpty()) t.debitAccount = newDebit;

        // Edit Credit Account
        System.out.print("Current Credit Account: " + t.creditAccount + " | New Credit Account: ");
        String newCredit = sc.nextLine();
        if (newCredit.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction edit cancelled.");
            return;
        }
        if (!newCredit.trim().isEmpty()) t.creditAccount = newCredit;

        // Edit Amount
        System.out.print("Current Amount: " + t.amount + " | New Amount: ");
        String newAmt = sc.nextLine();
        if (newAmt.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction edit cancelled.");
            return;
        }
        if (!newAmt.trim().isEmpty()) {
            try {
                t.amount = Double.parseDouble(newAmt);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Amount not changed.");
            }
        }

        System.out.println("Transaction updated successfully!");
    }

    static void removeTransaction() {
        viewJournal();
        if (journal.size() == 0) return;

        System.out.print("\nEnter transaction number to remove (or type 'cancel' to cancel): ");
        String input = sc.nextLine();
        
        if (input.equalsIgnoreCase("cancel")) {
            System.out.println("Transaction removal cancelled.");
            return;
        }

        int num;
        try {
            num = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Removal cancelled.");
            return;
        }

        if (num < 1 || num > journal.size()) {
            System.out.println("Invalid number. Removal cancelled.");
            return;
        }

        Transaction t = journal.remove(num - 1);
        System.out.println("Removed transaction: " + t.description);
    }

    static void postToLedger() {
        if (journal.size() == 0) {
            System.out.println("\nNo transactions to post.");
            return;
        }

        ledger.clear();

        for (int i = 0; i < journal.size(); i++) {
            Transaction t = journal.get(i);
            if (!ledger.containsKey(t.debitAccount))
                ledger.put(t.debitAccount, new Account(t.debitAccount));
            if (!ledger.containsKey(t.creditAccount))
                ledger.put(t.creditAccount, new Account(t.creditAccount));

            ledger.get(t.debitAccount).postDebit(t.amount);
            ledger.get(t.creditAccount).postCredit(t.amount);
        }

        System.out.println("\n=== T-ACCOUNTS LEDGER ===");
        for (String name : ledger.keySet()) {
            Account acc = ledger.get(name);
            System.out.println("\nAccount: " + acc.name);
            System.out.println("Debit       | Credit");
            System.out.println("----------------------");

            int rows = Math.max(acc.debits.size(), acc.credits.size());
            for (int i = 0; i < rows; i++) {
                String d = i < acc.debits.size() ? String.format("%.2f", acc.debits.get(i)) : "";
                String c = i < acc.credits.size() ? String.format("%.2f", acc.credits.get(i)) : "";
                System.out.printf("%-10s | %-10s%n", d, c);
            }

            double debitBal = acc.getDebitBalance();
            double creditBal = acc.getCreditBalance();

            if (debitBal > 0)
                System.out.println("Ending Debit Balance: " + debitBal);
            else if (creditBal > 0)
                System.out.println("Ending Credit Balance: " + creditBal);
            else
                System.out.println("Balance: 0.00");
        }
    }

    static void viewTrialBalance() {
        if (ledger.size() == 0) {
            System.out.println("\nPlease post to ledger first.");
            return;
        }

        System.out.println("\n=== TRIAL BALANCE ===");
        System.out.println("Current file: " + currentDataFile);
        double totalDebit = 0;
        double totalCredit = 0;

        for (String name : ledger.keySet()) {
            Account acc = ledger.get(name);
            double d = acc.getDebitBalance();
            double c = acc.getCreditBalance();
            System.out.printf("%-20s Debit: %10.2f | Credit: %10.2f%n", acc.name, d, c);
            totalDebit += d;
            totalCredit += c;
        }

        System.out.println("---------------------------------------------");
        System.out.printf("TOTAL DEBIT:  %.2f%n", totalDebit);
        System.out.printf("TOTAL CREDIT: %.2f%n", totalCredit);

        if (Math.abs(totalDebit - totalCredit) < 0.01)
            System.out.println("Trial Balance is BALANCED.");
        else
            System.out.println("Not Balanced. Difference: " + Math.abs(totalDebit - totalCredit));
    }

    static void viewFinancialStatements() {
        if (ledger.size() == 0) {
            System.out.println("\nPlease post to ledger first.");
            return;
        }

        double revenue = 0;
        double expenses = 0;
        double assets = 0;
        double liabilities = 0;
        double equity = 0;

        for (String name : ledger.keySet()) {
            Account acc = ledger.get(name);
            String type = acc.getType();

            if (type.equals("Revenue"))
                revenue += acc.getCreditBalance();
            else if (type.equals("Expense"))
                expenses += acc.getDebitBalance();
            else if (type.equals("Asset"))
                assets += acc.getDebitBalance();
            else if (type.equals("Liability"))
                liabilities += acc.getCreditBalance();
            else if (type.equals("Equity"))
                equity += acc.getCreditBalance();
        }

        double netIncome = revenue - expenses;
        double totalEquity = equity + netIncome;
        double rightSide = liabilities + totalEquity;

        System.out.println("\n=== FINANCIAL STATEMENTS ===");
        System.out.println("Current file: " + currentDataFile);
        System.out.println("\n--- INCOME STATEMENT ---");
        System.out.println("Service Revenue: " + revenue);
        System.out.println("Total Expenses:  " + expenses);
        System.out.println("Net Income:      " + netIncome);

        System.out.println("\n--- BALANCE SHEET ---");
        System.out.println("Total Assets:          " + assets);
        System.out.println("Total Liabilities:     " + liabilities);
        System.out.println("Owner's Equity:        " + totalEquity);
        System.out.println("Liabilities + Equity:  " + rightSide);

        if (Math.abs(assets - rightSide) < 0.01)
            System.out.println("Balance Sheet is BALANCED.");
        else
            System.out.println("Not Balanced. Difference: " + Math.abs(assets - rightSide));
    }
}