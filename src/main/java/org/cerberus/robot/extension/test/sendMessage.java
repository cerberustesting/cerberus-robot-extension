/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public class sendMessage {

    public static void main(String[] args) throws IOException {

        try {
            Socket clientSocket = new Socket("localhost", 9001);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            PrintStream os = new PrintStream(clientSocket.getOutputStream());

            JSONObject obj = new JSONObject();
            obj.put("action", "click");
            obj.put("picture", "C:/Users/bcivel/Documents/Selenium.sikuli/1440785043698.png");

            os.println(obj.toString());
            os.println("|ENDS|");
            System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + "] Sent");

            String responseLine;
            while (!(responseLine = br.readLine()).equals("|ENDR|")) {
                System.out.println("Server: " + responseLine);
            }

// clean up:
// close the output stream
// close the input stream
// close the socket
            br.close();
            clientSocket.close();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch (IOException e) {
            System.out.println("No I/O");
        } catch (JSONException ex) {
            Logger.getLogger(sendMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
