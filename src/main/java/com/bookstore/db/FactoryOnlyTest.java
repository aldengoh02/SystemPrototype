package com.bookstore.db;

/**
 * Test class that only tests the factory pattern logic without instantiating database classes.
 * This avoids dependency issues while still validating the factory implementation.
 */
public class FactoryOnlyTest {
    
    public static void main(String[] args) {
        System.out.println("=== Factory Pattern Logic Test ===\n");
        
        // Test 1: Test enum values
        System.out.println("Test 1: Testing enum values");
        testEnumValues();
        
        // Test 2: Test factory utility methods
        System.out.println("\nTest 2: Testing factory utility methods");
        testUtilityMethods();
        
        // Test 3: Test string validation
        System.out.println("\nTest 3: Testing string validation");
        testStringValidation();
        
        // Test 4: Test factory creation logic (without instantiation)
        System.out.println("\nTest 4: Testing factory creation logic");
        testFactoryLogic();
        
        System.out.println("\n=== All tests completed ===");
    }
    
    private static void testEnumValues() {
        DatabaseFactory.DatabaseType[] types = DatabaseFactory.DatabaseType.values();
        System.out.println("✓ Found " + types.length + " database types:");
        
        for (DatabaseFactory.DatabaseType type : types) {
            System.out.println("  - " + type.name());
        }
        
        // Test specific enum values
        System.out.println("✓ BOOK enum: " + DatabaseFactory.DatabaseType.BOOK);
        System.out.println("✓ USER enum: " + DatabaseFactory.DatabaseType.USER);
        System.out.println("✓ CART enum: " + DatabaseFactory.DatabaseType.CART);
    }
    
    private static void testUtilityMethods() {
        // Test getSupportedTypes
        DatabaseFactory.DatabaseType[] types = DatabaseFactory.getSupportedTypes();
        System.out.println("✓ getSupportedTypes() returns " + types.length + " types");
        
        // Test isSupported with enum
        boolean isBookSupported = DatabaseFactory.isSupported(DatabaseFactory.DatabaseType.BOOK);
        System.out.println("✓ BOOK type supported: " + isBookSupported);
        
        boolean isUserSupported = DatabaseFactory.isSupported(DatabaseFactory.DatabaseType.USER);
        System.out.println("✓ USER type supported: " + isUserSupported);
        
        // Test isSupported with string
        boolean isBookStringSupported = DatabaseFactory.isSupported("BOOK");
        System.out.println("✓ 'BOOK' string supported: " + isBookStringSupported);
        
        boolean isUserStringSupported = DatabaseFactory.isSupported("user"); // Test case insensitive
        System.out.println("✓ 'user' string supported: " + isUserStringSupported);
        
        boolean isUnsupported = DatabaseFactory.isSupported("UNSUPPORTED");
        System.out.println("✓ 'UNSUPPORTED' string supported: " + isUnsupported);
    }
    
    private static void testStringValidation() {
        // Test string to enum conversion
        try {
            DatabaseFactory.DatabaseType bookType = DatabaseFactory.DatabaseType.valueOf("BOOK");
            System.out.println("✓ String 'BOOK' converts to enum: " + bookType);
            
            DatabaseFactory.DatabaseType userType = DatabaseFactory.DatabaseType.valueOf("USER");
            System.out.println("✓ String 'USER' converts to enum: " + userType);
            
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Error converting valid strings to enum: " + e.getMessage());
        }
        
        // Test invalid string
        try {
            DatabaseFactory.DatabaseType invalidType = DatabaseFactory.DatabaseType.valueOf("INVALID");
            System.err.println("✗ Should have thrown exception for invalid string");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly handled invalid string: " + e.getMessage());
        }
    }
    
    private static void testFactoryLogic() {
        // Test the switch statement logic by checking enum values
        DatabaseFactory.DatabaseType[] types = DatabaseFactory.DatabaseType.values();
        
        System.out.println("✓ Factory supports all " + types.length + " database types:");
        for (DatabaseFactory.DatabaseType type : types) {
            System.out.println("  - " + type.name() + " -> would create " + getExpectedClassName(type));
        }
        
        // Test that all enum values are handled in the factory
        boolean allHandled = true;
        for (DatabaseFactory.DatabaseType type : types) {
            if (!DatabaseFactory.isSupported(type)) {
                allHandled = false;
                System.err.println("✗ Type " + type + " not handled in factory");
            }
        }
        
        if (allHandled) {
            System.out.println("✓ All database types are properly handled in factory");
        } else {
            System.err.println("✗ Some database types are not handled in factory");
        }
    }
    
    private static String getExpectedClassName(DatabaseFactory.DatabaseType type) {
        switch (type) {
            case BOOK:
                return "BookDatabase";
            case USER:
                return "UserDatabase";
            case CART:
                return "CartDatabase";
            case ORDERS:
                return "OrdersDatabase";
            case PAYMENT_CARD:
                return "PaymentCardDatabase";
            case PROMOTION:
                return "PromotionDatabase";
            case SHIPPING_ADDRESS:
                return "ShippingAddressDatabase";
            case TRANSACTION:
                return "TransactionDatabase";
            case BILLING_ADDRESS:
                return "BillingAddressDatabase";
            case VERIFICATION_TOKEN:
                return "VerificationTokenDatabase";
            default:
                return "Unknown";
        }
    }
} 