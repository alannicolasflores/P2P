import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    private int port;
    private IndexManager indexManager;
    private Map<String, Integer> knownPeers;

    public NetworkManager(int port, IndexManager indexManager) {
        this.port = port;
        this.indexManager = indexManager;
        this.knownPeers = new ConcurrentHashMap<>();
    }

    public void startDiscovery() {
        new Thread(() -> {
            try {
                DatagramSocket socket = findAvailableDatagramSocket(port + 1000); // Add offset to avoid conflict

                // Listen for broadcast messages
                new Thread(() -> {
                    try (DatagramSocket listenerSocket = findAvailableDatagramSocket(port + 1000)) {
                        while (true) {
                            byte[] buffer = new byte[1024];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            listenerSocket.receive(packet);

                            String message = new String(packet.getData(), 0, packet.getLength());
                            System.out.println("Received message: " + message); // Debug message
                            if (message.startsWith("DISCOVER_PEER")) {
                                String peerInfo = message.split(":")[1];
                                String peerIP = packet.getAddress().getHostAddress();
                                int peerPort = Integer.parseInt(peerInfo);

                                knownPeers.put(peerIP, peerPort);

                                System.out.println("Known peers updated: " + knownPeers); // Debug message

                                // Reply to the discovery message
                                String reply = "PEER_RESPONSE:" + port;
                                DatagramPacket response = new DatagramPacket(reply.getBytes(), reply.length(), packet.getAddress(), peerPort);
                                listenerSocket.send(response);
                            } else if (message.startsWith("PEER_RESPONSE")) {
                                String peerInfo = message.split(":")[1];
                                String peerIP = packet.getAddress().getHostAddress();
                                int peerPort = Integer.parseInt(peerInfo);

                                knownPeers.put(peerIP, peerPort);

                                System.out.println("Received PEER_RESPONSE from " + peerIP + ":" + peerPort);
                                System.out.println("Known peers updated: " + knownPeers); // Debug message
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                // Broadcast discovery message
                while (true) {
                    String discoveryMessage = "DISCOVER_PEER:" + port;
                    DatagramPacket packet = new DatagramPacket(discoveryMessage.getBytes(), discoveryMessage.length(), InetAddress.getByName("255.255.255.255"), 5000);
                    socket.send(packet);

                    System.out.println("Broadcasted discovery message: " + discoveryMessage); // Debug message
                    Thread.sleep(5000); // Sleep for 5 seconds before broadcasting again
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private DatagramSocket findAvailableDatagramSocket(int startPort) {
        int port = startPort;
        while (true) {
            try {
                return new DatagramSocket(port);
            } catch (SocketException e) {
                port++;
            }
        }
    }

    public void searchFile(String fileName) {
        System.out.println("Searching for file: " + fileName);
        File file = indexManager.searchFile(fileName);
        if (file != null) {
            System.out.println("File found locally: " + file.getName());
        } else {
            System.out.println("File not found locally. Searching in network...");
            for (Map.Entry<String, Integer> entry : knownPeers.entrySet()) {
                String peerIP = entry.getKey();
                int peerPort = entry.getValue();
                System.out.println("Searching in peer: " + peerIP + ":" + peerPort); // Debug message
                if (searchFileInPeer(fileName, peerIP, peerPort)) {
                    System.out.println("File found in peer: " + peerIP + ":" + peerPort);
                    break;
                }
            }
        }
    }

    private boolean searchFileInPeer(String fileName, String peerIP, int peerPort) {
        try (Socket socket = new Socket(peerIP, peerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(fileName);
            String response = in.readLine();
            System.out.println("Response from peer: " + response); // Debug message
            return "File found".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
