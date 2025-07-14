package test;

import com.bookstore.SecUtils;

/*
 * Testing class to test if encryption and decryption as implemented
 * in SecUtils works as expected
 * Is run via script in scripts folder (testEncryption.sh)
 */

public class EncryptionTest {
    
    public static void main(String[] args) {
        System.out.println("Testing encryption...");
        
        // some fake card numbers to test with
        String[] cards = {
            "4111111111111111",
            "5555555555554444", 
            "378282246310005"
        };
        
        boolean works = true;
        
        for(int i = 0; i < cards.length; i++) {
            String card = cards[i];
            System.out.println("\nTest " + (i+1) + ":");
            System.out.println("Card: " + hide(card));
            
            try {
                String enc = SecUtils.encryptCreditCard(card);
                System.out.println("Encrypted: " + enc.substring(0, Math.min(enc.length(), 15)) + "...");
                
                String dec = SecUtils.decryptCreditCard(enc);
                System.out.println("Decrypted: " + hide(dec));
                
                if(card.equals(dec)) {
                    System.out.println("OK");
                } else {
                    System.out.println("FAILED - doesn't match");
                    works = false;
                }
                
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                works = false;
                
                if(e.getMessage().contains("Encryption secret key and salt must be set")) {
                    System.out.println("hint: run the key generator script first");
                }
            }
        }
        
        System.out.println("\n" + (works ? "All tests passed!" : "Something's broken"));
    }
    
    // hide most of the card number 
    private static String hide(String card) {
        if(card == null || card.length() < 4) return "****";
        return "****-****-****-" + card.substring(card.length()-4);
    }
} 
