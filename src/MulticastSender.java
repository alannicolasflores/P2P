import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastSender implements Runnable {
    private Nodo nodo;
    private boolean isLocal;
    private int multicastPort;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";

    public MulticastSender(Nodo nodo, boolean isLocal, int multicastPort) {
        this.nodo = nodo;
        this.isLocal = isLocal;
        this.multicastPort = multicastPort;
    }

    @Override
    public void run() {
        // El envío de mensajes será gestionado por métodos específicos de búsqueda
    }

    public void sendRequest(String fileName) {
        if (isLocal) {
            sendLocalRequest(fileName);
        } else {
            sendMulticastRequest(fileName);
        }
    }

    private void sendLocalRequest(String fileName) {
        try (DatagramSocket socket = new DatagramSocket(PortUtil.findAvailablePort(multicastPort))) {
            String message = "Solicitud de archivo: " + fileName;
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, multicastPort);
            socket.send(packet);
            System.out.println("Consulta enviada localmente: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMulticastRequest(String fileName) {
        try (DatagramSocket socket = new DatagramSocket(PortUtil.findAvailablePort(multicastPort))) {
            String message = "Solicitud de archivo: " + fileName;
            byte[] buffer = message.getBytes();
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);
            socket.send(packet);
            System.out.println("Consulta enviada por multicast: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
