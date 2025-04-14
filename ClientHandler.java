import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * ClientHandler.java
 *
 * <p>This class handles communication between the client
 * and the server.  It runs in a separate thread but has a
 * link to a common list of clients to handle broadcast.
 *
 */
public class ClientHandler implements Runnable {
  private Client myClient = null;
  private ArrayList<Client> clientList;

  ClientHandler(Client client, ArrayList<Client> clientList) {
    this.myClient = client;
    this.clientList = clientList;  // Keep reference to master list
  }

  /**
   * received input from a client.
   * sends it to other clients.
   */
  public void run() {
    try {
      System.out.println("Connection made with socket " + myClient.connectionSock);
      BufferedReader clientInput = new BufferedReader(
          new InputStreamReader(myClient.connectionSock.getInputStream()));
      DataOutputStream clientOutput = new DataOutputStream(myClient.connectionSock.getOutputStream());
      
      // Prompt for username
      clientOutput.writeBytes("Welcome to the chat! Please enter your username:\n");
      String username = clientInput.readLine();
      
      // Update the client's username
      myClient.username = username;
      
      // Add client to the list
      clientList.add(myClient);
      
      // Notify all clients that a new user has joined
      String joinMessage = myClient.username + " has joined the chat";
      System.out.println(joinMessage);
      for (Client c : clientList) {
        if (c.connectionSock != myClient.connectionSock) {
          DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
          output.writeBytes(joinMessage + "\n");
        }
      }
      
      while (true) {
        // Get data sent from a client
        String clientText = clientInput.readLine();
        if (clientText != null) {
          System.out.println("Received from " + myClient.username + ": " + clientText);
          
          // Use switch statement to handle client commands
          switch (clientText.trim().toLowerCase()) {
            case "who?":
              // Build list of all usernames
              StringBuilder userList = new StringBuilder("Connected users:\n");
              for (Client c : clientList) {
                userList.append(c.username).append("\n");
              }
              // Send list to the requesting client only
              clientOutput.writeBytes(userList.toString() + "\n");
              break;
              
            default:
              // Broadcast regular messages to all other clients
              String messageWithUsername = myClient.username + ": " + clientText;
              for (Client c : clientList) {
                if (c.connectionSock != myClient.connectionSock) {
                  DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
                  output.writeBytes(messageWithUsername + "\n");
                }
              }
              break;
          }
        } else {
          // Connection was lost
          System.out.println("Closing connection for socket " + myClient.connectionSock);
          // Remove from arraylist
          clientList.remove(myClient);
          myClient.connectionSock.close();
          break;
        }
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.toString());
      // Find and remove the client with this socket
      for (int i = 0; i < clientList.size(); i++) {
        if (clientList.get(i).connectionSock == myClient.connectionSock) {
          clientList.remove(i);
          break;
        }
      }
    }
  }
} // ClientHandler for MtServer.java
