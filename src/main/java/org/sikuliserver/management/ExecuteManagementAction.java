/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver.management;

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
import org.sikuliserver.version.Infos;

/**
 *
 * @author bcivel
 */
public class ExecuteManagementAction extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ExecuteManagementAction.class);

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

        // Forcing a garbage collection
        System.gc();

        PrintStream os = null;
        BufferedReader is = null;
        StringBuilder sb = new StringBuilder();

        try {
            LOG.info("Received: [Request from " + request.getServerName() + "]");

            /**
             * Get input information until the syntax |ENDS| is received Input
             * information expected is a JSON cast into String JSONObject
             * contains action, picture, text, defaultWait, pictureExtension
             */
            LOG.debug("Trying to open InputStream");
            is = new BufferedReader(new InputStreamReader(request.getInputStream()));

            //continue if BufferReader is not null, 
            //else, print message
            if (is.ready()) {

                os = new PrintStream(response.getOutputStream());
                String line = "";

                Infos infos = new Infos();
                LOG.debug("Start reading InputStream");
                while (!(line = is.readLine()).equals("|ENDS|")) {
                    sb.append(line);
                }

                /**
                 * Convert String into JSONObject
                 */
                LOG.debug("InputStream : " + sb.toString());

                LOG.info("Executing Management Request.");

                JSONObject actionResult = new JSONObject();
                actionResult.put("status", "OK");

                actionResult.put("version", infos.getProjectNameAndVersion());
                actionResult.put("buildid", infos.getProjectBuildId());
                actionResult.put("java.prop.log4j.logger", System.getProperty("log4j.logger"));
                actionResult.put("java.prop.java.io.tmpdir", System.getProperty("java.io.tmpdir"));

                actionResult.put("javaVersion", System.getProperty("java.version"));
                Runtime instance = Runtime.getRuntime();
                int mb = 1024 * 1024;
                actionResult.put("javaFreeMemory", instance.freeMemory() / mb);
                actionResult.put("javaTotalMemory", instance.totalMemory() / mb);
                actionResult.put("javaUsedMemory", (instance.totalMemory() - instance.freeMemory()) / mb);
                actionResult.put("javaMaxMemory", instance.maxMemory() / mb);

                String str1 = getServletContext().getServerInfo();
                actionResult.put("applicationServerInfo", str1);

                /**
                 * Log and return actionResult
                 */
                os.println(actionResult.toString(1));
                os.println("|ENDR|");

                is.close();
                os.close();

            } else {
                LOG.info("ExecuteManagementAction is up and running. Waiting for requests from Cerberus");
                response.getWriter().print("ExecuteManagementAction is up and running. Waiting for requests from Cerberus");
            }

        } catch (JSONException ex) {
            LOG.warn("JSON Exception : " + ex, ex);
            if (os != null) {
                os.println("{\"status\" : \"Failed\", \"message\" : \"Unsupported request to Extension\"}");
                os.println("|ENDR|");
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
                    os.println("|ENDR|");
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
