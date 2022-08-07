/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver.filemanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public class ExecuteFilemanagementAction extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ExecuteFilemanagementAction.class);
    private static final String DATE_FORMAT = "YYYY.MM.dd:HH.mm.ss";
    private static final int MAX_SIZE = 100000000; // 100 Mega bytes

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

            JSONObject actionResult = new JSONObject();
            actionResult.put("status", "OK");

            StringBuilder sb = new StringBuilder();

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

                LOG.debug("Start reading InputStream");
                while (!(line = is.readLine()).equals("|ENDS|")) {
                    sb.append(line);
                }

                /**
                 * Convert String into JSONObject
                 */
                LOG.debug("InputStream : " + sb.toString());

                JSONObject obj = new JSONObject(sb.toString());

                String action = obj.getString("action");

                LOG.info("Executing: [" + action + "]");
                String path = "";
                String filename = "";
                String opt = "";
                String contentBase64 = "";
                File pathDir;
                switch (action) {
                    case "upload":
                        /**
                         * We get content of the file in Baase64 format, write
                         * it to a file, optionaly purge the corresponding
                         * folder before.
                         */
                        if (obj.has("path")) {
                            path = obj.getString("path");
                        }
                        if (obj.has("filename")) {
                            filename = obj.getString("filename");
                        }
                        if (obj.has("option")) {
                            opt = obj.getString("option");
                        }
                        if (obj.has("contentBase64")) {
                            contentBase64 = obj.getString("contentBase64");
                        }
                        LOG.info("Saving local file '{}' to path '{}' with option '{}'.", filename, path, opt);
                        actionResult = upload_files(path, filename, opt, contentBase64);

                        break;
                    case "download":
                        /**
                         * We get local content of the file in Baase64 format,
                         * It could be the last file generated.
                         */
                        if (obj.has("path")) {
                            path = obj.getString("path");
                        }
                        if (obj.has("option")) {
                            opt = obj.getString("option");
                        }
                        int nbfiles = 1;
                        if (obj.has("nbfiles")) {
                            nbfiles = obj.getInt("nbfiles");
                        }
                        if (obj.has("filename")) {
                            filename = obj.getString("filename");
                        }
                        LOG.info("Getting {} local file(s) '{}' from path '{}' with option '{}'.", nbfiles, filename, path, opt);
                        actionResult = download_files(path, opt, nbfiles, filename);

                        break;
                    default:
                        actionResult.put("status", "Failed");
                        actionResult.put("message", "Unknown or mising action '" + action + "'");
                }

                /**
                 * Log and return actionResult
                 */
                LOG.info("[" + action + "] finish with result: " + actionResult.get("status"));
                os.println(actionResult.toString(1));
                os.println("|ENDR|");

                is.close();
                os.close();

            } else {
                LOG.info("ExecuteFilemanagementAction is up and running. Waiting for requests from Cerberus");
                response.getWriter().print("ExecuteFilemanagementAction is up and running. Waiting for requests from Cerberus");
            }

        } catch (JSONException ex) {
            LOG.warn("JSON Exception : " + ex, ex);
            if (os != null) {
                os.println("{\"status\" : \"Failed\", \"message\" : \"Unsupported request to Extension\"}");
                os.println("|ENDR|");
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

    private JSONObject upload_files(String path, String filename, String opt, String contentBase64) throws JSONException, IOException {
        JSONObject actionResult = new JSONObject();
        if (!contentBase64.isEmpty()) {
            // We create the folder that will host the file if it does not exist.
            File pathDir = new File(path);
            if (!pathDir.exists()) {
                LOG.info("Path '{}' does not exist. We create it.");
                pathDir.mkdirs();
            } else {
                if ("EMPTYFOLDER".equals(opt)) {
                    File[] rawfiles = pathDir.listFiles();
                    int i = 0;
                    for (File rawfile : rawfiles) {
                        rawfile.delete();
                        i++;
                    }
                    actionResult.put("totalFilesDeleted", i);
                }
            }
            // We delete the file if it exist.
            File fileToCreate = new File(path + File.separator + filename);
            if (fileToCreate.exists()) {
                fileToCreate.delete();
            }
            byte[] fileContent = Base64.decodeBase64(contentBase64);
            Files.write(fileToCreate.toPath(), fileContent);
            actionResult.put("message", "File '" + fileToCreate.getAbsolutePath() + "' created with total size : " + fileToCreate.length() + " b");
            actionResult.put("status", "OK");
        }
        return actionResult;
    }

    ;
    

    private JSONObject download_files(String path, String opt, int nbfiles, String filename) throws JSONException, IOException {
        JSONObject actionResult = new JSONObject();

        File pathDir = new File(path);
        if (!pathDir.exists()) {
            actionResult.put("message", "Path '" + path + "' does not exist !! Please specify aa valid path.");
            actionResult.put("status", "Failed");
        } else {
            // Get all files.
            File[] rawfiles;
            if (!"".equals(filename)) {
                FileFilter fileFilter = new WildcardFileFilter(filename);
                rawfiles = pathDir.listFiles(fileFilter);
            } else {
                rawfiles = pathDir.listFiles();
            }
            // Only keep real files (removing directories)
            List<File> files = new ArrayList<>();
            for (File rawfile : rawfiles) {
                if (rawfile.isFile()) {
                    files.add(rawfile);
                }
            }

            if (files.isEmpty()) {
                actionResult.put("status", "Failed");
                if (!"".equals(filename)) {
                    actionResult.put("message", "Path '" + path + "' does not contain any file that match '" + filename + "' !!");
                } else {
                    actionResult.put("message", "Path '" + path + "' is empty or only contains folders !!");
                }
            } else {
                Collections.sort(files, new Comparator<File>() {
                    public int compare(File o1, File o2) {
                        return new Long(o2.lastModified()).compareTo(o1.lastModified());
                    }
                });
                JSONArray resFiles = new JSONArray();
                for (int i = 0; i < nbfiles; i++) {
                    if (i < files.size()) {
                        JSONObject resfile = new JSONObject();
                        resfile.put("filename", files.get(i).getName());
                        resfile.put("path", files.get(i).getPath());
                        resfile.put("size", files.get(i).length());
                        resfile.put("lastModified", new SimpleDateFormat(DATE_FORMAT).format(new Date(files.get(i).lastModified())));
                        if (files.get(i).length() < MAX_SIZE) {
                            byte[] fileContent = Files.readAllBytes(files.get(i).toPath());
                            resfile.put("contentBase64", Base64.encodeBase64String(fileContent));
                        }
                        resFiles.put(resfile);
                    }
                }
                actionResult.put("files", resFiles);
                actionResult.put("totalFilesAvailable", files.size());
                actionResult.put("totalFilesDownloaded", resFiles.length());
                actionResult.put("status", "OK");
            }
        }
        return actionResult;
    }

    ;
    
    
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
