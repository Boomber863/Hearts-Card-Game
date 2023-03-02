package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final NodeList nList = getXMLConfig();
    private ServerSocket serverSocket;

    /**
     * Assigns the ServerSocket for the server
     * @param socket a ServerSocket used by the server
     */
    public Server(ServerSocket socket){
        serverSocket = socket;
    }

    /**
     * Opens a configuration file written in XML.
     * Creates a NodeList object with order of game rounds.
     * @return the created NodeList object
     */
    public static NodeList getXMLConfig() {
        try {
            File fXmlFile = new File("config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("runda");
            return nList;
        } catch (Exception e){
            System.err.println("Nie można wczytać pliku konfiguracyjnego");
            return null;
        }
    }

    /**
     * Connects new clients to the server while it's open.
     * Every client connection is handled in a new Thread.
     */
    public void startServer(){
        try {
            while(!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Nowy klient połączony");
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch(IOException e){
            System.err.println("Nie można nasłuchiwać na porcie 4321");
        }
    }

    /**
     * Reads commands written in the server terminal in a seperate Thread
     * Executes them using a ClientHandler
     */
    public void readCommands(){
        BufferedReader terminal = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter toTerminal = new BufferedWriter(new OutputStreamWriter(System.out));
        ClientHandler server = new ClientHandler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!serverSocket.isClosed()){
                    try {
                        String message = terminal.readLine();
                        if(message.equals("list")) {
                            server.listEverything(toTerminal);
                        }
                        if(message.startsWith("endRound ")) {
                            String[] messageSplit = message.split(" ", 2);
                            if (messageSplit.length == 2) {
                                server.endRound(messageSplit[1]);
                            }
                        }
                    } catch (IOException | NumberFormatException e){
                        System.err.println("Błąd w poleceniach serwera");
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(4321);
            Server server = new Server(serverSocket);
            System.out.println("Serwer kierek jest online...");
            System.out.println("Komendy - list, endRound <id>");
            server.readCommands();
            server.startServer();
        } catch(IOException e){
            System.err.println("Nie można nasłuchiwać na porcie 4321");
        }
    }
}