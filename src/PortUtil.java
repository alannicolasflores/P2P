import java.net.DatagramSocket;
public class PortUtil {
    public static int findAvailablePort(int startPort) {
        int port = startPort;
        while (true) {
            try (DatagramSocket ignored = new DatagramSocket(port)) {
                return port;
            } catch (Exception e) {
                port++;
            }
        }
    }
}
