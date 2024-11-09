package de.uulm.in.vs.grn.p2a;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public class URLFetcher{


    public static void main(String[] args) throws IOException{
        //Das funktioniert legit nur mit google und den test seiten der uni da wir mit sehr alten http1.1 daher kommen, was niemand mehr mag
        URL url = new URL("http://vns.lxd-vs.uni-ulm.de/img/http.jpg");
        Socket socket = new Socket(url.getHost(), (url.getPort() == -1) ? 80 : url.getPort()); //nimmt 80 an, wenn der port nicht extra gesetzt wurde
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        InputStream in = socket.getInputStream();


        String request = String.format("GET %s HTTP/1.1\r\nHost: %s\r\n \r\n",url.getPath(),url.getHost());
        out.println(request);
        System.out.println(request);

        //Das sieht übelst dumm aus aber nen reader wirft die line ends automatisch weg
        //weshalb wir dann nach emtpy lines schauen müssen was halt False Positives uns geben kann, wenn z.b. statt line ends irgendein error beim Parsen passiert
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        while(!response.toString().endsWith("\r\n\r\n")) { //ist zwar wastefull af aber funkt sogar recht robust, es sei den daten gehen verloren dann time für nen infinite loop
            response.write(in.read());
        }
        String headerString = response.toString();
        response.close();
        System.out.println(headerString);

        /*Convert the headerString into an String array for ease of working with it
         *Jede Zeile des Headers ist Ein String im Array
         *Ich mach das deshalb so weil sonst die Values von dem response zu kriegen grauenhaft wird oder man regexs braucht und einfach nein
        */
        String[] header = headerString.split("\n");

        String responseCode = header[0].split(" ")[1]; // ist nur die Zahl im response anfang um zu schauen ob der Request funktioniert hat oder nicht
        if(!Objects.equals(responseCode, "200")){
            throw new IllegalArgumentException("\nReturned this Header: " + header[0]);
        }

        String contentType = "None";
        int contentLen = -1;
        for(String s : header) {
            if (s.contains("Content-Type:")){
                contentType = s.split("/")[1].strip(); //wenn du das strip nicht hast explodiert alles (kinda freaky ngl)
            }
            if (s.contains("Content-Length:"))
                contentLen = Integer.parseInt(s.split(":")[1].strip());//this looks worse than it is trust me
        }
        System.out.println(contentLen + " " + contentType);


        /* Der grund warum ich hier den Filename und den filetype seperat anschaue liegt daran das für den fall das der Path der website kein .html endung hat
        *  Was die Norm ist der Code sonst massivst brechen würde und die datei ohne eine endung speichern
        *  Ansich wäre das oke weil für unseren zweck das ausreicht aber das ist besser isch
        */
        //Ja das ist cursed aber ich will es einfach funktionierend haben und um das schön zu machen muss ich viel umschreiben bzw dinge wieder verwenden
        String fileName = url.getPath().split("/")[url.getPath().split("/").length-1].split("\\.")[0];

        byte[] fileData = in.readNBytes(contentLen);
        Path file = Paths.get(fileName + "." + contentType);
        System.out.println(file.toAbsolutePath());
        Files.write(file,fileData);


        in.close();
        out.close();
        socket.close();
    }
}
