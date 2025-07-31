package com.bookstore.db;

/**
 * Standalone test for the factory pattern concept.
 * This test demonstrates the factory pattern without depending on database classes.
 */
public class StandaloneFactoryTest {
    
    // Mock interface for testing
    public interface MockDatabaseInterface {
        String getType();
        boolean connect();
        boolean disconnect();
    }
    
    // Mock database classes for testing
    public static class MockBookDatabase implements MockDatabaseInterface {
        @Override
        public String getType() { return "BookDatabase"; }
        @Override
        public boolean connect() { return true; }
        @Override
        public boolean disconnect() { return true; }
    }
    
    public static class MockUserDatabase implements MockDatabaseInterface {
        @Override
        public String getType() { return "UserDatabase"; }
        @Override
        public boolean connect() { return true; }
        @Override
        public boolean disconnect() { return true; }
    }
    
    public static class MockCartDatabase implements MockDatabaseInterface {
        @Override
        public String getType() { return "CartDatabase"; }
        @Override
        public boolean connect() { return true; }
        @Override
        public boolean disconnect() { return true; }
    }
    
    // Mock factory for testing
    public static class MockDatabaseFactory {
        
        public enum DatabaseType {
            BOOK,
            USER,
            CART
        }
        
        public static MockDatabaseInterface createDatabase(DatabaseType type) {
            switch (type) {
                case BOOK:
                    return new MockBookDatabase();
                case USER:
                    return new MockUserDatabase();
                case CART:
                    return new MockCartDatabase();
                default:
                    throw new IllegalArgumentException("Unsupported database type: " + type);
            }
        }
        
        public static MockDatabaseInterface createDatabase(String typeString) {
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
        
        public static DatabaseType[] getSupportedTypes() {
            return DatabaseType.values();
        }
        
        public static boolean isSupported(DatabaseType type) {
            for (DatabaseType supportedType : DatabaseType.values()) {
                if (supportedType == type) {
                    return true;
                }
            }
            return false;
        }
        
        public static boolean isSupported(String typeString) {
            try {
                DatabaseType.valueOf(typeString.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Standalone Factory Pattern Test ===\n");
        
        // Test 1: Create databases using enum types
        System.out.println("Test 1: Creating databases using enum types");
        testEnumCreation();
        
        // Test 2: Create databases using string types
        System.out.println("\nTest 2: Creating databases using string types");
        testStringCreation();
        
        // Test 3: Test unsupported types
        System.out.println("\nTest 3: Testing unsupported types");
        testUnsupportedTypes();
        
        // Test 4: Test factory utility methods
        System.out.println("\nTest 4: Testing factory utility methods");
        testUtilityMethods();
        
        // Test 5: Test interface compliance
        System.out.println("\nTest 5: Testing interface compliance");
        testInterfaceCompliance();
        
        System.out.println("\n=== All tests completed ===");
    }
    
    private static void testEnumCreation() {
        try {
            MockDatabaseInterface bookDb = MockDatabaseFactory.createDatabase(MockDatabaseFactory.DatabaseType.BOOK);
            System.out.println("✓ Created BookDatabase: " + bookDb.getType());
            
            MockDatabaseInterface userDb = MockDatabaseFactory.createDatabase(MockDatabaseFactory.DatabaseType.USER);
            System.out.println("✓ Created UserDatabase: " + userDb.getType());
            
            MockDatabaseInterface cartDb = MockDatabaseFactory.createDatabase(MockDatabaseFactory.DatabaseType.CART);
            System.out.println("✓ Created CartDatabase: " + cartDb.getType());
            
        } catch (Exception e) {
            System.err.println("✗ Error in enum creation test: " + e.getMessage());
        }
    }
    
    private static void testStringCreation() {
        try {
            MockDatabaseInterface bookDb = MockDatabaseFactory.createDatabase("BOOK");
            System.out.println("✓ Created BookDatabase using string: " + bookDb.getType());
            
            MockDatabaseInterface userDb = MockDatabaseFactory.createDatabase("user"); // Test case insensitive
            System.out.println("✓ Created UserDatabase using lowercase string: " + userDb.getType());
            
            MockDatabaseInterface cartDb = MockDatabaseFactory.createDatabase("CART");
            System.out.println("✓ Created CartDatabase using string: " + cartDb.getType());
            
        } catch (Exception e) {
            System.err.println("✗ Error in string creation test: " + e.getMessage());
        }
    }
    
    private static void testUnsupportedTypes() {
        try {
            MockDatabaseFactory.createDatabase("UNSUPPORTED_TYPE");
            System.err.println("✗ Should have thrown exception for unsupported string type");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly handled unsupported string type: " + e.getMessage());
        }
        
        try {
            MockDatabaseFactory.createDatabase((String) null);
            System.err.println("✗ Should have thrown exception for null string");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly handled null string: " + e.getMessage());
        }
    }
    
    private static void testUtilityMethods() {
        MockDatabaseFactory.DatabaseType[] types = MockDatabaseFactory.getSupportedTypes();
        System.out.println("✓ Found " + types.length + " supported database types");
        
        boolean isBookSupported = MockDatabaseFactory.isSupported(MockDatabaseFactory.DatabaseType.BOOK);
        System.out.println("✓ BOOK type supported: " + isBookSupported);
        
        boolean isUserSupported = MockDatabaseFactory.isSupported("USER");
        System.out.println("✓ USER string supported: " + isUserSupported);
        
        boolean isUnsupported = MockDatabaseFactory.isSupported("UNSUPPORTED");
        System.out.println("✓ UNSUPPORTED string supported: " + isUnsupported);
    }
    
    private static void testInterfaceCompliance() {
        try {
            MockDatabaseInterface bookDb = MockDatabaseFactory.createDatabase(MockDatabaseFactory.DatabaseType.BOOK);
            
            boolean connectResult = bookDb.connect();
            System.out.println("✓ Interface method connect() works: " + connectResult);
            
            boolean disconnectResult = bookDb.disconnect();
            System.out.println("✓ Interface method disconnect() works: " + disconnectResult);
            
            String type = bookDb.getType();
            System.out.println("✓ Interface method getType() works: " + type);
            
        } catch (Exception e) {
            System.err.println("✗ Error in interface compliance test: " + e.getMessage());
        }
    }
} 