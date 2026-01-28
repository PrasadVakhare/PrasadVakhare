import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GenerateTestToken {
    public static void main(String[] args) {
        String jwtSecret = "mySecretKeyForJWTTokenGenerationAndValidationMustBeLongEnough";
        long jwtExpiration = 86400000; // 24 hours
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // Generate tokens for different users
        System.out.println("=== JWT Tokens for Testing ===\n");
        
        // User token
        String userToken = generateToken(key, "user123", Arrays.asList("USER"), jwtExpiration);
        System.out.println("USER Token (user123):");
        System.out.println(userToken);
        System.out.println();
        
        // Manager token
        String managerToken = generateToken(key, "manager456", Arrays.asList("MANAGER", "APPROVER"), jwtExpiration);
        System.out.println("MANAGER Token (manager456):");
        System.out.println(managerToken);
        System.out.println();
        
        // Finance token
        String financeToken = generateToken(key, "finance789", Arrays.asList("FINANCE", "APPROVER"), jwtExpiration);
        System.out.println("FINANCE Token (finance789):");
        System.out.println(financeToken);
        System.out.println();
        
        // Admin token
        String adminToken = generateToken(key, "admin999", Arrays.asList("ADMIN", "APPROVER"), jwtExpiration);
        System.out.println("ADMIN Token (admin999):");
        System.out.println(adminToken);
        System.out.println();
    }
    
    private static String generateToken(SecretKey key, String userId, List<String> roles, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .subject(userId)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
}
