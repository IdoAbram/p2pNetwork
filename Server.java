import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Peer {
    public static Server instance; // Singleton instance
    List<Peer> peers = new ArrayList<>();

    // Private constructor to prevent instantiation from other classes
    private Server(String ip, int port) {
        super(ip, port);
        startListening();
    }

    // Public method to get the Singleton instance
    public static void getInstance(String ip, int port) {
        if (instance == null) {
            instance = new Server(ip, port);
        }
    }

    void connectPeer(Peer peer) {
        peers.add(peer);
        if (peers.size() > 1) {
            connect(peers.get(peers.size() - 2),peer);
        }
    }

    void disconnectPeer(Peer peer) {
        if (peers != null && peers.size() > 1) {
            int index = peers.indexOf(peer);
            if (index > 0 && index < peers.size() - 1 && peers.size() > 1) {
                connect(peers.get(index - 1),(peers.get(index + 1)));
            }
        }
        peers.remove(peer);
    }

    public void connect(Peer peer1,Peer peer2) {
        try {
            Socket socket = new Socket(peer1.ip, peer1.port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("connect");
            out.println(peer2.port);
            out.println(peer2.ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Socket socket = new Socket(peer2.ip, peer2.port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("connect");
            out.println(peer1.port);
            out.println(peer1.ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startListening() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening for incoming connections on port " + port + "...\n");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String action = in.readLine();
                int clientPort = Integer.parseInt(in.readLine());
                String clientIp = in.readLine();
                switch (action) {
                    case "connect":
                        System.out.println("user connected");
                        connectPeer(new Peer(clientIp,clientPort));
                        break;
                    case "disconnect":
                        System.out.println("user disconnected");
                        disconnectPeer(new Peer(clientIp,clientPort));
                        break;

                    default:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
