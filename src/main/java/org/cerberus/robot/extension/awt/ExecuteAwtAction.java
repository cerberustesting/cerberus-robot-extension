/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.awt;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.sikuli.script.FindFailed;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author bcivel
 */
public class ExecuteAwtAction extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ExecuteAwtAction.class);
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

        PrintStream os = null;
        BufferedReader is = null;
        StringBuilder sb = new StringBuilder();

        try {
            LOG.info("Received ExecuteAwtAction: [Request from {}]", request.getServerName());

            /**
             * Get input information until the syntax |ENDS| is received Input
             * information expected is a JSON cast into String JSONObject
             * contains action, picture, text, defaultWait, pictureExtension
             */
            LOG.debug("Trying to open InputStream");
            is = new BufferedReader(new InputStreamReader(request.getInputStream()));
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
                obj.put("text", "");
                obj.put("defaultWait", 0);
            }

            String action = obj.getString("action");
            String text = obj.getString("text");
            String text2 = "";
            if (obj.has("text2")) {
                text2 = obj.getString("text2");
            }
            int defaultWait = obj.getInt("defaultWait");

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

            /**
             * Init startTime and endTime for loop retry
             */
            long start_time = System.currentTimeMillis();
            long end_time = start_time + defaultWait;

            LOG.info("Executing: [" + action + "]");

            JSONObject actionResult = new JSONObject();
            actionResult.put("status", "KO");
            AwtAction awtAction = new AwtAction();

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
                    actionResult = awtAction.doAction(action, text, text2, typeDelayD);
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
            LOG.info("[" + action + "] finish with result: " + actionResult.get("status"));
            if (action.equals("endExecution")) {
                LOG.info("----------------------------------------------------------------------");
            }
            os.println(actionResult.toString());

            is.close();
            os.close();
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
