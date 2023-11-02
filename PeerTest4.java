public class PeerTest4 {
    public static void main(String[] args) {
        ActivePeer peer = new ActivePeer("127.0.0.1", 8084);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Asking for file1");
        Thread searchThread = new Thread(() -> {
            peer.searchFile("file1");
        });
        searchThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        searchThread.interrupt();
        System.out.println("Disconnect");
        peer.disconnect();
        System.exit(0);
    }
}
