/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.sikuli.script.FindFailed;

/**
 *
 * @author bcivel
 */
public class ExecuteSikuliAction extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ExecuteSikuliAction.class);

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

        /**
         * Check if picture folder exists to store the picture. If not, create
         * it.
         */
        File dir = new File("picture");

        if (!dir.exists()) {
            dir.mkdir();
        } else {
            FileDeleteStrategy.FORCE.delete(dir);
            dir.mkdir();
        }

        /**
         *
         */
        PrintStream os = null;
        try {
            LOG.info("Received: [Request from " + request.getServerName() + "]");

            /**
             * Get input information until the syntax |ENDS| is received Input
             * information expected is a JSON cast into String JSONObject
             * contains action, picture, text, defaultWait, pictureExtension
             */
            LOG.debug("Trying to open InputStream");
            BufferedReader is = new BufferedReader(new InputStreamReader(request.getInputStream()));

            //continue if BufferReader is not null, 
            //else, print message
            if (is.ready()) {

                os = new PrintStream(response.getOutputStream());
                String line = "";

                LOG.debug("Start reading InputStream");
                StringBuilder sb = new StringBuilder();
                while (!(line = is.readLine()).equals("|ENDS|")) {
                    sb.append(line);
                }

                /**
                 * Convert String into JSONObject
                 */
                LOG.debug("InputStream : " + sb.toString());

                JSONObject obj = new JSONObject(sb.toString());
                String action = obj.getString("action");
                String picture = obj.getString("picture");
                String text = obj.getString("text");
                int defaultWait = obj.getInt("defaultWait");
                String extension = obj.getString("pictureExtension");
                String start = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

                /**
                 * Init startTime and endTime for loop retry
                 */
                long start_time = System.currentTimeMillis();
                long end_time = start_time + defaultWait;

                /**
                 * Generate pictureName and Path if picture is not empty.
                 * PictureName is a timestamp to ensure new name for every
                 * action
                 */
                String picturePath = "";
                String logPictureInfo = "";
                if (!"".equals(picture)) {
                    String pictureName = new SimpleDateFormat("YYYY.MM.dd.HH.mm.ss.SSS").format(new Date()) + ".";
                    pictureName += extension;
                    picturePath = "picture" + File.separator + pictureName;

                    /**
                     * Decode picture and print it
                     */
                    byte[] data = Base64.decodeBase64(picture);
                    try (OutputStream stream = new FileOutputStream(picturePath)) {
                        stream.write(data);
                    }
                    //Update logPictureInfo with that info
                    logPictureInfo = ": on picture " + picturePath;
                }

                LOG.info("Executing: [" + action + logPictureInfo + "]");

                JSONObject actionResult = new JSONObject();
                actionResult.put("status", "Failed");
                SikuliAction sikuliAction = new SikuliAction();
                boolean actionSuccess = false;

                /**
                 * Loop on action until success or timeout
                 */
                while (System.currentTimeMillis() < end_time) {
                    try {
                        actionResult = sikuliAction.doAction(action, picturePath, text);
                        /**
                         * If action OK, break the loop. Else, log and try again
                         * until timeout
                         */
                        if (actionResult.has("status")) {
                            if ("OK".equals(actionResult.get("status"))) {
                                actionSuccess = true;
                                break;
                            }
                        }

                    } catch (FindFailed ex) {
                        LOG.debug("Element Not Found yet: " + ex);
                        LOG.info("Retrying again during " + (System.currentTimeMillis() - end_time) + " ms");
                    }
                }

                /**
                 * Log and return actionResult
                 */
                LOG.info(actionResult.get("status") + " [" + action + logPictureInfo + "] finish with result:" + actionResult.get("status"));
                os.println(actionResult.toString());
                os.println("|ENDR|");

            } else {
                LOG.info("ExecuteSikuliAction is up and running. Waiting stuff from Cerberus");
                response.getWriter().print("ExecuteSikuliAction is up and running. Waiting stuff from Cerberus");
            }

            //is.close();
            //os.close();
        } catch (IOException e) {
            LOG.warn("IOException : " + e);
            if (os != null) {
                os.println("Failed");
                os.println("|ENDR|");
            }
        } catch (JSONException ex) {
            LOG.warn("JSON Exception : " + ex);
            if (os != null) {
                os.println("Failed");
                os.println("|ENDR|");
            }
        } finally {
            FileDeleteStrategy.FORCE.delete(dir);
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
