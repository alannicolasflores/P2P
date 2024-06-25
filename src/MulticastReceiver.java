import java.io.*;
import java.net.*;

public class MulticastReceiver implements Runnable {
    private Nodo nodo;
    private boolean isLocal;
    private int multicastPort;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";

    public MulticastReceiver(Nodo nodo, boolean isLocal, int multicastPort) {
        this.nodo = nodo;
        this.isLocal = isLocal;
        this.multicastPort = multicastPort;
    }

    @Override
    public void run() {
        if (isLocal) {
            runLocal();
        } else {
            runMulticast();
        }
    }

    private void runLocal() {
        try (DatagramSocket socket = new DatagramSocket(multicastPort)) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Mensaje recibido localmente: " + message);
                processMulticastMessage(message, packet.getAddress(), packet.getPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runMulticast() {
        try (MulticastSocket socket = new MulticastSocket(multicastPort)) {
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Mensaje recibido por multicast: " + message);
                processMulticastMessage(message, packet.getAddress(), packet.getPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMulticastMessage(String message, InetAddress address, int port) {
        if (message.startsWith("Solicitud de archivo:")) {
            String fileName = message.substring("Solicitud de archivo:".length()).trim();
            System.out.println("Buscando archivo: " + fileName + " en respuesta a la red.");
            File file = nodo.index.searchFile(fileName);
            if (file != null && file.exists()) {
                System.out.println("Archivo encontrado: " + fileName);
                sendResponse("Archivo encontrado: " + fileName, address, port);
            } else {
                System.out.println("Archivo no encontrado: " + fileName);
                sendResponse("Archivo no encontrado: " + fileName, address, port);
            }
        } else if (message.startsWith("Archivo encontrado:")) {
            String fileName = message.substring("Archivo encontrado:".length()).trim();
            System.out.println("Confirmación de archivo encontrado: " + fileName);
            new FileClient(nodo).downloadFile(fileName);
        }
    }

    private void sendResponse(String responseMessage, InetAddress address, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = responseMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            System.out.println("Respuesta enviada: " + responseMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
