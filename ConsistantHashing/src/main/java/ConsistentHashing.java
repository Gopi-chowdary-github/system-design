import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ConsistentHashing {
    // TreeMap to store the hash ring
    private final SortedMap<Integer, String> hashRing = new TreeMap<>();
    // Number of virtual nodes per physical node
    private final int virtualNodeCount;
    // Hashing algorithm
    private static final String HASH_ALGORITHM = "MD5";

    // Constructor to initialize with virtual nodes
    public ConsistentHashing(List<String> nodes, int virtualNodeCount) {
        this.virtualNodeCount = virtualNodeCount;
        for (String node : nodes) {
            addNode(node);
        }
    }

    // Add a node with virtual nodes
    public void addNode(String node) {
        for (int i = 0; i < virtualNodeCount; i++) {
            int hash = hash(node + "-VN" + i);
            hashRing.put(hash, node);
            System.out.println("Virtual Node [" + node + "-VN" + i + "] added at position " + hash);
        }
    }

    // Remove a node and its virtual nodes
    public void removeNode(String node) {
        for (int i = 0; i < virtualNodeCount; i++) {
            int hash = hash(node + "-VN" + i);
            hashRing.remove(hash);
            System.out.println("Virtual Node [" + node + "-VN" + i + "] removed from position " + hash);
        }
    }

    // Find the node responsible for the given key
    public String getNode(String key) {
        if (hashRing.isEmpty()) {
            return null;
        }
        int hash = hash(key);
        // Find the nearest node clockwise
        SortedMap<Integer, String> tailMap = hashRing.tailMap(hash);
        hash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        return hashRing.get(hash);
    }

    // Hash function (MD5-based hashing)
    private int hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(key.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return Math.abs(Arrays.hashCode(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        List<String> nodes = Arrays.asList("NodeA", "NodeB", "NodeC");
        ConsistentHashing consistentHashing = new ConsistentHashing(nodes, 5);

        System.out.println("\n--- Data Mapping ---");
        for (int i = 1; i <= 10; i++) {
            String key = "Key" + i;
            System.out.println(key + " is mapped to " + consistentHashing.getNode(key));
        }

        System.out.println("\n--- Adding NodeD ---");
        consistentHashing.addNode("NodeD");

        System.out.println("\n--- Data Mapping After Adding NodeD ---");
        for (int i = 1; i <= 10; i++) {
            String key = "Key" + i;
            System.out.println(key + " is mapped to " + consistentHashing.getNode(key));
        }

        System.out.println("\n--- Removing NodeB ---");
        consistentHashing.removeNode("NodeB");

        System.out.println("\n--- Data Mapping After Removing NodeB ---");
        for (int i = 1; i <= 10; i++) {
            String key = "Key" + i;
            System.out.println(key + " is mapped to " + consistentHashing.getNode(key));
        }
    }
}
