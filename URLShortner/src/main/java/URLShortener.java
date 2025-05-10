import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class URLShortener {
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;

    // In-memory DB simulation
    private final HashMap<String, String> urlDatabase = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    // Base62 encoding
    private String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62.charAt((int) (id % BASE)));
            id /= BASE;
        }
        return sb.reverse().toString();
    }

    // Base62 decoding
    private long decode(String shortURL) {
        long id = 0;
        for (int i = 0; i < shortURL.length(); i++) {
            id = id * BASE + BASE62.indexOf(shortURL.charAt(i));
        }
        return id;
    }

    // Hashing with SHA-256
    private String generateHash(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    // Shorten URL - Combining Auto-increment, Hash, and Base62
    public String shortenURL(String longURL) {
        long id = idCounter.getAndIncrement();
        String shortHash = generateHash(longURL).substring(0, 5); // Get the first 5 chars of the hash
        String shortURL = encode(id);
        urlDatabase.put(shortURL, longURL);
        return "https://short.ly/" + shortURL;
    }

    // Retrieve URL
    public String retrieveURL(String shortURL) {
        String key = shortURL.replace("https://short.ly/", "");
        return urlDatabase.getOrDefault(key, "URL not found");
    }

    // Main for testing
    public static void main(String[] args) {
        URLShortener shortener = new URLShortener();
        String shortUrl = shortener.shortenURL("https://example.com/my-long-url");
        System.out.println("Short URL: " + shortUrl);
        System.out.println("Original URL: " + shortener.retrieveURL(shortUrl));
    }
}
