import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ClientHandler.java
 *
 * <p>This class handles communication between the client
 * and the server.  It runs in a separate thread but has a
 * link to a common list of clients to handle broadcast.
 *
 */
public class ClientHandler implements Runnable {
  private Socket connectionSock = null;
  private ArrayList<Client> clientList;

  ClientHandler(Socket sock, ArrayList<Client> clientList) {
    this.connectionSock = sock;
    this.clientList = clientList;  // Keep reference to master list
  }

  /**
   * received input from a client.
   * sends it to other clients.
   */
  public void run() {
    try {
      System.out.println("Connection made with socket " + connectionSock);
      BufferedReader clientInput = new BufferedReader(
          new InputStreamReader(connectionSock.getInputStream()));
      DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());
      
      // Prompt for username
      String username;
      boolean isUsernameTaken;
      clientOutput.writeBytes("Welcome to the chat! Please enter your username:\n");
      do {
        username = clientInput.readLine();
        isUsernameTaken = false;
        for (Client c : clientList) {
          if (c.username.equals(username)) {
              isUsernameTaken = true;
              clientOutput.writeBytes("Username '" + username + "' is taken. Try another:\n");
              break;
          }
        }
      } while (isUsernameTaken);
      
      // Create a new Client object and add to the list
      Client newClient = new Client(connectionSock, username);
      clientList.add(newClient);
      
      // Notify all clients that a new user has joined
      String joinMessage = username + " has joined the chat";
      System.out.println(joinMessage);
      for (Client c : clientList) {
        if (c.connectionSock != connectionSock) {
          DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
          output.writeBytes(joinMessage + "\n");
        }
      }
      
      while (true) {
        // Get data sent from a client
        String clientText = clientInput.readLine();
        if (clientText != null) {
          System.out.println("Received from " + username + ": " + clientText);
          // Turn around and output this data
          // to all other clients except the one
          // that sent us this information

          String clientCommand = clientText.trim().toLowerCase();
          boolean isCommand = true;
          switch (clientCommand) {
            case "who?":
              StringBuilder users = new StringBuilder();
              for (Client c : clientList) {
                  if (c.connectionSock != connectionSock) { // Exclude self
                      users.append(c.username).append(", ");
                  }
              }
              // response to who command
              String response = users.length() > 0 ? 
                  "Connected users: " + users.substring(0, users.length()-2) : 
                  "No other users connected";
              // send to requesting client only
              clientOutput.writeBytes(response + "\n");
              break;
            case "goodbye":
              String exitMessage = username + " has left the chat";
              System.out.println(exitMessage);
              for (Client c : new ArrayList<>(clientList)) {
                if (c.connectionSock != connectionSock) {
                  try {
                    DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
                    output.writeBytes(exitMessage + "\n");
                  } catch (IOException e) {
                    clientList.remove(c); // Remove dead clients 
                  }
                }
              }
              clientList.remove(newClient);
              connectionSock.close();
              return;
            default:
              isCommand = false;
              break;
          } 
          if (isCommand) continue;
          String messageWithUsername = username + ": " + clientText;
            for (Client c : clientList) {
              if (c.connectionSock != connectionSock) {
                DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
                output.writeBytes(messageWithUsername + "\n");
              }
            }
          } else {
            // Connection was lost
            System.out.println("Closing connection for socket " + connectionSock);
            // Remove from arraylist
            clientList.remove(newClient);
            connectionSock.close();
            break;
          }
        }
    } catch (Exception e) {
      System.out.println("Error: " + e.toString());
      // Find and remove the client with this socket
      for (int i = 0; i < clientList.size(); i++) {
        if (clientList.get(i).connectionSock == connectionSock) {
          clientList.remove(i);
          break;
        }
      }
    }
  }
} // ClientHandler for MtServer.java
