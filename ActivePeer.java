import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

//message protocol for each message: action\n port\n ip\n data(if exist)
//using strategy pattern so the code will be open to changes with the protocol for each message, every object is for different action
//peers communicate with sockets
interface ActionStrategy {
    void execute(ActivePeer activePeer, Socket clientSocket, String clientIp, int clientPort, BufferedReader in) throws IOException, ClassNotFoundException;
}

class ConnectAction implements ActionStrategy {
    @Override
    public void execute(ActivePeer activePeer, Socket clientSocket, String clientIp, int clientPort, BufferedReader in) {
        activePeer.peers.add(new Peer(clientIp, clientPort));
        System.out.println("New peer added from port: " + clientPort + " and address: " + clientIp);
    }
}//"connect"
class DisconnectAction implements ActionStrategy {
    @Override
    public void execute(ActivePeer activePeer, Socket clientSocket, String clientIp, int clientPort, BufferedReader in) {
        activePeer.peers.remove(new Peer(clientIp, clientPort));
        System.out.println("Peer disconnected from port: " + clientPort + " and address: " + clientIp);
    }
}//"disconnect"
class SearchAction implements ActionStrategy {
    @Override
    public void execute(ActivePeer activePeer, Socket clientSocket, String clientIp, int clientPort, BufferedReader in) throws IOException {
        String fileName = in.readLine();
        if (activePeer.files.containsKey(fileName)) {
            activePeer.sendFile(new Peer(clientIp, clientPort), fileName);
            activePeer.connectTo(new Peer(clientIp, clientPort));
        } else {
            activePeer.executor.execute(() -> {
                try {
                    Socket socket = new Socket(clientIp, clientPort);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("sendPeers");
                    out.println(activePeer.port);
                    out.println(activePeer.ip);
                    out.println(fileName);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(activePeer.peers);
                } catch (IOException e) {}
            });
        }
    }
}//"searchFile"
class SendAction implements ActionStrategy {
    @Override
    public void execute(ActivePeer activePeer, Socket clientSocket, String clientIp, int clientPort, BufferedReader in) throws IOException {
        String fileName = in.readLine();
        String fileData = in.readLine();
        activePeer.files.put(fileName, fileData);
        activePeer.fileData = fileName + ": " + fileData;
        System.out.println(fileName + ": " + fileData);
    }
}//"sendFile"
class SendPeersAction implements ActionStrategy {
    @Override
    public void execute(ActivePeer activePeer, Socket clientSocket, String clientIp, int clientPort, BufferedReader in) throws IOException, ClassNotFoundException {
        String fileName = in.readLine();
        ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        Set<Peer> newPeers = (Set<Peer>) objectInputStream.readObject();
        //using for the next search only unreached peers
        newPeers.forEach(peer -> {if(!activePeer.peers.contains(peer)&& !peer .equals(activePeer)){activePeer.askForFile(peer,fileName);}});
        activePeer.peers.addAll(newPeers);
    }
}//"sendPeers"

public class ActivePeer extends Peer{
    Set<Peer> peers =  new HashSet<>();
    HashMap<String,String> files = new HashMap<>();
    AtomicBoolean stop = new AtomicBoolean(false);
    ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, ActionStrategy> actionStrategies = new HashMap<>();
    String fileData;

    public ActivePeer(String ip, int port){
        super(ip,port);
        setupActionStrategies();
        startListening();
        connectTo(new Peer("127.0.0.1",8080));//connect to the server so the servel will apply me to the other peers network (the server is not connected to the network but it is know the network)
    }

    private void setupActionStrategies() {
        actionStrategies.put("connect", new ConnectAction());
        actionStrategies.put("disconnect", new DisconnectAction());
        actionStrategies.put("searchFile", new SearchAction());
        actionStrategies.put("sendFile", new SendAction());
        actionStrategies.put("sendPeers", new SendPeersAction());
    }
    public void addFile(String fileName,String fileData){ executor.execute(()-> files.put(fileName,fileData)); }
    public void connectTo(Peer peer) {
        executor.execute(() -> {
            peers.add(peer);
            try {
                Socket socket = new Socket(peer.ip, peer.port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("connect");
                out.println(port);
                out.println(ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }//connecting to other peer with socket
    public void disconnect(){
        this.peers.forEach(peer -> disconnectFrom(new Peer("127.0.0.1",8080)));
        executor.shutdown(); // Initiate a graceful shutdown
        stop.set(true);
        try {
            // Wait for the executor to finish executing tasks
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                // If it doesn't finish within 5 seconds, you can choose to force shutdown
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void disconnectFrom(Peer peer) {
        try {
            Socket socket = new Socket(peer.ip, peer.port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("disconnect");
            out.println(port);
            out.println(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        peers.remove(peer);
    }
    public void sendFile(Peer peer,String fileName) {
        executor.execute(() -> {
            try {
                Socket socket = new Socket(peer.ip, peer.port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("sendFile");
                out.println(port);
                out.println(ip);
                out.println(fileName);
                out.println(files.get(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void searchFile(String fileName){ executor.execute(()-> peers.forEach(peer -> askForFile(peer,fileName))); }
    public void askForFile(Peer peer,String fileName) {
        executor.execute(() -> {
            try {
                Socket socket = new Socket(peer.ip, peer.port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("searchFile");
                out.println(port);
                out.println(ip);
                out.println(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void startListening() {
        executor.execute(this::run);
    }

    private void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening for incoming connections on port " + port + "...\n");
            while (!stop.get()) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String action = in.readLine();
                int clientPort = Integer.parseInt(in.readLine());
                String clientIp = in.readLine();
                ActionStrategy actionStrategy = actionStrategies.get(action);
                if (actionStrategy != null) {
                    actionStrategy.execute(this,clientSocket, clientIp, clientPort, in);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}