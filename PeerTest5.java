public class PeerTest5 {
    public static void main(String[] args) throws InterruptedException {
        ActivePeer peer = new ActivePeer("127.0.0.1",8085);
        Thread.sleep(1000);
        System.out.println("Asking for file3");
        peer.searchFile("file3");
    }
}
