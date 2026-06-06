/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.service.system;

import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

/**
 *
 * @author bcivel
 */
public class SystemAction {

    private static final Logger LOG = LogManager.getLogger(SystemAction.class);

    private static final String STATUS_OK = "OK";
    private static final String STATUS_KO = "KO";
    private static final String STATUS_FA = "Failed";

    public JSONObject checkCertificate(String urlString) {
        JSONObject result = new JSONObject();
        try {
            URL url = new URL(urlString);
            result.put("url", urlString);
            // 1. Ouvrir la connexion HTTPS
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // On déclenche la poignée de main (handshake) SSL sans télécharger toute la page
            conn.connect();

            // 2. Récupérer les certificats du serveur
            Certificate[] certs = conn.getServerCertificates();

            if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                // Le premier certificat [0] de la chaîne est toujours celui du site (End-Entity)
                X509Certificate cert = (X509Certificate) certs[0];
                result.put("exist", true);
                result.put("message", "Certificate found");

                // Expiration Date
                Date expiryDate = cert.getNotAfter();
                result.put("endDate", expiryDate);

                // Certificate Owner
                result.put("owner", cert.getSubjectX500Principal());

                // Expiration Days
                LocalDate dateExpiration = expiryDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                LocalDate now = LocalDate.now();
                long expireDays = ChronoUnit.DAYS.between(now, dateExpiration);
                result.put("validityDurationDays", expireDays);

            } else {
                result.put("exist", false);
                result.put("message", "No Certificate found !!");
            }
            conn.disconnect();
            result.put("status", STATUS_OK);

        } catch (Exception e) {
            result.put("exist", false);
            result.put("status", STATUS_FA);
            result.put("message", "Connection error to " + urlString + " !!!");
        }
        LOG.debug(result.toString(1));
        return result;
    }

    public JSONObject getCPU(String param1, boolean filterStrict) {
        JSONObject result = new JSONObject();
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();

        String targetBinary = param1;

        // Récupérer tous les processus et filtrer par nom
        result.put("processNameFilter", param1);
        result.put("processNameFilterStrict", filterStrict);

        if (param1 == null || param1.isEmpty()) {
            result.put("status", STATUS_OK);
            result.put("message", "No process name requested");
            return result;
        }

        List<Integer> pids = new ArrayList<>();
        if (filterStrict) {
            pids = os.getProcesses().stream()
                    // .getName() contient le nom du binaire (ex: "vlc", "notepad.exe")
                    .filter(p -> p.getName().equalsIgnoreCase(targetBinary))
                    .map(OSProcess::getProcessID)
                    .collect(Collectors.toList());
        } else {
            pids = os.getProcesses().stream()
                    // .getName() contient le nom du binaire (ex: "vlc", "notepad.exe")
                    .filter(p -> p.getName().toLowerCase().contains(targetBinary.toLowerCase()))
                    .map(OSProcess::getProcessID)
                    .collect(Collectors.toList());
        }

        if (pids.isEmpty()) {

            result.put("status", STATUS_FA);
            result.put("message", "No process found for " + param1);

        } else {

            result.put("nbProcessFound", pids.size());

            long totKernelTime = 0;
            long totUserTime = 0;
            long totTotalCpuTimeMs = 0;
            long maxProcessUpTimeMs = 0;

            double totResidentMemoryB = 0;
            double totVirtualMemoryB = 0;

            JSONArray allProcesses = new JSONArray();

            for (Integer pid : pids) {
                OSProcess process = os.getProcess(pid);

                if (process != null) {

                    JSONObject singleProcess = new JSONObject();

                    singleProcess.put("name", process.getName());
                    singleProcess.put("cmd", process.getCommandLine());

                    // Cumulative Kernel CPU time (in ms)
                    long kernelTime = process.getKernelTime();
                    totKernelTime += kernelTime;
                    singleProcess.put("kernelTimeMs", kernelTime);

                    // Cumulative User CPU time (in ms)
                    long userTime = process.getUserTime();
                    totUserTime += userTime;
                    singleProcess.put("userTimeMs", userTime);

                    // Cumulative totoal CPU time (in ms)
                    long totalCpuTimeMs = kernelTime + userTime;
                    totTotalCpuTimeMs += totalCpuTimeMs;
                    singleProcess.put("totalTimeMs", totalCpuTimeMs);

                    // Global CPU % time since start of the process (in percentage)
                    // it calculates : (CPU Time / Real time spent since up-time)
                    double globalCpuAverage = process.getProcessCpuLoadCumulative() * 100;
                    singleProcess.put("globalAveragePer", globalCpuAverage);

                    // Process Up-time
                    long processUpTimeMs = process.getUpTime();
                    maxProcessUpTimeMs = Math.max(maxProcessUpTimeMs, processUpTimeMs);
                    singleProcess.put("upTimeMs", processUpTimeMs);

                    double residentMemoryB = process.getResidentSetSize();
                    double residentMemoryMb = residentMemoryB / (1024.0 * 1024.0);
                    totResidentMemoryB += residentMemoryB;
                    singleProcess.put("residentMemoryByte", residentMemoryB);
                    singleProcess.put("residentMemoryMb", residentMemoryMb);

                    double virtualMemoryB = process.getVirtualSize();
                    double virtualMemoryMb = virtualMemoryB / (1024.0 * 1024.0);
                    totVirtualMemoryB += virtualMemoryB;
                    singleProcess.put("virtualMemoryByte", virtualMemoryB);
                    singleProcess.put("virtualMemoryMb", virtualMemoryMb);

                    allProcesses.put(singleProcess);

                } else {
                    result.put("status", STATUS_FA);
                    result.put("message", "Process " + pid + " could not be found ");
                }

            }

            result.put("kernelTimeMs", totKernelTime);
            result.put("userTimeMs", totUserTime);
            result.put("totalTimeMs", totTotalCpuTimeMs);
            result.put("upTimeMs", maxProcessUpTimeMs);

            result.put("residentMemoryByte", totResidentMemoryB);
            result.put("virtualMemoryByte", totVirtualMemoryB);
            result.put("residentMemoryMb", totResidentMemoryB / (1024.0 * 1024.0));
            result.put("virtualMemoryMb", totVirtualMemoryB / (1024.0 * 1024.0));

            result.put("processes", allProcesses);

            result.put("status", STATUS_OK);
        }

        return result;
    }

}
