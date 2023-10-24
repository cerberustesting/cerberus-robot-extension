/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.filemanagement;

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

        // Limit scope of action to this folder
        String authorisedFolderScope = System.getProperty("authorisedFolderScope");

        try {

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF8");

            JSONObject actionResult = new JSONObject();
            actionResult.put("status", "OK");

            StringBuilder sb = new StringBuilder();

            LOG.info("Received ExecuteFilemanagementAction: [Request from {}] | Scope limited to {}", request.getServerName(), authorisedFolderScope);

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

            LOG.info("Executing: [" + action + "]");
            String path = "";
            String filename = "";
            String opt = "";
            String contentBase64 = "";
            File pathDir;
            switch (action) {
                case "cleanFolder":
                    /**
                     * We get content of the file in Baase64 format, write it to
                     * a file, optionaly purge the corresponding folder before.
                     */
//                        if (obj.has("path")) {
//                            path = obj.getString("path");
//                        }
                    if (obj.has("filename")) {
                        filename = obj.getString("filename");
                    }
                    LOG.info("Cleaning files from '{}'.", filename);
                    actionResult = clean_folder(filename, authorisedFolderScope);

                    break;
                case "upload":
                    /**
                     * We get content of the file in Baase64 format, write it to
                     * a file, optionaly purge the corresponding folder before.
                     */
//                        if (obj.has("path")) {
//                            path = obj.getString("path");
//                        }
                    if (obj.has("filename")) {
                        filename = obj.getString("filename");
                    }
                    if (obj.has("contentBase64")) {
                        contentBase64 = obj.getString("contentBase64");
                    }
                    if (obj.has("option")) {
                        opt = obj.getString("option");
                    }
                    LOG.info("Saving local file to path '{}' with option '{}'.", filename, opt);
                    actionResult = upload_files(filename, contentBase64, opt, authorisedFolderScope);

                    break;
                case "download":
                    /**
                     * We get local content of the file in Baase64 format, It
                     * could be the last file generated.
                     */
                    if (obj.has("filename")) {
                        filename = obj.getString("filename");
                    }
                    int nbfiles = 1;
                    if (obj.has("nbFiles")) {
                        nbfiles = obj.getInt("nbFiles");
                    }
                    if (obj.has("option")) {
                        opt = obj.getString("option");
                    }
                    LOG.info("Getting {} local file(s) from '{}'.", nbfiles, filename);
                    actionResult = download_files(filename, nbfiles, opt, authorisedFolderScope);

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

            is.close();
            os.close();

//            } else {
//                LOG.info("ExecuteFilemanagementAction is up and running. Waiting for requests from Cerberus");
//                response.getWriter().print("ExecuteFilemanagementAction is up and running. Waiting for requests from Cerberus");
//            }
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
                    result.put("status", "Failed");
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

    private JSONObject upload_files(String filenamecomplete, String contentBase64, String opt, String authorisedFolderScope) throws JSONException, IOException {
        JSONObject actionResult = new JSONObject();

        File file = new File(filenamecomplete);
        File pathDir;
        String filename;
        if (file.isFile()) {
            pathDir = file.getParentFile();
            filename = file.getName();
        } else if (file.isDirectory()) {
            pathDir = file;
            filename = "";
        } else {
            pathDir = file.getParentFile();
            filename = file.getName();
        }

        if (!check_authorisation(pathDir, authorisedFolderScope)) {
            actionResult.put("message", "Path '" + pathDir.getAbsolutePath() + "' is not authorised !! The path is not inside '" + authorisedFolderScope + "'.");
            actionResult.put("status", "Failed");
            actionResult.put("code", 403);
        } else {
            // We create the folder that will host the file if it does not exist.
            if (!pathDir.exists()) {
                LOG.info("Path '{}' does not exist. We create it.", pathDir.toString());
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
            if (file.exists()) {
                file.delete();
            }
            byte[] fileContent = new byte[0];
            if (!contentBase64.isEmpty()) {
                fileContent = Base64.decodeBase64(contentBase64);
            }
            Files.write(file.toPath(), fileContent);
            actionResult.put("message", "File '" + file.getAbsolutePath() + "' created with total size : " + file.length() + " b");
            actionResult.put("status", "OK");
            actionResult.put("code", 200);
        }
        return actionResult;
    }

    private JSONObject clean_folder(String filenamecomplete, String authorisedFolderScope) throws JSONException, IOException {
        JSONObject actionResult = new JSONObject();

        File file = new File(filenamecomplete);
        File pathDir;
        String filename;
        if (file.isFile()) {
            pathDir = file.getParentFile();
            filename = file.getName();
        } else if (file.isDirectory()) {
            pathDir = file;
            filename = "";
        } else {
            pathDir = file;
            filename = "";
        }

        if (!pathDir.exists()) {
            actionResult.put("message", "Path '" + pathDir.getAbsolutePath() + "' does not exist !! We consider it as empty/cleaned already...");
            actionResult.put("status", "OK");
            actionResult.put("code", 200);
        } else if (!check_authorisation(pathDir, authorisedFolderScope)) {
            actionResult.put("message", "Path '" + pathDir.getAbsolutePath() + "' is not authorised !! The path '" + pathDir.toPath().toRealPath().toString() + File.separator + "' is not inside '" + authorisedFolderScope + "'.");
            actionResult.put("status", "Failed");
            actionResult.put("code", 403);
        } else {
            // Get all files.
            File[] rawfiles;
            if (!"".equals(filename)) {
                FileFilter fileFilter = new WildcardFileFilter(filename);
                rawfiles = pathDir.listFiles(fileFilter);
            } else {
                rawfiles = pathDir.listFiles();
            }
            int i = 0;
            for (File rawfile : rawfiles) {
                rawfile.delete();
                i++;
            }
            actionResult.put("totalFilesDeleted", i);
            actionResult.put("message", i + " file(s) from '" + filenamecomplete + "' deleted sucessfuly.");
            actionResult.put("status", "OK");
            actionResult.put("code", 200);
        }
        return actionResult;
    }

    private JSONObject download_files(String filenamecomplete, int nbfiles, String opt, String authorisedFolderScope) throws JSONException, IOException {
        JSONObject actionResult = new JSONObject();

        File file = new File(filenamecomplete);
        File pathDir;
        String filename;
        if (file.isFile()) {
            pathDir = file.getParentFile();
            filename = file.getName();
        } else if (file.isDirectory()) {
            pathDir = file;
            filename = "";
        } else {
            pathDir = file.getParentFile();
            filename = file.getName();
        }

        if (!pathDir.exists()) {
            actionResult.put("message", "Path '" + pathDir.getAbsolutePath() + "' does not exist !! Please specify a valid path.");
            actionResult.put("status", "Failed");
            actionResult.put("code", 400);
        } else if (!check_authorisation(pathDir, authorisedFolderScope)) {
            actionResult.put("message", "Path '" + pathDir.getAbsolutePath() + "' is not authorised !! The path '" + pathDir.toPath().toRealPath().toString() + File.separator + "' is not inside '" + authorisedFolderScope + "'.");
            actionResult.put("status", "Failed");
            actionResult.put("code", 403);
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
                actionResult.put("code", 400);
                if (!"".equals(filename)) {
                    actionResult.put("message", "Path '" + pathDir.getAbsolutePath() + "' does not contain any file that match '" + filename + "' !!");
                } else {
                    actionResult.put("message", "Path '" + pathDir.getAbsolutePath() + "' is empty or only contains folders !!");
                }
            } else {
                if ("LASMODIFIED".equals(opt)) {
                    Collections.sort(files, new Comparator<File>() {
                        public int compare(File o1, File o2) {
                            return new Long(o2.lastModified()).compareTo(o1.lastModified());
                        }
                    });
                } else if ("IGNORECASEDESC".equals(opt)) {
                    Collections.sort(files, new Comparator<File>() {
                        public int compare(File o1, File o2) {
                            return o2.getName().compareToIgnoreCase(o1.getName());
                        }
                    });
                } else if ("IGNORECASEASC".equals(opt)) {
                    Collections.sort(files, new Comparator<File>() {
                        public int compare(File o1, File o2) {
                            return o1.getName().compareToIgnoreCase(o2.getName());
                        }
                    });
                } else if ("DESC".equals(opt)) {
                    Collections.sort(files, new Comparator<File>() {
                        public int compare(File o1, File o2) {
                            return o2.getName().compareTo(o1.getName());
                        }
                    });
                } else {
                    // "ASC" and default value.
                    Collections.sort(files, new Comparator<File>() {
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                }
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
                actionResult.put("message", resFiles.length() + " file(s) retrieved successfully out of " + files.size() + " that was(were) available to download from '" + pathDir.getAbsolutePath() + "'.");
                actionResult.put("code", 200);
            }
        }
        return actionResult;
    }

    boolean check_authorisation(File pathDir, String authorisedFolderScope) {
        String pathToCheck;
        try {
            pathToCheck = pathDir.toPath().toRealPath().toString() + File.separator;
            return pathToCheck.startsWith(authorisedFolderScope);
        } catch (IOException ex) {
            // PathDir could not exist. We should still guess if it is authorised.
            if (pathDir.toString().contains("..")) {
                LOG.warn("Path that do not exist should not include .. !! '{}'", pathDir.toString());
                return false;
            } else {
                pathToCheck = pathDir.toPath().toString() + File.separator;
                return pathToCheck.startsWith(authorisedFolderScope);
            }
//            LOG.error(ex, ex);
//            return false;
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
