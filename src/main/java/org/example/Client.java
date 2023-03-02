package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    /**
     * The constructor creates BufferedWriter and BufferedReader objects based on the server connection with the socket.
     * Assigns parameters for the object.
     * @param socket Socket used to connect to the server
     * @param username Username used by the client on the server
     */
    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    /**
     * Sends messages written by the client to the server
     * Starts with the username, then shows the client possible options and accepts them
     */
    public void sendMessage(){
       try{
           bufferedWriter.write(username);
           bufferedWriter.newLine();
           bufferedWriter.flush();
           Scanner scanner = new Scanner(System.in);
           System.out.println("Opcje - list, create, join <ID> / w pokoju - invite <name>, chat <msg>, start / podczas gry - play <karta>, chat <msg>");
           while(socket.isConnected()){
               String message = scanner.nextLine();
               bufferedWriter.write(message);
               bufferedWriter.newLine();
               bufferedWriter.flush();
           }
       } catch (IOException e) {
           closeEverything(socket, bufferedReader, bufferedWriter);
       }
    }

    /**
     * Listens for messages coming from the server in a separate Thread.
     * When a message is received it shows it to the client.
     */
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;

                while (socket.isConnected()){
                    try {
                        message = bufferedReader.readLine();
                        System.out.println(message);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    /**
     * Closes the BufferedReader, BufferedWriter and Socket for the client if they're open
     * @param socket Socket used by the client
     * @param bufferedReader BufferedReader that receives messages from the server
     * @param bufferedWriter BufferedWriter that sends messages to the server
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Podaj nazwe uzytkownika");
        String username = scanner.nextLine();
        try {
            Socket socket = new Socket("localhost", 4321);
            Client client = new Client(socket,username);
            client.listenForMessage();
            client.sendMessage();
        } catch(IOException e){
            System.err.println("Nie można połączyć z serwerem");
        }
    }
}
