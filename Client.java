import java.net.Socket;
import java.io.DataOutputStream;

public class Client {
  public Socket connectionSock = null;
  public String username = "";
  public int score = 0;
  public DataOutputStream output;

  Client(Socket sock, String username) {
    this.connectionSock = sock;
    this.username = username;
  }
}
