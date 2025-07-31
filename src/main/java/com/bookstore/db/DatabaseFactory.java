package com.bookstore.db;

/**
 * Factory class for creating database instances.
 * This factory centralizes the creation of database objects and provides
 * a consistent way to instantiate different database types.
 */
public class DatabaseFactory {
    
    /**
     * Database types supported by the factory
     * Open to extension whenever needed
     */
    public enum DatabaseType {
        BOOK,
        USER,
        CART,
        ORDERS,
        PAYMENT_CARD,
        PROMOTION,
        SHIPPING_ADDRESS,
        TRANSACTION,
        BILLING_ADDRESS,
        VERIFICATION_TOKEN
    }
    
    /**
     * Create a database instance based on the specified type
     * @param type the type of database to create
     * @return the database instance implementing DatabaseInterface
     * @throws IllegalArgumentException if the database type is not supported
     */
    public static DatabaseInterface createDatabase(DatabaseType type) {
        switch (type) {
            case BOOK:
                return new BookDatabase();
            case USER:
                return new UserDatabase();
            case CART:
                return new CartDatabase();
            case ORDERS:
                return new OrdersDatabase();
            case PAYMENT_CARD:
                return new PaymentCardDatabase();
            case PROMOTION:
                return new PromotionDatabase();
            case SHIPPING_ADDRESS:
                return new ShippingAddressDatabase();
            case TRANSACTION:
                return new TransactionDatabase();
            case BILLING_ADDRESS:
                return new BillingAddressDatabase();
            case VERIFICATION_TOKEN:
                return new VerificationTokenDatabase();
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
    
    /**
     * Create a database instance using a string identifier
     * @param typeString the string representation of the database type
     * @return the database instance implementing DatabaseInterface
     * @throws IllegalArgumentException if the database type string is not recognized
     */
    public static DatabaseInterface createDatabase(String typeString) {
        if (typeString == null) {
            throw new IllegalArgumentException("Database type string cannot be null");
        }
        try {
            DatabaseType type = DatabaseType.valueOf(typeString.toUpperCase());
            return createDatabase(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported database type: " + typeString);
        }
    }
    
    /**
     * Get all supported database types
     * @return array of all supported database types
     */
    public static DatabaseType[] getSupportedTypes() {
        return DatabaseType.values();
    }
    
    /**
     * Check if a database type is supported
     * @param type the database type to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(DatabaseType type) {
        for (DatabaseType supportedType : DatabaseType.values()) {
            if (supportedType == type) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if a database type string is supported
     * @param typeString the database type string to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(String typeString) {
        try {
            DatabaseType.valueOf(typeString.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 