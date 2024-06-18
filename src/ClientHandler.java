import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private FileManager fileManager;

    public ClientHandler(Socket clientSocket, FileManager fileManager) {
        this.clientSocket = clientSocket;
        this.fileManager = fileManager;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String request = in.readLine();
            File file = fileManager.getFile(request);
            if (file != null) {
                out.println("File found");
                sendFile(file, clientSocket.getOutputStream());
            } else {
                out.println("File not found");
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(File file, OutputStream outputStream) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             BufferedOutputStream bos = new BufferedOutputStream(outputStream)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
        }
    }
}
