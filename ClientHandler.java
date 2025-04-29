import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * ClientHandler.java This class handles communication between the client
 *
 * <p>and the server. It runs in a separate thread but has a link to a common list of clients to
 * handle broadcast.
 */
public class ClientHandler implements Runnable {
  private Client myClient = null;
  private ArrayList<Client> clientList;

  ClientHandler(Client client, ArrayList<Client> clientList) {
    this.myClient = client;
    this.clientList = clientList; // Keep reference to master list
  }

  public void run() {
    try {
      System.out.println("Connection made with socket " + myClient.connectionSock);
      BufferedReader clientInput =
          new BufferedReader(new InputStreamReader(myClient.connectionSock.getInputStream()));
      DataOutputStream clientOutput =
          new DataOutputStream(myClient.connectionSock.getOutputStream());

      // Prompt for unique username
      clientOutput.writeBytes("Welcome to the chat! Please enter your username:\n");
      String username;
      boolean isUsernameTaken;
      do {
        username = clientInput.readLine();
        isUsernameTaken = false;
        for (Client c : clientList) {
          if (c.username.equalsIgnoreCase(username)) {
            isUsernameTaken = true;
            clientOutput.writeBytes("Username '" + username + "' is taken. Try another:\n");
            break;
          }
        }
      } while (isUsernameTaken);

      // Assign unique username to myClient
      myClient.username = username;

      // Add to client list
      clientList.add(myClient);

      // Notify others
      String joinMessage = myClient.username + " has joined the chat";
      System.out.println(joinMessage);
      for (Client c : clientList) {
        if (c != myClient) {
          DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
          output.writeBytes(joinMessage + "\n");
        }
      }

      while (true) {
        String clientText = clientInput.readLine();

        if (clientText != null) {
          System.out.println("Received from " + myClient.username + ": " + clientText);
          String input = clientText.trim().toLowerCase();

          switch (input) {
            case "who?":
              // Send list of usernames to the requester only
              StringBuilder userList = new StringBuilder("Connected users:\n");
              for (Client c : clientList) {
                userList.append(c.username).append("\n");
              }
              clientOutput.writeBytes(userList.toString() + "\n");
              break;

            case "goodbye":
              String exitMessage = myClient.username + " has left the chat";
              System.out.println(exitMessage);
              // Notify others
              for (Client c : new ArrayList<>(clientList)) {
                if (c != myClient) {
                  try {
                    DataOutputStream output =
                        new DataOutputStream(c.connectionSock.getOutputStream());
                    output.writeBytes(exitMessage + "\n");
                  } catch (IOException e) {
                    clientList.remove(c);
                  }
                }
              }
              clientList.remove(myClient);
              myClient.connectionSock.close();
              return;

            default:
              // Broadcast message
              String messageWithUsername = myClient.username + ": " + clientText;
              for (Client c : clientList) {
                if (c != myClient) {
                  DataOutputStream output =
                      new DataOutputStream(c.connectionSock.getOutputStream());
                  output.writeBytes(messageWithUsername + "\n");
                }
              }
              break;
          }

        } else {
          // Disconnected
          System.out.println("Closing connection for socket " + myClient.connectionSock);
          clientList.remove(myClient);
          myClient.connectionSock.close();
          break;
        }
      }

    } catch (Exception e) {
      System.out.println("Error: " + e.toString());
      for (int i = 0; i < clientList.size(); i++) {
        if (clientList.get(i).connectionSock == myClient.connectionSock) {
          clientList.remove(i);
          break;
        }
      }
    }
  }
}
