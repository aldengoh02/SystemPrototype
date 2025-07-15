package com.bookstore.db;

public class testDB {
    public static void main(String[] args) {
        // Test BookDatabase
        BookDatabase bookDb = new BookDatabase();
        if (bookDb.connectDb()) {
            System.out.println("Connected to Book database.");

            String result = bookDb.loadResults();
            System.out.println(result);
            for (BookRecords book : bookDb.getResults()) {
                String status = book.isComingSoon() ? " (Coming Soon)" : " (Available)";
                System.out.println(
                        book.getId() + ": " + book.getTitle() + " by " + book.getAuthor() +
                                " - $" + book.getSellingPrice() + " [" + book.getCategory() + "]" + status
                );
            }

            bookDb.disconnectDb();
            System.out.println("Disconnected from Book database.");
        } else {
            System.out.println("Failed to connect to Book database.");
        }

        System.out.println("------------------------------------------");

        // Test UserDatabase
        UserDatabase userDb = new UserDatabase();
        if (userDb.connectDb()) {
            System.out.println("Connected to User database.");

            String userResult = userDb.loadResults();
            System.out.println(userResult);
            for (UserRecords user : userDb.getResults()) {
                System.out.println(
                        user.getUserID() + ": " + user.getFirstName() + " " + user.getLastName() +
                                " (" + user.getEmail() + "), Status: " + user.getStatus() +
                                ", User Type ID: " + user.getUserTypeID() +
                                ", Enrolled for Promotions: " + user.isEnrollForPromotions()
                );
            }

            userDb.disconnectDb();
            System.out.println("Disconnected from User database.");
        } else {
            System.out.println("Failed to connect to User database.");
        }

        System.out.println("------------------------------------------");

        // Test ShippingAddressDatabase
        ShippingAddressDatabase shippingDb = new ShippingAddressDatabase();
        if (shippingDb.connectDb()) {
            System.out.println("Connected to Shipping Address database.");

            String shippingResult = shippingDb.loadResults();
            System.out.println(shippingResult);
            for (ShippingAddressRecords addr : shippingDb.getResults()) {
                System.out.println(
                        addr.getAddressID() + ": UserID " + addr.getUserID() + ", " +
                                addr.getStreet() + ", " + addr.getCity() + ", " + addr.getState() + " " + addr.getZipCode()
                );
            }

            shippingDb.disconnectDb();
            System.out.println("Disconnected from Shipping Address database.");
        } else {
            System.out.println("Failed to connect to Shipping Address database.");
        }

        System.out.println("------------------------------------------");

        // Test PaymentCardDatabase
        PaymentCardDatabase paymentDb = new PaymentCardDatabase();
        if (paymentDb.connectDb()) {
            System.out.println("Connected to Payment Card database.");

            String paymentResult = paymentDb.loadResults();
            System.out.println(paymentResult);
            for (PaymentCardRecords card : paymentDb.getResults()) {
                System.out.println(
                        "CardNo: " + card.getCardNo() + ", UserID: " + card.getUserID() +
                                ", Type: " + card.getType() + ", Expiration: " + card.getExpirationDate() +
                                ", BillingAddressID: " + card.getBillingAddressID()
                );
            }

            paymentDb.disconnectDb();
            System.out.println("Disconnected from Payment Card database.");
        } else {
            System.out.println("Failed to connect to Payment Card database.");
        }
    }
}
