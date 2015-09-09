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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.sikuli.script.FindFailed;

/**
 *
 * @author bcivel
 */
public class ExecuteSikuliAction extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pictureName = "sikuliPicture.png";
        try {
            System.out.println(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) 
                    + " INFO - Received: [Request from " + request.getServerName()+"]");

            BufferedReader is = new BufferedReader(new InputStreamReader(
                    request.getInputStream()));
            PrintStream os = new PrintStream(response.getOutputStream());
            String line = "";

            StringBuilder sb = new StringBuilder();
            while (!(line = is.readLine()).equals("|ENDS|")) {
                sb.append(line);
            }

            JSONObject obj = new JSONObject(sb.toString());

            String action = obj.getString("action");
            String picture = obj.getString("picture");
            String text = obj.getString("text");
            String start = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

//            System.out.println(start + " INFO - Receiving picture [" 
//                    + picture.substring(1, 100) + "...] and naming it sikuliPicture.png");
            byte[] data = Base64.decodeBase64(picture);
            try (OutputStream stream = new FileOutputStream(pictureName)) {
                stream.write(data);
            }
            
            System.out.println(start + " INFO - Executing: [" + action + ": on picture ./"+pictureName+"]");

            SikuliAction sikuliAction = new SikuliAction();
            sikuliAction.doAction(action, pictureName, text);

            String end = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            System.out.println(end + " INFO - Done [" + action + "]");
            os.println(end + " INFO - End of action [" + action + "]");
            os.println("|ENDR|");
        } catch (IOException e) {
            System.out.println("No I/O");
        } catch (FindFailed ex) {
            Logger.getLogger(QueueReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(QueueReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
