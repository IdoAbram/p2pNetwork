public class PeerTest3 {
    public static void main(String[] args) throws InterruptedException {
        ActivePeer peer = new ActivePeer("127.0.0.1",8083);
        peer.addFile("file3","text3");
        Thread.sleep(500);
        System.out.println("Uploading file3");
        Thread.sleep(1000);
        System.out.println("Asking for file1");
        peer.searchFile("file1");
    }
}
