import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * MTServer.java
 *
 * <p>This program implements a simple multithreaded chat server. Every client that connects to the
 * server can broadcast data to all other clients. The server stores an ArrayList of clients to
 * perform the broadcast.
 *
 * <p>The MTServer uses a ClientHandler whose code is in a separate file. When a client connects,
 * the MTServer starts a ClientHandler in a separate thread to receive messages from the client.
 *
 * <p>To test, start the server first, then start multiple clients and type messages in the client
 * windows.
 */
public class MtServer {
  // Maintain list of all clients for broadcast
  private ArrayList<Client> clientList;

  /**
   * Constructs a new MtServer with an empty client list.
   */
  public MtServer() {
    clientList = new ArrayList<Client>();
  }

  /**
   * Waits for incoming client connections on port 9004 and starts a new ClientHandler thread for
   * each connected client.
   */
  private void getConnection() {
    // Wait for a connection from the client
    try {
      System.out.println("Waiting for client connections on port 9004.");
      ServerSocket serverSock = new ServerSocket(9004);
      // This is an infinite loop, the user will have to shut it down
      // using control-c
      while (true) {
        Socket connectionSock = serverSock.accept();
        // Send to ClientHandler the socket and arraylist of all clients
        Client tempClient = new Client(connectionSock, "");
        ClientHandler handler = new ClientHandler(tempClient, this.clientList);

        Thread theThread = new Thread(handler);
        theThread.start();
      }
      // Will never get here, but if the above loop is given
      // an exit condition then we'll go ahead and close the socket
      // serverSock.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Starts the chat server by accepting incoming client connections.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    MtServer server = new MtServer();
    server.getConnection();
  }
} // MtServer
