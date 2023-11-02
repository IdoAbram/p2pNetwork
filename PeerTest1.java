public class PeerTest1 {
    public static void main(String[] args) throws InterruptedException {
        ActivePeer peer = new ActivePeer("127.0.0.1",8081);
        Thread.sleep(500);
        System.out.println("Uploading file1");
        peer.addFile("file1","text1");
    }
}
