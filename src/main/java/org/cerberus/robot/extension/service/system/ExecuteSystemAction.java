/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.service.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public class ExecuteSystemAction extends HttpServlet {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_KO = "KO";
    private static final String STATUS_FA = "Failed";

    private static final Logger LOG = LogManager.getLogger(ExecuteSystemAction.class);

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

        PrintStream os = null;
        BufferedReader is = null;

        try {

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF8");

            JSONObject actionResult = new JSONObject();
            actionResult.put("status", "OK");

            StringBuilder sb = new StringBuilder();

            LOG.info("Received ExecuteSystemAction: [Request from {}]", request.getServerName());

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

            JSONObject obj = new JSONObject(sb.toString());

            String action = obj.getString("action");

            LOG.info("[{}] Executing...", action);
            String param1 = "";
            Boolean param1Boo = true;
            SystemAction systemAction = new SystemAction();
            
            switch (action) {
                case "getProcess":
                    /**
                     * We get all details of a process
                     */
                    if (obj.has("processName")) {
                        param1 = obj.getString("processName");
                    }
                    if (obj.has("filterStrict")) {
                        param1Boo = obj.getBoolean("filterStrict");
                    } else {
                        param1Boo = false;
                    }
                    LOG.info("[{}] Checking CPU from process name {} with strict filter {}.", action, param1, param1Boo);
                    actionResult = systemAction.getCPU(param1, param1Boo);

                    break;
                case "checkCertificate":
                    /**
                     * We get the details of a certificate
                     */
                    if (obj.has("url")) {
                        param1 = obj.getString("url");
                    }
                    LOG.info("[{}] Checking certificate details from {} .", action, param1);
                    actionResult = systemAction.checkCertificate(param1);

                    break;
                default:
                    actionResult.put("status", STATUS_FA);
                    actionResult.put("message", "Unknown or mising action '" + action + "'");
            }

            /**
             * Log and return actionResult
             */
            LOG.info("[{}]  finish with result: {}", action, actionResult.get("status"));
            os.println(actionResult.toString(1));

            is.close();
            os.close();

        } catch (JSONException ex) {
            LOG.warn("JSON Exception : " + ex, ex);
            if (os != null) {
                os.println("{\"status\" : \"Failed\", \"message\" : \"Unsupported request to Extension\"}");
            }
        } catch (Exception ex) {
            LOG.error("Exception : " + ex, ex);
            try {
                if (os != null) {
                    String message = ex.toString();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String stacktrace = sw.toString();
                    JSONObject result = new JSONObject();
                    result.put("status", STATUS_FA);
                    result.put("message", message);
                    result.put("stacktrace", stacktrace);
                    os.println(result.toString(1));
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
