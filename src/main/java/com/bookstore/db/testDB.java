package com.bookstore.db;

import com.bookstore.records.BookRecords;
import com.bookstore.records.UserRecords;
import com.bookstore.records.ShippingAddressRecords;
import com.bookstore.records.PaymentCardRecords;
import com.bookstore.records.PromotionRecords;
import com.bookstore.records.OrdersRecords;
import com.bookstore.records.TransactionRecords;
import com.bookstore.records.CartRecord;

public class testDB {
    public static void main(String[] args) {
        // Test BookDatabase
        BookDatabase bookDb = new BookDatabase();
        if (bookDb.connectDb()) {
            System.out.println("Connected to Book database.");

            String result = bookDb.loadResults();
            System.out.println(result);
            System.out.println("Books in database: ");
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
            System.out.println("\nUsers in database: ");
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
            System.out.println("\nShipping Addresses in database: ");
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
            System.out.println("\nPayment Cards in database: ");
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

        System.out.println("------------------------------------------");

// Test PromotionDatabase
        PromotionDatabase promoDb = new PromotionDatabase();
        if (promoDb.connectDb()) {
            System.out.println("Connected to Promotion database.");

            String promoResult = promoDb.loadResults();
            System.out.println(promoResult);
            System.out.println("\nPromotions in database: ");
            for (PromotionRecords promo : promoDb.getResults()) {
                System.out.println(
                        promo.getPromoID() + ": " + promo.getPromoCode() + ", Discount: " + promo.getDiscount() + "%" +
                                ", Start Date: " + promo.getStartDate() + ", End Date: " + promo.getEndDate()
                );
            }

            promoDb.disconnectDb();
            System.out.println("Disconnected from Promotion database.");
        } else {
            System.out.println("Failed to connect to Promotion database.");
        }


        System.out.println("------------------------------------------");

        // Test OrdersDatabase
        OrdersDatabase ordersDb = new OrdersDatabase();
        if (ordersDb.connectDb()) {
            System.out.println("Connected to Orders database.");

            String ordersResult = ordersDb.loadResults();
            System.out.println(ordersResult);
            System.out.println("\nOrders in database: ");
            for (OrdersRecords order : ordersDb.getResults()) {
                System.out.println(
                        "OrderID: " + order.getOrderID() + ", UserID: " + order.getUserID() +
                                ", CardID: " + order.getCardID() + ", PromoID: " + order.getPromoID() +
                                ", GrandTotal: $" + order.getGrandTotal() + ", Date: " + order.getOrderDateTime()
                );
            }

            ordersDb.disconnectDb();
            System.out.println("Disconnected from Orders database.");
        } else {
            System.out.println("Failed to connect to Orders database.");
        }

        System.out.println("------------------------------------------");

        // Test TransactionDatabase
        TransactionDatabase transactionDb = new TransactionDatabase();
        if (transactionDb.connectDb()) {
            System.out.println("Connected to Transaction database.");

            String transactionResult = transactionDb.loadResults();
            System.out.println(transactionResult);
            System.out.println("\nTransactions in database: ");
            for (TransactionRecords transaction : transactionDb.getResults()) {
                System.out.println(
                        "TransactionID: " + transaction.getTransactionID() +
                                ", OrderID: " + transaction.getOrderID() +
                                ", BookID: " + transaction.getBookID() +
                                ", Quantity: " + transaction.getQuantity()
                );
            }

            transactionDb.disconnectDb();
            System.out.println("Disconnected from Transaction database.");
        } else {
            System.out.println("Failed to connect to Transaction database.");
        }

        System.out.println("------------------------------------------");

        // Test CartDatabase
        CartDatabase cartDb = new CartDatabase();
        if (cartDb.connectDb()) {
            System.out.println("Connected to Cart database.");

            String cartResult = cartDb.loadResults();
            System.out.println(cartResult);
            System.out.println("\nCart items in database: ");
            for (CartRecord cart : cartDb.getResults()) {
                System.out.println(
                        "UserID: " + cart.getUserID() +
                                ", BookID: " + cart.getBookID() +
                                ", Quantity: " + cart.getQuantity()
                );
            }

            cartDb.disconnectDb();
            System.out.println("Disconnected from Cart database.");
        } else {
            System.out.println("Failed to connect to Cart database.");
        }

        System.out.println("------------------------------------------");

        // Test VerificationTokenDatabase
        VerificationTokenDatabase tokenDb = new VerificationTokenDatabase();
        if (tokenDb.connectDb()) {
            System.out.println("Connected to VerificationToken database.");

            String tokenResult = tokenDb.loadResults();
            System.out.println(tokenResult);
            for (VerificationTokenRecords token : tokenDb.getResults()) {
                System.out.println(
                        "TokenID: " + token.getTokenId() +
                                ", UserID: " + token.getUserId() +
                                ", Token: " + token.getToken() +
                                ", Expiry: " + token.getExpiryDate() +
                                ", Type: " + token.getTokenType()
                );
            }

            tokenDb.disconnectDb();
            System.out.println("Disconnected from VerificationToken database.");
        } else {
            System.out.println("Failed to connect to VerificationToken database.");
        }

    }
}


