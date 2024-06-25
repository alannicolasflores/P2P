import java.io.*;
import java.net.Socket;

public class FileClient implements Runnable {
    private Nodo nodo;

    public FileClient(Nodo nodo) {
        this.nodo = nodo;
    }

    @Override
    public void run() {
        // Este método puede estar vacío si solo llamamos a downloadFile en los lugares necesarios
    }

    public void downloadFile(String fileName) {
        try (Socket socket = new Socket("localhost", nodo.getSocketPort());
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream())) {

            writer.println(fileName);  // Envía el nombre del archivo
            File file = (File) objectInput.readObject();  // Recibe el archivo

            if (file != null) {
                // Guardar el archivo descargado en la carpeta compartida
                File outFile = new File(nodo.getSharedFolder() + "/" + fileName);
                try (FileOutputStream fileOutput = new FileOutputStream(outFile)) {
                    fileOutput.write((byte[]) objectInput.readObject());  // Escribe los datos en el archivo
                }
                System.out.println("Archivo descargado: " + fileName);  // Imprime que el archivo se descargó
                nodo.index.updateIndex(fileName, outFile);  // Actualiza el índice con el archivo descargado
            } else {
                System.out.println("Archivo no encontrado durante la descarga.");
            }
        } catch (Exception e) {
            e.printStackTrace();  // Maneja las excepciones
        }
    }
}
