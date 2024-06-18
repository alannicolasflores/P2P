import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class PeerNode {
    private int port;
    private String sharedFolderPath;
    private FileManager fileManager;
    private IndexManager indexManager;
    private NetworkManager networkManager;

    public PeerNode(int startPort, String baseSharedFolderPath) {
        this.port = findAvailablePort(startPort); // Start searching from startPort
        this.sharedFolderPath = selectSharedFolder(baseSharedFolderPath);
        this.fileManager = new FileManager(sharedFolderPath);
        this.indexManager = new IndexManager(fileManager);
        this.networkManager = new NetworkManager(port, indexManager);
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Peer node started on port " + port + " with shared folder: " + sharedFolderPath);

        // Start a thread to handle incoming connections
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket, fileManager)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Start network discovery
        new Thread(() -> networkManager.startDiscovery()).start();

        // Start console UI
        startConsoleUI();
    }

    private void startConsoleUI() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("1. Search for a file");
                System.out.println("2. Exit");
                int choice = scanner.nextInt();
                scanner.nextLine();  // consume newline

                if (choice == 1) {
                    System.out.println("Enter file name to search:");
                    String fileName = scanner.nextLine();
                    networkManager.searchFile(fileName);
                } else if (choice == 2) {
                    System.out.println("Exiting...");
                    break;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();  // consume the invalid input
            } catch (NoSuchElementException e) {
                System.out.println("No input found. Exiting.");
                break;
            }
        }
        scanner.close();
    }

    private int findAvailablePort(int startPort) {
        int port = startPort;
        while (true) {
            try (ServerSocket socket = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                port++;
            }
        }
    }

    private String selectSharedFolder(String baseSharedFolderPath) {
        Scanner scanner = new Scanner(System.in);
        File baseFolder = new File(baseSharedFolderPath);

        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
            return baseSharedFolderPath;
        } else {
            System.out.println("Folder " + baseSharedFolderPath + " already exists. Do you want to use it? (yes/no)");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("yes")) {
                return baseSharedFolderPath;
            } else {
                int counter = 1;
                while (true) {
                    File newFolder = new File(baseSharedFolderPath + counter);
                    if (!newFolder.exists()) {
                        newFolder.mkdirs();
                        return newFolder.getPath();
                    }
                    counter++;
                }
            }
        }
    }

    public static void main(String[] args) {
        int startPort = 5000;
        String baseSharedFolderPath = "shared";

        if (args.length > 0) {
            try {
                startPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 5000.");
            }
        }

        if (args.length > 1) {
            baseSharedFolderPath = args[1];
        }

        try {
            PeerNode peerNode = new PeerNode(startPort, baseSharedFolderPath);
            peerNode.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
