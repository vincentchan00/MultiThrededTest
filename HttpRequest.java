import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.Thread;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.text.DateFormat;

public class HttpRequest extends Thread {
    Socket socket;

    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        String fileName, httpResponseType;
        FileInputStream inFile;
        int numOfBytes;
        File file;
        String requestMessageLine;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE,d MMM yyyy HH:mm:ss zzz");

        BufferedReader inFromClient =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
        requestMessageLine = inFromClient.readLine();
        String line = inFromClient.readLine();
        Date ifModifiedSince = null;
        while ((line = inFromClient.readLine()) != null) {
            if (line.equals("")) {
                break;
            }
            String[] requestHeaderArr = line.split(": ",2);
            if (requestHeaderArr[0].equals("If-Modified-Since")) {
                 ifModifiedSince = sdf.parse(requestHeaderArr[1]);
            }
        }

        StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
        DataOutputStream outToClient =
                new DataOutputStream(socket.getOutputStream());
        String requestMethod = tokenizedLine.nextToken();
        try {
            if (requestMethod.equals("GET") || requestMethod.equals("HEAD")) {
                fileName = tokenizedLine.nextToken();
                if (fileName.startsWith("/") == true)
                    fileName = fileName.substring(1);
                file = new File(fileName);
                numOfBytes = (int) file.length();
                inFile = new FileInputStream(fileName);
                httpResponseType = "HTTP/1.0 200 Document Follows\r\n";
                Date fileModifiedDate = new Date(file.lastModified());;

                int result = 0;
                if(ifModifiedSince != null)
                    result = ifModifiedSince.compareTo(fileModifiedDate);
                if(result < 0){
                    httpResponseType = "HTTP/1.1 304 Not Modified \r\n";
                }
            } else {
                System.out.println("\n400 Bad Request\n");
                fileName = "error400.html";
                file = new File(fileName);
                numOfBytes = (int) file.length();
                inFile = new FileInputStream(fileName);
                httpResponseType = "HTTP/1.1 400 Bad Request \r\n";
            }
        } catch (FileNotFoundException e) {
            System.out.println("\n404 File Not Found\n");
            fileName = "error404.html";
            file = new File(fileName);
            numOfBytes = (int) file.length();
            inFile = new FileInputStream(fileName);
            httpResponseType = "HTTP/1.1 404 File Not Found \r\n";
        }

        byte[] fileInBytes = new byte[numOfBytes];
        inFile.read(fileInBytes);
        outToClient.writeBytes(httpResponseType);
        outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
        outToClient.writeBytes("last-modified: " + sdf.format(file.lastModified()) + "\r\n");
        outToClient.writeBytes("\r\n");
        if (requestMethod.equals("GET")) {
            outToClient.write(fileInBytes, 0, numOfBytes);
        }
        String ip = socket.getRemoteSocketAddress().toString();
        String logContent = "ip address:" + ip + " File:" + fileName + " ResponseType:" + httpResponseType;
        socket.close();
        writeLog(logContent);

    }

    public void writeLog(String logContent) {
        Logger logger = Logger.getLogger("System");
        logger.setUseParentHandlers(false);
        FileHandler fh;

        try {
            fh = new FileHandler("System.log", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info(logContent);
            fh.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        // the following statement is used to log any messages
    }
}
