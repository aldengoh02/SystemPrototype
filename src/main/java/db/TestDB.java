public class TestDB {
    public static void main(String[] args) {
        BookDatabase bookDb = new BookDatabase();

        // Connect to DB
        if (bookDb.connectDb()) {
            System.out.println("Connected to database.");

            // Load and print book records
            String result = bookDb.loadResults();
            System.out.println(result);
            for (BookRecords book : bookDb.getResults()) {
                System.out.println(
                        book.getId() + ": " + book.getTitle() + " by " + book.getAuthor() +
                                " - $" + book.getSellingPrice() + " [" + book.getCategory() + "]"
                );
            }

            // Disconnect from DB
            bookDb.disconnectDb();
            System.out.println("Disconnected from database.");
        } else {
            System.out.println("Failed to connect to database.");
        }
    }
}