import java.io.File;
import java.net.ServerSocket;
import java.util.Scanner;

public class Nodo {
    private static final String DEFAULT_SHARED_FOLDER = "shared_folder";
    private static final int STARTING_SOCKET_PORT = 5000;
    private static final int MULTICAST_PORT_START = 8888;
    private static int socketPort;
    private static int multicastPort;
    private String nodeName;
    public Index index;
    private String sharedFolder;
    private SearchManager searchManager;
    private boolean isLocal;

    public Nodo() {
        this.socketPort = findAvailablePort(STARTING_SOCKET_PORT);
        this.multicastPort = PortUtil.findAvailablePort(MULTICAST_PORT_START);
        this.nodeName = "Node-" + (socketPort - STARTING_SOCKET_PORT);
        this.isLocal = askNetworkMode();
        configureSharedFolder();
        this.index = new Index(sharedFolder);
        updateSearchFile();
        this.searchManager = new SearchManager(this, index, isLocal);
        new Thread(new MulticastReceiver(this, isLocal, multicastPort)).start();
        new Thread(new MulticastSender(this, isLocal, multicastPort)).start();
        new Thread(new FileServer(this, socketPort)).start();
        new Thread(new FileClient(this)).start();
        runConsoleMenu();
    }

    private static int findAvailablePort(int startPort) {
        int port = startPort;
        while (true) {
            try (ServerSocket ignored = new ServerSocket(port)) {
                return port;
            } catch (Exception e) {
                port++;
            }
        }
    }

    public static void main(String[] args) {
        new Nodo();
    }

    private boolean askNetworkMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("¿Lo probarás en una computadora local o en una RedMulticast? (local/multicast):");
        String response = scanner.nextLine();
        return response.equalsIgnoreCase("local");
    }

    public String getNodeName() {
        return nodeName;
    }

    private void configureSharedFolder() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("¿Desea especificar una carpeta compartida personalizada? (s/n):");
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("s")) {
            System.out.println("Ingrese el nombre de la carpeta compartida:");
            sharedFolder = scanner.nextLine() + "-" + (socketPort - STARTING_SOCKET_PORT);
        } else {
            sharedFolder = DEFAULT_SHARED_FOLDER + "-" + (socketPort - STARTING_SOCKET_PORT);
        }
        File folder = new File(sharedFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        System.out.println("Carpeta asignada: " + sharedFolder);
    }

    private void updateSearchFile() {
        File folder = new File(sharedFolder);
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    index.updateIndex(file.getName(), file);
                }
            }
        }
    }

    public String getSharedFolder() {
        return sharedFolder;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    private void runConsoleMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Seleccione una opción:");
            System.out.println("1. Buscar archivo");
            System.out.println("2. Salir");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                System.out.println("Ingrese el nombre del archivo a buscar:");
                String fileName = scanner.nextLine();
                searchManager.searchFile(fileName);
            } else if (choice == 2) {
                System.out.println("Saliendo...");
                System.exit(0);
            } else {
                System.out.println("Opción no válida.");
            }
        }
    }
}
