package org.example;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.example.DeckOfCards.shuffleCards;
import static org.example.Server.nList;

public class Room {
    private final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private final int ID;
    private int roundNumber = 0;
    private int turnNumber;
    private int nextTurn = 0;
    private int playedCards;
    private String firstSuit;
    private final Card[] deck = DeckOfCards.getDeckOfCards();
    private final Card[][] playerHands = new Card[4][13];
    private final Card[] currentPot = new Card[4];
    private final int[] points = new int[4];
    private String gameStatus = "Oczekiwanie na graczy";

    /**
     * assigns parameters for the object
     * @param client connection with the client that created the room
     * @param ID ID of the room
     */
    public Room(ClientHandler client, int ID){
        this.ID = ID;
        clientHandlers.add(client);
    }

    /**
     * Returns ID of the room
     * @return ID of the room
     */
    public int getID(){
        return ID;
    }

    /**
     * Returns the number of the currently played round
     * @return  the number of the currently played round
     */
    public int getRoundNumber(){
        return roundNumber;
    }

    /**
     * returns the status of the game in a String
     * @return the status of the game in a String
     */
    public String getGameStatus(){
        return gameStatus;
    }

    /**
     * Returns the ArrayList of all connected clients in the room
     * @return the ArrayList of all connected clients in the room
     */
    public ArrayList<ClientHandler> getPlayers(){
        return clientHandlers;
    }

    /**
     * Sends the message to everyone in the room
     * @param message message to be sent
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        for(ClientHandler clientHandler : clientHandlers){
            clientHandler.getBufferedWriter().write(message);
            clientHandler.getBufferedWriter().newLine();
            clientHandler.getBufferedWriter().flush();
        }
    }

    /**
     * Adds the client handler to the list of handlers in the room
     * @param client the client connection
     */
    public void addClient(ClientHandler client){
        clientHandlers.add(client);
    }

    /**
     * Removes the client handler from the list of handlers in the room
     * @param client the client connection
     */
    public void removeClient(ClientHandler client){
        clientHandlers.remove(client);
    }

    /**
     * Returns the number of clients in the room
     * @return the number of clients in the room
     */
    public int getNumOfClients(){
        return clientHandlers.size();
    }

    /**
     * Returns the client whose turn it is to play
     * @return the client whose turn it is to play
     */
    public ClientHandler getCurrentPlayer(){
        return clientHandlers.get(nextTurn);
    }

    /**
     * Starts a new round of Hearts, shuffles the deck and randomizes the starting player
     * @throws IOException
     */
    public void startGame() throws IOException {
        gameStatus = "W trakcie gry";
        nextTurn = (int) (Math.random() * ((3) + 1));
        roundNumber++;
        turnNumber = 1;
        playedCards = 0;
        shuffleCards(deck);
        for(int i = 0, j = 0; i < 52; j++, i=i+4){
            playerHands[0][j] = deck[i];
            playerHands[1][j] = deck[i+1];
            playerHands[2][j] = deck[i+2];
            playerHands[3][j] = deck[i+3];
        }
        for(ClientHandler clientHandler : clientHandlers){
            clientHandler.setInGame();
        }

        sendMessage("Rozdanie " + roundNumber + " - " + getRoundFromFile());
        sendMessage("Teraz kolej - " + clientHandlers.get(nextTurn).getName());
        sendMessage("Twoje karty");
        int j = 0;
        for(ClientHandler clientHandler : clientHandlers){
            for(int i = 0; i < 13 ; i++){
                clientHandler.getBufferedWriter().write(playerHands[j][i].turnToString() + " ");
            }
            clientHandler.getBufferedWriter().newLine();
            clientHandler.getBufferedWriter().flush();
            j++;
        }
    }

    /**
     * Returns the round name from the configuration in an XML file
     * @return name of the round
     */
    public String getRoundFromFile(){
        assert nList != null;
        Node nNode = nList.item(roundNumber-1);
        Element eElement = (Element) nNode;
        return eElement.getElementsByTagName("nazwa").item(0).getTextContent();
    }

    /**
     * Plays the card if possible
     * Assigns the player for the next move
     * At the end of a round starts a new one
     * At the end of the game ends the game
     * @param message Card to be played
     * @return true if the card was played, false otherwise
     * @throws IOException
     */
    public boolean playCard(String message) throws IOException {
        boolean flag = true;
        if(playedCards == 0){
            if((getRoundFromFile().equals("Bez kierow") || getRoundFromFile().equals("Bez krola kier") || getRoundFromFile().equals("Rozbojnik")) && Character.toString(message.charAt(0)).equals("H")) return false;
            firstSuit = Character.toString(message.charAt(0));
        }
        else{
            if(!firstSuit.equals(Character.toString(message.charAt(0)))){
                for(int i = 0; i< 13; i++){
                    if(playerHands[nextTurn][i] != null) {if(firstSuit.equals(playerHands[nextTurn][i].getSuit())){
                        flag = false;
                    }}
                }
                if(!flag) return false;
            }

        }

        for(int i = 0; i< 13; i++){
            if(playerHands[nextTurn][i]!=null){ if(playerHands[nextTurn][i].turnToString().equals(message)){
                currentPot[nextTurn] = playerHands[nextTurn][i];
                playerHands[nextTurn][i] = null;
            }}
        }

        playedCards++;
        if(playedCards == 4){
            endTurn();
        }
        else{
            if(playedCards == 1) firstSuit = currentPot[nextTurn].getSuit();
            if(nextTurn == 0) nextTurn = 3;
            else nextTurn--;
            for(ClientHandler clientHandler : clientHandlers){
                clientHandler.getBufferedWriter().write("Aktualne karty: " );
                for(int i = 0; i< 4; i++) {
                    if(currentPot[i] != null) clientHandler.getBufferedWriter().write(currentPot[i].turnToString() + " ");
                }
                clientHandler.getBufferedWriter().newLine();
                clientHandler.getBufferedWriter().flush();
            }
            sendMessage("Teraz kolej - " + clientHandlers.get(nextTurn).getName());
            sendMessage("Twoje karty");
            int j = 0;
            for(ClientHandler clientHandler : clientHandlers){
                for(int i = 0; i < 13 ; i++){
                    if(playerHands[j][i] != null) clientHandler.getBufferedWriter().write(playerHands[j][i].turnToString() + " ");
                }
                clientHandler.getBufferedWriter().newLine();
                clientHandler.getBufferedWriter().flush();
                j++;
            }
        }

        return true;
    }

    /**
     * Ends the turn and clears the cards on the table
     * Assigns points to players depending on the current round
     * @throws IOException
     */
    public void endTurn() throws IOException {
        int max=0;
        int n=0;
        for(int i = 0; i<4; i++){
            if(currentPot[i].getSuit().equals(firstSuit) && currentPot[i].getValue() >= max){
                n = i;
                max = currentPot[i].getValue();
            }
        }

        switch(getRoundFromFile()) {
            case "Bez lew":
                points[n] = points[n] - 20;
                break;
            case "Bez kierow":
                for(int i = 0; i<4; i++){
                    if(currentPot[i].isHeart()){
                        points[n] = points[n] - 20;
                    }
                }
                break;
            case "Bez dam":
                for(int i = 0; i<4; i++){
                    if(currentPot[i].isQueen()){
                        points[n] = points[n] - 60;
                    }
                }
                break;
            case "Bez panow":
                for(int i = 0; i<4; i++){
                    if(currentPot[i].isMale()){
                        points[n] = points[n] - 30;
                    }
                }
                break;
            case "Bez krola kier":
                for(int i = 0; i<4; i++){
                    if(currentPot[i].isHeartKing()){
                        points[n] = points[n] - 150;
                    }
                }
                break;
            case "Bez siodmej i ostatniej":
                if(turnNumber == 7 || turnNumber == 1) points[n] = points[n] - 75;
                break;
            case "Rozbojnik":
                points[n] = points[n] - 20;
                for(int i = 0; i<4; i++){
                    if(currentPot[i].isHeart()){
                        points[n] = points[n] - 20;
                    }
                    if(currentPot[i].isQueen()){
                        points[n] = points[n] - 60;
                    }
                    if(currentPot[i].isMale()){
                        points[n] = points[n] - 30;
                    }
                    if(currentPot[i].isHeartKing()){
                        points[n] = points[n] - 150;
                    }
                }
                if(turnNumber == 7 || turnNumber == 1) points[n] = points[n] - 75;
                break;
        }

        turnNumber++;
        nextTurn = n;
        playedCards = 0;
        sendMessage("Punktacja: ");
        for (int j = 0; j<4; j++) sendMessage(clientHandlers.get(j).getName() + " - " + points[j]);
        if(turnNumber == 14){
            Arrays.fill(currentPot, null);
            if(roundNumber != 7) startGame();
            else endGame();
        }
        else{
            Arrays.fill(currentPot, null);
            sendMessage("Teraz kolej - " + clientHandlers.get(nextTurn).getName());
            sendMessage("Twoje karty");
            int j = 0;
            for(ClientHandler clientHandler : clientHandlers){
                for(int i = 0; i < 13 ; i++){
                    if(playerHands[j][i] != null) clientHandler.getBufferedWriter().write(playerHands[j][i].turnToString() + " ");
                }
                clientHandler.getBufferedWriter().newLine();
                clientHandler.getBufferedWriter().flush();
                j++;
            }
        }
    }

    /**
     * Checks if the player has the Card in his hand
     * @param message Card that's being checked
     * @return true if the lpayer has the Card, false otherwise
     */
    public boolean checkHand(String message){
        boolean flag = false;
        for(int i = 0; i < 13; i++){
            if(playerHands[nextTurn][i] != null){ if(playerHands[nextTurn][i].turnToString().equals(message)) flag = true;}
        }
        return flag;
    }

    /**
     * Ends the round by assigning random points to layers
     * If it's the last round ends the game
     * @throws IOException
     */
    public void endRound() throws IOException {
        sendMessage("Serwer pominal aktualne rozdanie");
        Arrays.fill(currentPot, null);
        playedCards = 0;

        sendMessage("Punktacja: ");
        for (int j = 0; j<4; j++){
            points[j] += (-100 + (int)(Math.random() * (100) + 1));
            sendMessage(clientHandlers.get(j).getName() + " - " + points[j]);
        }
        if(roundNumber != 7) startGame();
        else endGame();
    }

    /**
     * Ends the game, resetting it completely
     * The Room is reset to a regular room without a game in progress
     * @throws IOException
     */
    public void endGame() throws IOException {
        sendMessage("Koniec gry!");

        int max = points[0];
        int n = 0;
        for (int i = 1; i < 3; i++) {
            if(points[i] > max) {
                max = points[i];
                n = i;
            }
        }

        sendMessage("Wygrywa: " + clientHandlers.get(n).getName());
        Arrays.fill(currentPot, null);
        roundNumber = 0;
        playedCards = 0;
        for(ClientHandler ch : clientHandlers){
            ch.setInRoom();
        }
        gameStatus = "Oczekiwanie na graczy";
    }
}
