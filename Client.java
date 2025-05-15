import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Represents a connected client with a socket and username.
 */
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
