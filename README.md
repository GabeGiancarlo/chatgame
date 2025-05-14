# chatgame
A copy of the mtchat repo.  

This repo contains programs to implement a multi-threaded TCP chat server and client

* MtClient.java handles keyboard input from the user.
* ClientListener.java receives responses from the server and displays them.
* MtServer.java listens for client connections and creates a ClientHandler for each new client.
* ClientHandler.java receives messages from a client and relays it to the other clients.
* Client.java stores information about each connected client including their socket and username.
* ClientHandler.java will store the first connected user as the "host" of the game.
* ClientHandler.java will explain the directions of the game to the host and the SCORES, WHO, and GOODBYE commands to any user.
* ClientHandler.java prompts users to type a username, which must be unique, and notifies the client when their username is accepted.
* ClientHandler.java allows the host will send a multiple-choice or true/false question to the other clients.
* The first client to submit a correct answer will score points assigned by the host by typing @username points.

## Identifying Information

* Name:Gabriel Giancarlo
* Student ID: 2405449 [ID]
* Email:Giancarlo@gmail.com [Email]
* Course: CPSC 353
* Assignment: Chat Game Project

* Name: Dylan Massaro
* Student ID: 002444410
* Email: dmassaro@chapman.edu
* Course: CPSC 353
* Assignment: Chat Game Project 

* Name: Irene Ichwan
* Student ID: 002452538
* Email: ichwan@chapman.edu
* Course: CPSC 353
* Assignment: Chat Game Project 

*ADD YOUR INFO HERE

## Contributions
### Team Member 1: Gabriel Giancarlo
* Created the Client class
* Updated the README.md file
* Modified ClientHandler.java to assign host clients and explain the rules of the game
* Modified ClientHandler.java to display the list of commands
* Handled merge conflicts within ClientHandler.java

### Team Member 2: Dylan Massaro
* Modified MtServer.java to use the Client class
* Changed the server port to 9004
* Modified ClientHandler.java to have the functionalities of the trivia game

### Team Member 3: Irene
* Modified ClientHandler.java to handle unique usernames
* Updated MtClient.java and ClientListener.java
* Modified ClientHandler.java to contain the scores
* Modified ClientHandler.java to handle the Goodbye and Who commands

## Source Files

* Client.java
* MtServer.java
* MtClient.java
* ClientHandler.java
* ClientListener.java

## References

* Original chatgame repository from ChapmanCPSC353

## Known Errors

* None

## Build Instructions

* Compile all java files: `javac *.java`

## Execution Instructions

* Start the server first: `java MtServer`
* Start multiple clients: `java MtClient`
* Follow prompts to enter username and begin chatting
