import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConsistentHashingWithVirtualNodes {
    // Number of virtual nodes per physical node
    private static final int VIRTUAL_NODES = 5;
    private final SortedMap<Integer, String> hashRing = new TreeMap<>();

    // MurmurHash3 implementation for better distribution
    private int hash(String key) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int h = 0x9747b28c;
        while (buffer.remaining() >= 4) {
            int k = buffer.getInt();
            k *= 0xcc9e2d51;
            k = (k << 15) | (k >>> 17);
            k *= 0x1b873593;
            h ^= k;
            h = (h << 13) | (h >>> 19);
            h = h * 5 + 0xe6546b64;
        }
        h ^= buffer.remaining();
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        h *= 0xc2b2ae35;
        h ^= (h >>> 16);
        return h & 0x7fffffff;  // Non-negative hash
    }

    // Add a new node to the hash ring
    public void addNode(String nodeName) {
        for (int i = 0; i < VIRTUAL_NODES; i++) {
            int hash = hash(nodeName + "VN" + i);
            hashRing.put(hash, nodeName);
            System.out.println("Added virtual node: " + (nodeName + "VN" + i) + " with hash " + hash);
        }
    }

    // Remove a node and its virtual nodes from the hash ring
    public void removeNode(String nodeName) {
        for (int i = 0; i < VIRTUAL_NODES; i++) {
            int hash = hash(nodeName + "VN" + i);
            hashRing.remove(hash);
            System.out.println("Removed virtual node: " + (nodeName + "VN" + i) + " with hash " + hash);
        }
    }

    // Get the node responsible for the given key
    public String getNode(String key) {
        int hash = hash(key);
        if (!hashRing.containsKey(hash)) {
            SortedMap<Integer, String> tailMap = hashRing.tailMap(hash);
            hash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        }
        return hashRing.get(hash);
    }

    public static void main(String[] args) {
        ConsistentHashingWithVirtualNodes ch = new ConsistentHashingWithVirtualNodes();

        // Adding nodes to the cluster
        ch.addNode("NodeA");
        ch.addNode("NodeB");
        ch.addNode("NodeC");

        // Mapping some keys
        System.out.println("\nKey 'user123' is mapped to: " + ch.getNode("user123"));
        System.out.println("Key 'product456' is mapped to: " + ch.getNode("product456"));
        System.out.println("Key 'order789' is mapped to: " + ch.getNode("order789"));
        System.out.println("Key 'order789' is mapped to: " + ch.getNode("order7899"));
        System.out.println("Key 'order789' is mapped to: " + ch.getNode("order7897"));

        // Adding a new node and checking the distribution
        System.out.println("\n--- Adding NodeD ---");
        ch.addNode("NodeD");
        System.out.println("Key 'user123' is now mapped to: " + ch.getNode("user123"));
        System.out.println("Key 'product456' is now mapped to: " + ch.getNode("product456"));
        System.out.println("Key 'order789' is now mapped to: " + ch.getNode("order789"));
        System.out.println("Key 'order7899' is now mapped to: " + ch.getNode("order7899"));
        System.out.println("Key 'order7897' is now mapped to: " + ch.getNode("order7897"));
    }
}
