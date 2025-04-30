import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * ClientHandler.java This class handles communication between the client
 * and the server. It runs in a separate thread but has a link to a common list of clients to
 * handle broadcast.
 */
public class ClientHandler implements Runnable {
    private Client myClient = null;
    private ArrayList<Client> clientList;
    private static boolean hostAssigned = false; // Track if host has been assigned

    ClientHandler(Client client, ArrayList<Client> clientList) {
        this.myClient = client;
        this.clientList = clientList;
    }

    private String getCommandList() {
        return "Available commands:\n" +
               "SCORES - View current scores\n" +
               "who? - List connected users\n" +
               "goodbye - Exit the chat\n";
    }

    public void run() {
        try {
            System.out.println("Connection made with socket " + myClient.connectionSock);
            BufferedReader clientInput =
                new BufferedReader(new InputStreamReader(myClient.connectionSock.getInputStream()));
            DataOutputStream clientOutput =
                new DataOutputStream(myClient.connectionSock.getOutputStream());

            // Check if this is the first client (host)
            synchronized (clientList) {
                if (clientList.isEmpty() && !hostAssigned) {
                    myClient.username = "host";
                    hostAssigned = true;
                    clientList.add(myClient);
                    
                    // Send host instructions
                    String hostInstructions = 
                        "Welcome, host! You are the game host.\n" +
                        "To ask a question: Type your question followed by '@points' (e.g., 'What is 2+2? @5')\n" +
                        "To assign points: Type '@username points' (e.g., '@player1 5')\n" +
                        "Use SCORES to view all players' scores\n" +
                        getCommandList();
                    clientOutput.writeBytes(hostInstructions + "\n");
                    
                    System.out.println("Host has joined the chat");
                } else {
                    // Non-host client username prompt
                    clientOutput.writeBytes("Welcome to the chat! Please enter your username:\n");
                    String username;
                    boolean isUsernameTaken;
                    do {
                        username = clientInput.readLine();
                        isUsernameTaken = false;
                        if (username == null || username.trim().isEmpty() || username.equalsIgnoreCase("host")) {
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
                    
                    // Notify client of accepted username and send commands
                    clientOutput.writeBytes("Username '" + username + "' accepted!\n" + getCommandList() + "\n");
                    
                    // Notify others
                    String joinMessage = myClient.username + " has joined the chat";
                    System.out.println(joinMessage);
                    for (Client c : clientList) {
                        if (c != myClient) {
                            DataOutputStream output = new DataOutputStream(c.connectionSock.getOutputStream());
                            output.writeBytes(joinMessage + "\n");
                        }
                    }
                }
            }

            while (true) {
                String clientText = clientInput.readLine();

                if (clientText != null) {
                    System.out.println("Received from " + myClient.username + ": " + clientText);
                    String input = clientText.trim().toLowerCase();

                    switch (input) {
                        case "who?":
                            StringBuilder userList = new StringBuilder("Connected users:\n");
                            for (Client c : clientList) {
                                userList.append(c.username).append("\n");
                            }
                            clientOutput.writeBytes(userList.toString() + "\n");
                            break;

                        case "scores":
                            // Placeholder for SCORES command (to be implemented by teammate)
                            clientOutput.writeBytes("Scores feature not yet implemented.\n");
                            break;

                        case "goodbye":
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
                                hostAssigned = false; // Allow new host if host leaves
                            }
                            clientList.remove(myClient);
                            myClient.connectionSock.close();
                            return;

                        default:
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
            if (myClient.username != null && myClient.username.equals("host")) {
                hostAssigned = false;
            }
            clientList.remove(myClient);
        }
    }
}
