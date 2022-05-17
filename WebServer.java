import java.io.*;
import java.net.*;
import java.util.*;

class WebServer {
    public static void main(String argv[]) throws Exception {
        ServerSocket MainSocket;
        try {
            MainSocket = new ServerSocket(8000);
            while (true) {
                System.out.println("Waiting...");
                Socket connectionSocket = MainSocket.accept();
		        System.out.println("New client connected"+ connectionSocket);
                new HttpRequest(connectionSocket).start();
            }
        }
        catch (IOException e) {
            System.out.println("Could not listen on port 8000");
        }
    }
}
