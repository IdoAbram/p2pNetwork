import java.io.Serializable;

public class Peer implements Serializable{
    protected int port;
    protected String ip;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Peer otherPeer = (Peer) obj;
        return port == otherPeer.port && ip.equals(otherPeer.ip);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + port;
        result = 31 * result + ip.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }


    public Peer (String ip,int port){
        this.ip = ip;
        this.port = port;
    }
}
