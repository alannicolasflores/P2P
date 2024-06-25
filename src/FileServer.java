import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer implements Runnable {
    private Nodo nodo;
    private int serverPort;

    public FileServer(Nodo nodo, int serverPort) {
        this.nodo = nodo;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String fileName = reader.readLine();
                File file = nodo.index.searchFile(fileName);
                if (file != null) {
                    try (ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream())) {
                        objectOutput.writeObject(file);
                        objectOutput.writeObject(readFileBytes(file));
                    }
                    System.out.println("Archivo enviado: " + fileName);
                } else {
                    System.out.println("Archivo no encontrado.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fileInput = new FileInputStream(file)) {
            return fileInput.readAllBytes();
        }
    }
}
