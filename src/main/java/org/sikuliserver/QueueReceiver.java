/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.sikuli.script.FindFailed;

/**
 *
 * @author bcivel
 */
public class QueueReceiver {

    public static void main(String[] args) throws IOException {

        int port = 9001;
        if (args.length>1 && args[0].equals("-p")){
        port = Integer.valueOf(args[1]);
        }
        
        System.out.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())+"] Launching a SikuliServer on port : "+port);
        ServerSocket serverSocket = new ServerSocket(port);
        
        System.out.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())+"] Server Started and Listening...");
        
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())+"] Connected to the clientSocket "+clientSocket.getLocalAddress().getHostName());
                
                BufferedReader is = new BufferedReader(new InputStreamReader(
                        clientSocket.getInputStream()));
                PrintStream os = new PrintStream(clientSocket.getOutputStream());
                String line = "";
                
                StringBuilder sb = new StringBuilder();
                while (!(line = is.readLine()).equals("|ENDS|")) {
                    sb.append(line);
                }
                System.out.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())+"] Received :"+sb.toString());
                JSONObject obj = new JSONObject(sb.toString());
                
                String action = obj.getString("action");
                String picture = obj.getString("picture");
                String text = obj.getString("text");
                String start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                System.out.println("["+start+"] Starting to perform :"+sb.toString());
                
                if (action.equals("click") || action.equals("wait")) {
                    URL url = new URL("file:///" + picture);
                    InputStream istream = url.openStream();
                //byte[] bytes = IOUtils.toByteArray(istream);
                    //String imageDataString = Base64.encodeBase64URLSafeString(bytes);

                    //byte[] data = Base64.decodeBase64(imageDataString);
                    try (OutputStream stream = new FileOutputStream("sikuliPicture.png")) {
                        //stream.write(data);
                        stream.write(IOUtils.toByteArray(istream));
                    }
                }
                
                SikuliAction sikuliAction = new SikuliAction();
                sikuliAction.doAction(action, picture, text);
                
                
                String end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                System.out.println("["+end+"] End of action :"+action+"("+picture+")");
                os.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())+"] Done");
                os.println("|ENDR|");
            }
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch (IOException e) {
            System.out.println("No I/O");
        } catch (FindFailed ex) {
            Logger.getLogger(QueueReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(QueueReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
