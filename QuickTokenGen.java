import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Arrays;

public class QuickTokenGen {
    public static void main(String[] args) {
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date exp = new Date(now.getTime() + 86400000L);
        
        System.out.println(Jwts.builder().subject("user123").claim("roles", Arrays.asList("USER")).issuedAt(now).expiration(exp).signWith(key).compact());
        System.out.println(Jwts.builder().subject("manager456").claim("roles", Arrays.asList("MANAGER", "APPROVER")).issuedAt(now).expiration(exp).signWith(key).compact());
        System.out.println(Jwts.builder().subject("finance789").claim("roles", Arrays.asList("FINANCE", "APPROVER")).issuedAt(now).expiration(exp).signWith(key).compact());
        System.out.println(Jwts.builder().subject("admin999").claim("roles", Arrays.asList("ADMIN", "APPROVER")).issuedAt(now).expiration(exp).signWith(key).compact());
    }
}
