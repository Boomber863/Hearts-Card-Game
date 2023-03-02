package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;

import static org.example.ClientHandler.Status.*;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();
    public static int maxID = 0;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private int roomID;
    private String lastInvite;
    public enum Status { LOBBY, ROOM, GAME, INVITED }
    public Status status;


    public ClientHandler(){}

    /**
     * The constructor creates BufferedWriter and BufferedReader objects based on the connection with the client.
     * Reads the username sent by the client
     * Assigns parameters for the object.
     * @param socket Socket used to connect with the client
     */
    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();
            clientHandlers.add(this);
        } catch (IOException e) {
           closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Returns the BufferedWriter used to communicate with the client
     * @return the BufferedWriter used to communicate with the client
     */
    public BufferedWriter getBufferedWriter(){
        return bufferedWriter;
    }

    /**
     * Returns the username of the client
     * @return the username of the client
     */
    public String getName(){
        return username;
    }

    /**
     * While client is connected listens and executes commands from him
     * Different commands are available depending on the stat client
     */
    @Override
    public void run(){
        String message;
        status = LOBBY;

        while(socket.isConnected()){
            try {
                    message = bufferedReader.readLine();
                    switch(status) {
                        case GAME:
                            if (message.startsWith("chat ")) {
                                messageChat(message);
                            } else if (message.startsWith("play ")) {
                                playCard(message);
                            }
                            else if (message.startsWith("test")) {
                                sendMessage("test");
                            }
                            break;
                        case ROOM:
                            if (message.startsWith("chat ")) {
                                messageChat(message);
                            } else if (message.startsWith("invite ")) {
                                invitePlayer(message);
                            } else if (message.equals("start")) {
                                gameStart();
                            }
                            break;
                        case INVITED:
                            if (message.equals("Y")) {
                                acceptInvite(lastInvite);
                            } else if (message.equals("N")) {
                                status = LOBBY;
                            }
                            break;
                        default:
                            if (message.equals("create")) {
                                createRoom();
                            } else if (message.equals("list")) {
                                listRooms();
                            } else if (message.startsWith("join ")) {
                                joinRoom(message);
                            } else {
                                sendMessage("Niepoprawne polecenie");
                            }
                        }
                    } catch (IOException | NumberFormatException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    /**
     * Sets the status of the user to be playing the game
     */
    public void setInGame(){
        status = GAME;
    }

    /**
     * Sets the status of the user to be in a room
     */
    public void setInRoom(){
        status = ROOM;
    }

    /**
     * Changes the status of the user to invited and asks him to accept or decline
     * @param room Room the user was invited to
     * @throws IOException
     */
    public void receiveInvite(int room) throws IOException {
        status = INVITED;
        lastInvite = Integer.toString(room);
        sendMessage("Zostales zaproszony do pokju " + room);
        sendMessage("Czy chcesz zaakceptowac? Y/N ");
    }

    /**
     * Invites another player to the room of the user
     * @param message name of the player being invited
     * @throws IOException
     */
    public void invitePlayer(String message) throws IOException {
        String[] messageSplit = message.split(" ", 2);
        if (messageSplit.length == 2) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler.getName().equals(messageSplit[1]) && clientHandler.status != LOBBY)
                    sendMessage("Gracz jest juz w innym pokoju");
                else if(clientHandler.getName().equals(messageSplit[1]) && clientHandler.status == LOBBY){
                    clientHandler.receiveInvite(roomID);
                }
            }
        }
    }

    /**
     * Checks if it's users turn adn he can play that card.
     * Then plays the card
     * @param message the card the user wants to play
     * @throws IOException
     */
    public void playCard(String message) throws IOException {
        String[] messageSplit = message.split(" ", 2);
        for(Room room : rooms){
            if(room.getID() == roomID){
                if(this != room.getCurrentPlayer()){
                    sendMessage("To nie twoja kolej");
                }
                else{
                    if(!room.checkHand(messageSplit[1])) sendMessage("Nie masz takiej karty");
                    else if(!room.playCard(messageSplit[1])) sendMessage("Nie mozna zagrac tego koloru");
                }
            }
        }
    }

    /**
     * Sends a message to everyone in the room of the user
     * @param message message that user wants to send in chat
     * @throws IOException
     */
    public void messageChat(String message) throws IOException {
        String[] messageSplit = message.split(" ", 2);
        if(messageSplit.length == 2) {
            for(Room room : rooms){
                if(room.getID() == roomID){
                    room.sendMessage(username + ": " + messageSplit[1]);
                }
            }
        }
    }

    /**
     * Starts the game of Hearts in the users room if there are 4 layers
     * @throws IOException
     */
    public void gameStart() throws IOException {
        for(Room room : rooms){
            if(room.getID() == roomID){
                if(room.getNumOfClients() == 4) {
                    room.startGame();
                } else {
                    sendMessage("Za malo graczy w pokoju");
                }
            }
        }
    }

    /**
     * Creates a new room and adds the user to it
     * @throws IOException
     */
    public void createRoom() throws IOException {
        rooms.add(new Room(this, maxID + 1));
        sendMessage("Jestes w pokoju " + (maxID + 1));
        roomID = maxID + 1;
        setInRoom();
        maxID++;
    }

    /**
     * Adds the user to the room if it's not full
     * @param message ID of the room the user wants to join
     * @throws IOException
     * @throws NumberFormatException - if message is not a number
     */
    public void joinRoom(String message) throws IOException, NumberFormatException {
        String[] messageSplit = message.split(" ", 2);
        if (messageSplit.length == 2) {
            for(Room room : rooms){
                if(room.getID() == Integer.parseInt(messageSplit[1])){
                    if(room.getNumOfClients() == 4) {
                        sendMessage("Pokoj jest pelny");
                    }
                    else{
                        room.addClient(this);
                        sendMessage("Jestes w pokoju " + room.getID());
                        roomID = room.getID();
                        setInRoom();
                     }
                }
            }
        }
    }

    /**
     * Accepts the invite to the room and adds the user to it unless it's full
     * @param message ID of the room the user was invited to
     * @throws IOException
     * @throws NumberFormatException - if message is not a number
     */
    public void acceptInvite (String message) throws IOException, NumberFormatException {
        for(Room room : rooms){
            if(room.getID() == Integer.parseInt(message)){
                if(room.getNumOfClients() == 4) {
                    sendMessage("Pokoj jest pelny");
                    status = LOBBY;
                }
                else{
                    room.addClient(this);
                    sendMessage("Jestes w pokoju " + room.getID());
                    roomID = room.getID();
                    setInRoom();
                }
            }
        }
    }

    /**
     * Lists all rooms, users in them and the status of the game
     * @param toTerminal BufferedWriter sending  data to the server
     * @throws IOException
     */
    public void listEverything(BufferedWriter toTerminal) throws IOException {
        for(Room room : rooms) {
            toTerminal.write("Pokoj "+ room.getID());
            toTerminal.newLine();
            toTerminal.flush();

            toTerminal.write("Gracze: ");
            toTerminal.newLine();
            toTerminal.flush();

            ArrayList<ClientHandler> inRoom = room.getPlayers();
            for (ClientHandler c : inRoom){
                toTerminal.write(c.getName() + " ");
            }
            toTerminal.newLine();
            toTerminal.flush();

            toTerminal.write("Status: " + room.getGameStatus());
            toTerminal.newLine();
            toTerminal.flush();

            if(room.getGameStatus().equals("W trakcie gry")){
                toTerminal.write("Rozdanie: " + Integer.toString(room.getRoundNumber()));
                toTerminal.newLine();
                toTerminal.flush();
            }
        }
    }

    /**
     * Ends the current round in a room assigning random points to every user
     * @param message ID of the room
     * @throws IOException
     * @throws NumberFormatException - if message is not a number
     */
    public void endRound(String message) throws IOException, NumberFormatException {
        for(Room room : rooms) {
            if(room.getID() == Integer.parseInt(message)){
                room.endRound();
            }
        }
    }

    /**
     * Lists all created rooms on the server
     * @throws IOException
     */
    public void listRooms() throws IOException {
        for(Room room : rooms) {
            sendMessage("Pokoj "+ room.getID());
        }
    }

    /**
     * Sends a message to the client
     * @param message message to be sent
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    /**
     * removes the current ClientHandler from the list of all ClientHandlers on the server
     */
    public void removeClientHandler(){
        clientHandlers.remove(this);
    }

    /**
     * Closes the BufferedReader, BufferedWriter and Socket for the ClientHandler if they're open
     * Removes the client from a room if they're in one
     * @param socket Socket used by the client
     * @param bufferedReader BufferedReader that receives messages from the server
     * @param bufferedWriter BufferedWriter that sends messages to the server
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        for(Room room : rooms) {
            if(room.getID() == roomID){
                room.removeClient(this);
            }
        }
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
