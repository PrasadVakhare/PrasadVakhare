import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Arrays;

public class GenerateTokens {
    public static void main(String[] args) {
        String jwtSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        long jwtExpiration = 86400000L; // 24 hours
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        System.out.println("\n=== JWT Tokens for Testing ===\n");
        
        // User token
        String userToken = Jwts.builder()
                .subject("user123")
                .claim("roles", Arrays.asList("USER"))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        System.out.println("USER Token (user123):");
        System.out.println(userToken);
        System.out.println();
        
        // Manager token
        String managerToken = Jwts.builder()
                .subject("manager456")
                .claim("roles", Arrays.asList("MANAGER", "APPROVER"))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        System.out.println("MANAGER Token (manager456):");
        System.out.println(managerToken);
        System.out.println();
        
        // Finance token
        String financeToken = Jwts.builder()
                .subject("finance789")
                .claim("roles", Arrays.asList("FINANCE", "APPROVER"))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        System.out.println("FINANCE Token (finance789):");
        System.out.println(financeToken);
        System.out.println();
        
        // Admin token
        String adminToken = Jwts.builder()
                .subject("admin999")
                .claim("roles", Arrays.asList("ADMIN", "APPROVER"))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        System.out.println("ADMIN Token (admin999):");
        System.out.println(adminToken);
        System.out.println();
    }
}
