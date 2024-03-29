/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.sikuli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileDeleteStrategy;
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
    private static final String TYPEDELAY_DEFAULT = "0.1";
    private static final String MINSIMILARITY_DEFAULT = "0.7";

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF8");

        /**
         * Check if picture folder exists to store the picture. If not, create
         * it.
         */
        String rootPath = "";
        if (System.getProperty("java.io.tmpdir") != null) {
            rootPath = System.getProperty("java.io.tmpdir");
        } else {
            String sep = "" + File.separatorChar;
            LOG.info(sep);
            if (sep.equalsIgnoreCase("/")) {
                rootPath = "/tmp";
            } else {
                rootPath = "C:";
            }
            LOG.warn("Java Property for temporary folder not defined. Default to :" + rootPath);
        }

        String rootPictureFolder = rootPath + File.separator + "picture";
        File dir = new File(rootPictureFolder);

        if (!dir.exists()) {
            dir.mkdir();
        } else {
            FileDeleteStrategy.FORCE.delete(dir);
            dir.mkdir();
        }

        // Forcing a garbage collection
        System.gc();

        PrintStream os = null;
        BufferedReader is = null;
        StringBuilder sb = new StringBuilder();

        try {
            LOG.info("Received ExecuteSikuliAction: [Request from {}]", request.getServerName());

            /**
             * Get input information until the syntax |ENDS| is received Input
             * information expected is a JSON cast into String JSONObject
             * contains action, picture, text, defaultWait, pictureExtension
             */
            LOG.debug("Trying to open InputStream");
            is = new BufferedReader(new InputStreamReader(request.getInputStream()));

            //continue if BufferReader is not null, 
            //else, print message
//            if (is.ready()) {
            os = new PrintStream(response.getOutputStream());
            String line = "";

            LOG.debug("Start reading InputStream");
            while ((line = is.readLine()) != null) {
                sb.append(line);
            }

            /**
             * Convert String into JSONObject
             */
            LOG.debug("InputStream : " + sb.toString());

            JSONObject obj;
            if (sb.toString() != null && !"".equals(sb.toString())) {
                obj = new JSONObject(sb.toString());
            } else {
                obj = new JSONObject();
                obj.put("action", "ping");
                obj.put("picture", "");
                obj.put("text", "");
                obj.put("defaultWait", 0);
                obj.put("pictureExtension", "");
                obj.put("picture", "unknown");
            }

            String action = obj.getString("action");
            String picture = obj.getString("picture");
            String picture2 = "";
            if (obj.has("picture2")) {
                picture2 = obj.getString("picture2");
            }
            String text = obj.getString("text");
            String text2 = "";
            if (obj.has("text2")) {
                text2 = obj.getString("text2");
            }
            int defaultWait = obj.getInt("defaultWait");
            String extension = obj.getString("pictureExtension");
            String extension2 = "";
            if (obj.has("picture2Extension")) {
                extension2 = obj.getString("picture2Extension");
            }

            String minSimilarity = MINSIMILARITY_DEFAULT;
            Double minSimilarityD = Double.valueOf(minSimilarity);
            if (obj.has("minSimilarity")) {
                if (!obj.getString("minSimilarity").trim().equals("")) {
                    minSimilarity = obj.getString("minSimilarity");
                    try {
                        minSimilarityD = Double.valueOf(minSimilarity);
                        LOG.debug("Setting minSimilarity to : " + minSimilarity);
                    } catch (NumberFormatException e) {
                        minSimilarityD = Double.valueOf(MINSIMILARITY_DEFAULT);
                        LOG.error("minSimilarity parameter format is not valid : " + minSimilarity + " - Should be in double format (ex : 0.7). Value default to " + MINSIMILARITY_DEFAULT);
                    }
                }
            } else {
                LOG.info("minSimilarity parameter format is not defined. Value default to " + MINSIMILARITY_DEFAULT);
            }

            String typeDelay = TYPEDELAY_DEFAULT;
            Double typeDelayD = Double.valueOf(typeDelay);
            if (obj.has("typeDelay")) {
                if (!obj.getString("typeDelay").trim().equals("")) {
                    typeDelay = obj.getString("typeDelay");
                    try {
                        typeDelayD = Double.valueOf(typeDelay);
                        LOG.debug("Setting typeDelay to : " + typeDelay);
                    } catch (NumberFormatException e) {
                        typeDelayD = Double.valueOf(TYPEDELAY_DEFAULT);
                        LOG.error("typeDelay parameter format is not valid : " + typeDelay + " - Should be in double format (ex : 0.1). Value default to " + TYPEDELAY_DEFAULT);
                    }
                }
            } else {
                LOG.info("typeDelay parameter format is not defined. Value default to " + TYPEDELAY_DEFAULT);
            }

            int highlightElement = 0;
            if (obj.has("highlightElement")) {
                if (!obj.getString("highlightElement").trim().equals("")) {
                    String sHighlightElement = obj.getString("highlightElement");
                    highlightElement = Integer.valueOf(sHighlightElement);
                }
            }
            int xOffset = 0;
            if (obj.has("xOffset")) {
                xOffset = obj.getInt("xOffset");
            }
            int yOffset = 0;
            if (obj.has("yOffset")) {
                yOffset = obj.getInt("yOffset");
            }
            int xOffset2 = 0;
            if (obj.has("xOffset2")) {
                xOffset2 = obj.getInt("xOffset2");
            }
            int yOffset2 = 0;
            if (obj.has("yOffset2")) {
                yOffset2 = obj.getInt("yOffset2");
            }

            /**
             * Init startTime and endTime for loop retry
             */
            long start_time = System.currentTimeMillis();
            long end_time = start_time + defaultWait;

            /**
             * Generate pictureName and Path if picture is not empty.
             * PictureName is a timestamp to ensure new name for every action
             */
            String picturePath = "";
            String logPictureInfo = "";
            if (!"".equals(picture)) {
                String pictureName = new SimpleDateFormat("YYYY.MM.dd.HH.mm.ss.SSS").format(new Date());
                if (extension.startsWith(".")) {
                    pictureName += extension;
                } else {
                    pictureName += "." + extension;

                }
                picturePath = rootPictureFolder + File.separator + pictureName;

                /**
                 * Decode picture and print it
                 */
                byte[] data = Base64.decodeBase64(picture);
                try (OutputStream stream = new FileOutputStream(picturePath)) {
                    stream.write(data);
                }
                //Update logPictureInfo with that info
                logPictureInfo = ": on picture " + picturePath;
            } else if (!"".equals(text)) {
                logPictureInfo = ": on text '" + text + "'";
            }

            String picture2Path = "";
            if (!"".equals(picture2)) {
                String picture2Name = new SimpleDateFormat("YYYY.MM.dd.HH.mm.ss.SSS").format(new Date());
                if (extension2.startsWith(".")) {
                    picture2Name += extension2;
                } else {
                    picture2Name += "." + extension2;

                }
                picture2Path = rootPictureFolder + File.separator + picture2Name;

                /**
                 * Decode picture and print it
                 */
                byte[] data = Base64.decodeBase64(picture2);
                try (OutputStream stream = new FileOutputStream(picture2Path)) {
                    stream.write(data);
                }
                //Update logPictureInfo with that info
                logPictureInfo += " and picture " + picture2Path;
            } else if (!"".equals(text2)) {
                logPictureInfo += ": and text '" + text2 + "'";
            }

            LOG.info("Executing: [" + action + logPictureInfo + "]");

            JSONObject actionResult = new JSONObject();
            actionResult.put("status", "KO");
            SikuliAction sikuliAction = new SikuliAction();

            boolean breakOccured = false;
            boolean errorOccured = false;
            String message = null;
            String stacktrace = null;

            /**
             * Loop on action until success or timeout
             */
            int i = 0;
            while (System.currentTimeMillis() < end_time && i++ < 500) {
                try {
                    actionResult = sikuliAction.doAction(action, picturePath, picture2Path, text, text2, minSimilarityD, typeDelayD, highlightElement, rootPictureFolder, xOffset, yOffset, xOffset2, yOffset2);
                    if (actionResult.toString().length() > 300) {
                        LOG.debug("JSON Result from Action : " + actionResult.toString().substring(0, 300) + "...");
                    } else {
                        LOG.debug("JSON Result from Action : " + actionResult.toString());
                    }
                    /**
                     * If action OK, break the loop. Else, log and try again
                     * until timeout
                     */
                    if (actionResult.has("status")) {
                        if (action.equals("exists")) {
                            if ("OK".equals(actionResult.get("status"))) {
                                breakOccured = true;
                                LOG.debug("Break retry loop on exists action. (Element has been found)");
                                break;
                            }
                        } else if (action.equals("notExists")) {
                            if ("KO".equals(actionResult.get("status"))) {
                                breakOccured = true;
                                LOG.debug("Break retry loop on notExists action (Element has been found)");
                                break;
                            }

                        } else {
                            if ("OK".equals(actionResult.get("status"))) {
                                breakOccured = true;
                                LOG.debug("Break retry for default action");
                                break;
                            }
                        }
                    } else {
                        LOG.debug("Missing status entry from JSON.");
                    }
                    LOG.info("Retrying again during " + (end_time - System.currentTimeMillis()) + " ms");

                } catch (FindFailed ex) {
                    LOG.debug("Element Not Found yet: " + ex);
                    LOG.info("Retrying again during " + (end_time - System.currentTimeMillis()) + " ms");

                } catch (Exception ex) {
                    LOG.error("General Exception : ", ex);
                    message = ex.toString();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    stacktrace = sw.toString();
                    actionResult.put("message", message);
                    actionResult.put("stacktrace", stacktrace);
                    actionResult.put("status", "Failed");
                    errorOccured = true;
                    break;
                }
            }

            if (action.equals("exists") && !breakOccured && !errorOccured) {
                LOG.debug("We looped until the end never finding the element so can conclude it is not there. Exists --> KO");
                actionResult.put("status", "KO");
            }
            if (action.equals("notExists") && !breakOccured && !errorOccured) {
                LOG.debug("We looped until the end never finding the element so can conclude it is not there. NotExists --> OK");
                actionResult.put("status", "OK");
            }

            /**
             * Log and return actionResult
             */
            LOG.info("[" + action + logPictureInfo + "] finish with result: " + actionResult.get("status"));
            if (action.equals("endExecution")) {
                LOG.info("----------------------------------------------------------------------");
            }
            os.println(actionResult.toString());

            is.close();
            os.close();

//            } else {
//                LOG.info("ExecuteSikuliAction is up and running. Waiting for requests from Cerberus");
//                response.getWriter().print("ExecuteSikuliAction is up and running. Waiting for requests from Cerberus");
//            }
        } catch (JSONException ex) {
            LOG.warn("JSON Exception : " + ex, ex);
            if (os != null) {
                os.println("{\"status\" : \"Failed\", \"message\" : \"Unsupported request to Extension\"}");
            }
        } catch (Exception ex) {
            LOG.error("Exception : " + ex);
            try {
                if (os != null) {
                    String message = ex.toString();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String stacktrace = sw.toString();
                    JSONObject result = new JSONObject();
                    result.put("status", "Failed");
                    result.put("message", message);
                    result.put("stacktrace", stacktrace);
                    os.println(result.toString());
                }
            } catch (JSONException ex1) {
                LOG.error(ex1, ex1);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    LOG.error("Exception when closing output stream.", e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    LOG.error("Exception when closing output stream.", e);
                }
            }
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
