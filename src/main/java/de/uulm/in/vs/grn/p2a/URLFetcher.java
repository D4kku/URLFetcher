package de.uulm.in.vs.grn.p2a;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;


public class URLFetcher{


    public static void main(String[] args) throws IOException{
        URL url = new URL("http://www.google.com/");
        Socket socket = new Socket(url.getHost(), (url.getPort() == -1) ? 80 : url.getPort()); //nimmt 80 an wenn der port nicht extra gesetzt wurde
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        InputStream in = socket.getInputStream();
        //kein plan warum das mit dem pdf nicht geht
        String request = String.format("GET %s HTTP/1.1\r\nHost: %s\r\n \r\n",url.getPath(),url.getHost());
        out.println(request);
        System.out.println(request);


        //Eigentlich ist der weg hier voll dumm weil ich alles parse und dann schaue was ich davon brauch but idc das funktiniert recht gut also who cares
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        while(!response.toString().endsWith("\r\n\r\n")) { //ist zwar wastefull af aber funkt sogar recht robust
            response.write(in.read());
        }
        String headerString = new String(response.toString());
        response.close();
        System.out.println(headerString);
        //Convert the headerString into an String array for ease of working with it
        String[] header = headerString.split("\n");

        String responseCode = header[0].split(" ")[1]; // ist nur die Zahl im response anfang um zu schauen ob das funktioniert hat oder nicht
        if(!Objects.equals(responseCode, "200")){
            throw new IllegalArgumentException("Bad Header" + header[0]);
        }
        String type;
        for(String s : header){
            if(s.contains("Content-Type")){
                type = s.split(" ")[1];
            }
        }

        //BufferedOutputStream writer = Files.newBufferedWriter()


        out.close();
        socket.close();
    }
}
