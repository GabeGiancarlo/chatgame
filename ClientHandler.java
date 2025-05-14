import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
  private Client myClient = null;
  private ArrayList<Client> clientList;
  private static boolean hostAssigned = false;

  ClientHandler(Client client, ArrayList<Client> clientList) {
    this.myClient = client;
    this.clientList = clientList;
  }

  private String getCommandList() {
    return "Available commands:\n"
        + "SCORES - View current scores\n"
        + "who? - List connected users\n"
        + "goodbye - Exit the chat\n";
  }

  public void run() {
    try {
      System.out.println("Connection made with socket " + myClient.connectionSock);
      BufferedReader clientInput =
          new BufferedReader(new InputStreamReader(myClient.connectionSock.getInputStream()));
      DataOutputStream clientOutput =
          new DataOutputStream(myClient.connectionSock.getOutputStream());

      // Assign host
      synchronized (clientList) {
        if (clientList.isEmpty() && !hostAssigned) {
          myClient.username = "host";
          hostAssigned = true;
          clientList.add(myClient);
          String hostInstructions =
              "Welcome, host! You are the game host.\n"
                  + "To ask a question: Type your question followed by '@points' (e.g., 'What is"
                  + " 2+2? @5')\n"
                  + "To assign points: Type '@username points' (e.g., '@player1 5')\n"
                  + "Use SCORES to view all players' scores\n"
                  + getCommandList();
          clientOutput.writeBytes(hostInstructions + "\n");
          System.out.println("Host has joined the chat");
        } else {
          // Prompt for unique username
          clientOutput.writeBytes("Welcome to the chat! Please enter your username:\n");
          String username;
          boolean isUsernameTaken;
          do {
            username = clientInput.readLine();
            isUsernameTaken = false;
            if (username == null
                || username.trim().isEmpty()
                || username.equalsIgnoreCase("host")) {
              isUsernameTaken = true;
              clientOutput.writeBytes("Invalid username. Try another:\n");
              continue;
            }
            for (Client c : clientList) {
              if (c.username.equalsIgnoreCase(username)) {
                isUsernameTaken = true;
                clientOutput.writeBytes("Username '" + username + "' is taken. Try another:\n");
                break;
              }
            }
          } while (isUsernameTaken);

          myClient.username = username;
          clientList.add(myClient);
          clientOutput.writeBytes(
              "Username '" + username + "' accepted!\n" + getCommandList() + "\n");

          String joinMessage = myClient.username + " has joined the chat";
          System.out.println(joinMessage);
          for (Client c : clientList) {
            if (c != myClient) {
              DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
              output.writeBytes(joinMessage + "\n");
            }
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

      // Main loop
      while (true) {
        String clientText = clientInput.readLine();

        if (clientText != null) {
          System.out.println("Received from " + myClient.username + ": " + clientText);
          String input = clientText.trim();

          // WHO command
          if (input.equalsIgnoreCase("who?")) {
            StringBuilder userList = new StringBuilder("Connected users:\n");
            for (Client c : clientList) {
              userList.append(c.username).append("\n");
            }
            clientOutput.writeBytes(userList.toString() + "\n");
            continue;
          }

          // SCORES command
          if (input.equalsIgnoreCase("scores")) {
            StringBuilder scores = new StringBuilder("Current scores:\n");
            for (Client c : clientList) {
              scores.append(c.username).append(": ").append(c.score).append(" points\n");
            }
            for (Client c : clientList) {
              DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
              output.writeBytes(scores.toString());
            }
            continue;
          }

          // GOODBYE command
          if (input.equalsIgnoreCase("goodbye")) {
            String exitMessage = myClient.username + " has left the chat";
            System.out.println(exitMessage);
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
            if (myClient.username.equals("host")) {
              hostAssigned = false;
            }
            clientList.remove(myClient);
            myClient.connectionSock.close();
            return;
          }

          // Point-award command (only host)
          if (myClient.username.equals("host") && input.startsWith("@")) {
            String[] parts = input.split(" ");
            if (parts.length == 2) {
              String targetUsername = parts[0].substring(1);
              try {
                int points = Integer.parseInt(parts[1]);
                boolean found = false;
                for (Client c : clientList) {
                  if (c.username.equalsIgnoreCase(targetUsername)) {
                    c.score += points;
                    clientOutput.writeBytes(
                        "Awarded " + points + " points to " + c.username + "\n");
                    for (Client cl : clientList) {
                      DataOutputStream out =
                          new DataOutputStream(cl.connectionSock.getOutputStream());
                      out.writeBytes("Host awarded " + points + " points to " + c.username + "!\n");
                    }
                    found = true;
                    break;
                  }
                }
                if (!found) {
                  clientOutput.writeBytes("User not found.\n");
                }
              } catch (NumberFormatException e) {
                clientOutput.writeBytes("Invalid point value.\n");
              }
              continue;
            } else {
              clientOutput.writeBytes("Invalid format. Use @username points (e.g., @bob 5)\n");
              continue;
            }
          }

          // Broadcast regular messages
          String messageWithUsername = myClient.username + ": " + input;
          for (Client c : clientList) {
            if (c != myClient) {
              DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
              output.writeBytes(messageWithUsername + "\n");
            }
          }
        } else {
          // Client disconnected
          System.out.println("Closing connection for socket " + myClient.connectionSock);
          if (myClient.username.equals("host")) {
            hostAssigned = false;
          }
          clientList.remove(myClient);
          myClient.connectionSock.close();
          break;
        }
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.toString());
      if (myClient.username.equals("host")) {
        hostAssigned = false;
      }
      clientList.remove(myClient);
    }
  }
}
