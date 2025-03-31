# chatgame
A copy of the mtchat repo.  

This repo contains programs to implement a multi-threaded TCP chat server and client

* MtClient.java handles keyboard input from the user.
* ClientListener.java receives responses from the server and displays them
* MtServer.java listens for client connections and creates a ClientHandler for each new client
* ClientHandler.java receives messages from a client and relays it to the other clients.
* Client.java stores information about each connected client including their socket and username

## Identifying Information

* Name:Gabriel Giancarlo [Team Member 3]
* Student ID: 2405449 [ID]
* Email:Giancarlo@gmail.com [Email]
* Course: CPSC 353
* Assignment: Chat Game Project

* Name: Dylan Massaro
* Student ID: 002444410
* Email: dmassaro@chapman.edu
* Course: CPSC 353
* Assignment: Chat Game Project 

*ADD YOUR INFO HERE

## Contributions
### Team Member 1: Gabriel Giancarlo
* Created the Client class
* Updated the README.md file

### Team Member 2: Dylan Massaro
* Modified MtServer.java to use the Client class
* Changed the server port to 9004

### Team Member 3: Irene
* Modified ClientHandler.java to handle usernames
* Updated MtClient.java and ClientListener.java

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
