/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.service.sikuli;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.DoublePointer;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_core.*;
import static org.cerberus.robot.extension.service.sikuli.KeyCodeEnum.values;
import org.cerberus.robot.extension.service.system.ExecuteSystemAction;
import org.cerberus.robot.extension.service.system.SystemAction;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public class SikuliAction {

    private static final Logger LOG = LogManager.getLogger(SikuliAction.class);

    private static final String STATUS_OK = "OK";
    private static final String STATUS_KO = "KO";
    private static final String STATUS_FA = "Failed";

    private static final String MESSAGE_DND_SOURCENOTFOUND = "Could not find the location of the element to drag!";
    private static final String MESSAGE_DND_DESTINATIONNOTFOUND = "Could not find the location of the destination where to drop!";
    private static final String MESSAGE_DND_1ELEMENTFOUND = "Unique elements were found on both source and destination.";
    private static final String MESSAGE_DND_MANYELEMENTFOUND = "Warning!! Multiple elements were found on either source or destination! %NB1% on source(s) and %NB2% on destination(s)!";
    private static final String MESSAGE_ELEMENTNOTFOUND = "Could not find any element on the screen that match with a higher similarity than %MINSIM%!";
    private static final String MESSAGE_NOTIMPLEMEMTED = "Feature not implemented yet!";
    private static final String MESSAGE_MISSINGPARAMETER = "Could not perform the action as some parameters are missing!";
    private static final String MESSAGE_1ELEMENTFOUND = "A unique element was found.";
    private static final String MESSAGE_MANYELEMENTFOUND = "Warning!! %NB% elements were found. Best match was used!";

    private static final String CMD_STARTVIDEO = "ffmpeg -y -video_size 1920x1080 -framerate 15 -f x11grab -draw_mouse 1 -i :99 -codec:v libx264 -preset ultrafast -pix_fmt yuv420p %FILE%";
    private static final String CMD_STOPVIDEO = "ffmpeg";
    private static final String CMD_VIDEOREGEX = "*.mp4";

    /**
     * Invariant SIKULI ACTION String.
     */
    public static final String SIKULI_PING = "ping";
    public static final String SIKULI_TYPE = "type";
    public static final String SIKULI_CLICK = "click";
    public static final String SIKULI_DRAGANDDROP = "dragAndDrop";
    public static final String SIKULI_DOUBLECLICK = "doubleClick";
    public static final String SIKULI_RIGHTCLICK = "rightClick";
    public static final String SIKULI_MOSEDOWN = "mouseDown"; // Press and keep left mouse button down
    public static final String SIKULI_MOUSEUP = "mouseUp"; // Release left mouse button
    public static final String SIKULI_MOUSEMOVE = "mouseMove";
    public static final String SIKULI_SWITCHTOWINDOW = "switchToWindow";
    public static final String SIKULI_OPENAPP = "openApp";
    public static final String SIKULI_CLOSEAPP = "closeApp";
    public static final String SIKULI_SWITCHAPP = "switchApp";
    public static final String SIKULI_PASTE = "paste";
    public static final String SIKULI_CLEARFIELD = "clearField";
    public static final String SIKULI_WAIT = "wait";
    public static final String SIKULI_WAITVANISH = "waitVanish";
    public static final String SIKULI_MOUSEOVER = "mouseOver";
    public static final String SIKULI_EXISTS = "exists";
    public static final String SIKULI_NOTEXISTS = "notExists";
    public static final String SIKULI_FINDTEXT = "findText";
    public static final String SIKULI_CAPTURE = "capture";
    public static final String SIKULI_STARTVIDEO = "startVideo";
    public static final String SIKULI_ENDEXECUTION = "endExecution";
    public static final String SIKULI_ENDEXECUTIONWITHVIDEO = "endExecutionWithVideo";

    static class PointMatch {

        Point point;
        double score;
//        Mat template;
        Point templateTLCorner;
        int templateCols;
        int templateRow;

        PointMatch(Point point, double score, Mat template, Point templateTLCorner) {
            this.point = point;
            this.score = score;
//            this.template = template;
            this.templateTLCorner = templateTLCorner;
        }

        PointMatch(Point point, double score, Point templateTLCorner) {
            this.point = point;
            this.score = score;
            this.templateTLCorner = templateTLCorner;
        }

        PointMatch(Point point, double score, Point templateTLCorner, int templateCols, int templateRows) {
            this.point = point;
            this.score = score;
            this.templateTLCorner = templateTLCorner;
            this.templateCols = templateCols;
            this.templateRow = templateRows;
        }
    }

    JSONObject doAction(String action, String picture, String picture2, String text, String text2,
            Double minSimilarity, Double typeDelay, int highlightElementDurationSecond_Requested, String rootPictureFolder, String rootVideoFolder,
            int xOffset, int yOffset, int xOffset2, int yOffset2,
            long endTime, long executionId, int screenshot, String processName, boolean filterStrict) {

        JSONObject result = new JSONObject();
        int highlightElementDurationSecond = highlightElementDurationSecond_Requested;
        Integer highlightElementDurationSecond_System = null;
        if (System.getProperty("highlightElement") != null) {
            try {
                highlightElementDurationSecond_System = Integer.valueOf(System.getProperty("highlightElement"));
                highlightElementDurationSecond = Math.max(highlightElementDurationSecond_Requested, highlightElementDurationSecond_System);
            } catch (Exception ex) {
                LOG.warn("Exception parsing highlightElement argument : " + ex, ex);
            }
        }
        LOG.debug("Highlight : Requested {} | System {} | Final {}", highlightElementDurationSecond_Requested, highlightElementDurationSecond_System, highlightElementDurationSecond);

        try {
            /**
             * Result Object init with result KO
             */
            String status = STATUS_FA;
            String message = null;
            String stacktrace = null;
            BufferedImage screenDebug = null;
            String videoFileName = rootVideoFolder + File.separator + executionId + ".mp4";

            /**
             * Switch on Action. Do the action/control.
             */
            try {
                Robot robot = new Robot();

                switch (action) {

                    /**
                     * ACTIONS - Either OK or FA but never KO
                     */
                    case SIKULI_PING:
                        LOG.info("[{}] Ping.", action);
                        status = STATUS_OK;
                        break;

                    case SIKULI_STARTVIDEO:
                        String cmdVideo = CMD_STARTVIDEO.replace("%FILE%", videoFileName);
                        LOG.info("[{}] Opening ffmpeg and record video : {}", action, cmdVideo);
                        // Start new recording
                        ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmdVideo);
                        pb.redirectErrorStream(true);
                        Process process = pb.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            LOG.debug("[FFMpeg] " + line);
                        }
                        int exitCode = process.waitFor();
                        LOG.info("FFmpeg exited with code " + exitCode);

                        status = STATUS_OK;
                        break;

                    case SIKULI_OPENAPP:
                        LOG.info("[{}] Opening Application : {}", action, text);
                        new ProcessBuilder("bash", "-c", text).start();
                        status = STATUS_OK;
                        break;
                    case SIKULI_CLOSEAPP:
                        LOG.info("[{}] Closing Application : {}", action, text);
                        new ProcessBuilder("pkill", "-f", text).start();
                        status = STATUS_OK;
                        break;

                    // All actions that require a single element.
                    case SIKULI_CLICK:
                    case SIKULI_DOUBLECLICK:
                    case SIKULI_RIGHTCLICK:
                    case SIKULI_MOUSEOVER:
                        // Simple click
                        if ("".equals(picture) && "".equals(text)) {
                            LOG.debug("[{}] Simple Click|doubleClick|rightClick Action.", action);

                            // Effectuer l'action
                            performJustTheClick(action, robot);
                            Thread.sleep(500);
                            status = STATUS_OK;
                        }
                        // click on a picture
                        if (!"".equals(picture)) {
                            LOG.debug("[{}] Click|doubleClick|rightClick|mouseOver an IMAGE Action.", action);
                            List<PointMatch> p = performWait(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);
                            if (!p.isEmpty()) {
                                screenDebug = highLightPointMatches(robot, p, highlightElementDurationSecond, action);
                                performMouseMove(action, robot, "absolute " + p.get(0).point.x + "," + p.get(0).point.y);
                                performJustTheClick(action, robot);
                                Thread.sleep(500);
                                status = STATUS_OK;
                                message = getMessageFromListPoints(p);
                            } else {
                                status = STATUS_FA;
                                message = MESSAGE_ELEMENTNOTFOUND.replace("%MINSIM%", String.valueOf(minSimilarity));
                            }
                        } else if (!"".equals(text)) {
                            status = STATUS_FA;
                            message = MESSAGE_NOTIMPLEMEMTED;
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_DRAGANDDROP:
                        if (!"".equals(picture) && !"".equals(picture2)) {
                            LOG.debug("[{}] dragAndDrop an IMAGE Action.", action);
                            List<PointMatch> p = performWait(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);
                            List<PointMatch> p2 = performWait(action, robot, endTime, picture2, minSimilarity, xOffset, yOffset);

                            if ((!p.isEmpty()) || (!p2.isEmpty())) {
                                screenDebug = highLightPointMatches(robot, p, p2, highlightElementDurationSecond, action + " from", action + " to");
                            }

                            if (!p.isEmpty()) {
                                if (!p2.isEmpty()) {
                                    performMouseMove(action, robot, "absolute " + p.get(0).point.x + "," + p.get(0).point.y);
                                    LOG.info("[{}] Press left mouse button.", action);
                                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                    Thread.sleep(500);
                                    performMouseMove(action, robot, "absolute " + p2.get(0).point.x + "," + p2.get(0).point.y);
                                    LOG.info("[{}] Release left mouse button.", action);
                                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                } else {
                                    status = STATUS_FA;
                                    message = MESSAGE_DND_DESTINATIONNOTFOUND;
                                }
                                Thread.sleep(500);
                                status = STATUS_OK;
                                message = getMessageFromListPoints(p, p2);
                            } else {
                                status = STATUS_FA;
                                message = MESSAGE_DND_SOURCENOTFOUND;
                            }
                        } else if (!"".equals(text)) {
                            status = STATUS_FA;
                            message = MESSAGE_NOTIMPLEMEMTED;
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_MOSEDOWN:
                        LOG.info("[{}] Press left mouse button.", action);
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        status = STATUS_OK;
                        break;
                    case SIKULI_MOUSEUP:
                        LOG.info("[{}] Release left mouse button.", action);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        status = STATUS_OK;
                        break;

                    case SIKULI_MOUSEMOVE:
                        status = performMouseMove(action, robot, text);
                        break;

                    case SIKULI_WAIT:
                        if (!"".equals(picture)) {
                            List<PointMatch> p = performWait(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);
                            if (!p.isEmpty()) {
                                screenDebug = highLightPointMatches(robot, p, highlightElementDurationSecond, action);
                                status = STATUS_OK;
                                message = getMessageFromListPoints(p);
                            } else {
                                status = STATUS_FA;
                                message = MESSAGE_ELEMENTNOTFOUND.replace("%MINSIM%", String.valueOf(minSimilarity));
                            }
                        } else if (!"".equals(text)) {
                            status = STATUS_FA;
                            message = MESSAGE_NOTIMPLEMEMTED;
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_WAITVANISH:
                        if (!"".equals(picture)) {
                            List<PointMatch> p = performWaitVanish(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);
                            if (!p.isEmpty()) {
                                screenDebug = highLightPointMatches(robot, p, highlightElementDurationSecond, action);
                                status = STATUS_FA;
                                message = getMessageFromListPoints(p);
                            } else {
                                status = STATUS_OK;
                                message = MESSAGE_ELEMENTNOTFOUND.replace("%MINSIM%", String.valueOf(minSimilarity));
                            }
                        } else if (!"".equals(text)) {
                            status = STATUS_OK;
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_PASTE: // Equivalent of type (ie move to a field and enter data by pressing keys)

                        if (!"".equals(picture) && (!"".equals(text))) {
                            LOG.debug("[{}] paste text on an IMAGE Action.", action);
                            List<PointMatch> p = performWait(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);

                            if (!p.isEmpty()) {
                                screenDebug = highLightPointMatches(robot, p, highlightElementDurationSecond, action);
                                performMouseMove(action, robot, "absolute " + p.get(0).point.x + "," + p.get(0).point.y);
                                performLeftClick(robot);
                                Thread.sleep(500);
                                typeKeyboard(action, robot, text, "", typeDelay);
                                status = STATUS_OK;
                                message = getMessageFromListPoints(p);
                            } else {
                                status = STATUS_FA;
                                message = MESSAGE_ELEMENTNOTFOUND.replace("%MINSIM%", String.valueOf(minSimilarity));

                            }
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_CLEARFIELD: // Equivalent of type (ie move to a field and enter data by pressing keys)

                        if (!"".equals(picture)) {
                            LOG.debug("[{}] clearfield on an IMAGE Action.", action);
                            List<PointMatch> p = performWait(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);

                            if (!p.isEmpty()) {
                                screenDebug = highLightPointMatches(robot, p, highlightElementDurationSecond, action);
                                performMouseMove(action, robot, "absolute " + p.get(0).point.x + "," + p.get(0).point.y);
                                performLeftClick(robot);
                                Thread.sleep(500);
                                // Select All using CTRL + A
                                robot.keyPress(KeyEvent.VK_CONTROL);
                                robot.keyPress(KeyEvent.VK_A);
                                robot.keyRelease(KeyEvent.VK_A);
                                robot.keyRelease(KeyEvent.VK_CONTROL);
                                robot.delay((int) (typeDelay * 1000));
                                // Delete the selected text
                                robot.keyPress(KeyEvent.VK_DELETE);
                                robot.keyRelease(KeyEvent.VK_DELETE);
                                robot.delay((int) (typeDelay * 1000));
                                status = STATUS_OK;
                                message = getMessageFromListPoints(p);
                            } else {
                                status = STATUS_FA;
                                message = MESSAGE_ELEMENTNOTFOUND.replace("%MINSIM%", String.valueOf(minSimilarity));

                            }
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_TYPE: // Just press some keyboard
                        int res = 0;
                        typeKeyboard(action, robot, text, text2, typeDelay);
                        status = STATUS_OK;
                        break;

                    /**
                     * CONTROLS
                     */
                    case SIKULI_EXISTS:
                        if (!"".equals(picture)) {
                            List<PointMatch> p = performWait(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);

                            if (!p.isEmpty()) {
                                screenDebug = highLightPointMatches(robot, p, highlightElementDurationSecond, action);
                                status = STATUS_OK;
                                message = getMessageFromListPoints(p);
                            } else {
                                status = STATUS_KO;
                                message = MESSAGE_ELEMENTNOTFOUND.replace("%MINSIM%", String.valueOf(minSimilarity));
                            }
                        } else if (!"".equals(text)) {
                            status = STATUS_FA;
                            message = MESSAGE_NOTIMPLEMEMTED;
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_NOTEXISTS:
                        // PICTURE
                        if (!"".equals(picture)) {
                            List<PointMatch> p = performWaitVanish(action, robot, endTime, picture, minSimilarity, xOffset, yOffset);
                            if (!p.isEmpty()) {
                                screenDebug = highLightPointMatches(robot, p, highlightElementDurationSecond, action);
                                status = STATUS_KO;
                                message = getMessageFromListPoints(p);
                            } else {
                                status = STATUS_OK;
                                message = MESSAGE_ELEMENTNOTFOUND.replace("%MINSIM%", String.valueOf(minSimilarity));
                            }
                        } else if (!"".equals(text)) {
                            status = STATUS_FA;
                            message = MESSAGE_NOTIMPLEMEMTED;
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_FINDTEXT:
                        if (!"".equals(text)) {
                            status = STATUS_FA;
                            message = MESSAGE_NOTIMPLEMEMTED;
                        } else {
                            status = STATUS_FA;
                            message = MESSAGE_MISSINGPARAMETER;
                        }
                        break;

                    case SIKULI_CAPTURE:
                        String screenshotInBase64 = getScreenshotInBase64(rootPictureFolder);
                        status = STATUS_OK;
                        result.put("screenshot", screenshotInBase64);
                        break;

                    case SIKULI_ENDEXECUTION:
                    case SIKULI_ENDEXECUTIONWITHVIDEO:
                        SystemAction systemAction = new SystemAction();
                        status = performEndExecution(action, robot);
                        String videoDebug = null;
                        if (SIKULI_ENDEXECUTIONWITHVIDEO.equals(action)) {
                            videoDebug = performEndVideo(videoFileName, true);
                        } else {
                            performEndVideo(videoFileName, false);
                        }
                        if (videoDebug != null) {
                            result.put("videoDebug", videoDebug);
                        }
                        LOG.info("[{}] Checking CPU from process name {} with strict filter {}.", action, processName, filterStrict);
                        result.put("system", systemAction.getCPU(processName, filterStrict));

                        break;
                }

            } catch (Exception ex) {
                message = ex.toString();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                stacktrace = sw.toString();

                LOG.error(ex, ex);

            } finally {
                //Update the status
                result.put("status", status);
                result.put("message", message);
                result.put("stacktrace", stacktrace);
                if (screenDebug != null && (screenshot == 1 || screenshot == 2)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(screenDebug, "png", baos);
                    picture = Base64.getEncoder().encodeToString(baos.toByteArray());
                    result.put("screenshotDebug", picture);
                }

            }
        } catch (JSONException ex) {
            LOG.error(ex, ex);
        } catch (Exception ex) {
            try {
                LOG.error(ex, ex);
                //Update the status
                String message = ex.toString();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                String stacktrace = sw.toString();

                result.put("status", STATUS_FA);
                result.put("message", message);
                result.put("stacktrace", stacktrace);
            } catch (JSONException ex1) {
                LOG.error(ex1, ex1);
            }
        }
        return result;
    }

    private List<PointMatch> performWait(String action, Robot robot, long endTime, String picture, double minSimilarity, int xOffset, int yOffset) throws Exception {

        Mat template = imread(picture);
        int i = 1;
        List<PointMatch> p = new ArrayList<>();
        while (System.currentTimeMillis() < endTime && i++ < 500) {
            p.clear();
            p = findAllPatternsOnScreen(action, robot, template, minSimilarity, xOffset, yOffset);
            if (!p.isEmpty()) {
                return p;
            }
            LOG.debug("[{}] Not found - Retrying again during " + (endTime - System.currentTimeMillis()) + " ms", action);
        }
        template.release();
        p.clear();
        return new ArrayList<>();
    }

    private List<PointMatch> performWaitVanish(String action, Robot robot, long endTime, String picture, double minSimilarity, int xOffset, int yOffset) throws Exception {

        Mat template = imread(picture);
        int i = 1;
        List<PointMatch> p = new ArrayList<>();
        while (System.currentTimeMillis() < endTime && i++ < 500) {
            p.clear();
            p = findAllPatternsOnScreen(action, robot, template, minSimilarity, xOffset, yOffset);
            if (p.isEmpty()) {
                return p;
            }
            LOG.debug("[{}] Still here - Retrying again during " + (endTime - System.currentTimeMillis()) + " ms", action);
        }
        return p;
    }

    private String performMouseMove(String action, Robot robot, String text) throws InterruptedException, AWTException {

        String[] instructions = text.split(";");
        for (String instruction : instructions) {
            String curInstruction = instruction.trim();
            if (!curInstruction.isEmpty()) {

                // Display position before move.
                Point loc = MouseInfo.getPointerInfo().getLocation();
                int x = loc.x;
                int y = loc.y;
                LOG.info("[{}] Current Position : {} | {}", action, x, y);
                // Determine if coords are in 'absolute' mode
                boolean isAbsolute = false;
                if (curInstruction.contains("absolute")) {
                    isAbsolute = true;
                    curInstruction = curInstruction.replace("absolute", "").trim();
                }

                // Determine the target coords to move (managing 'center' keyword)
                int x1 = 0;
                int y1 = 0;
                if (curInstruction.contains("center")) {
                    isAbsolute = true;
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    int width = screenSize.width;
                    int height = screenSize.height;
                    x1 = width / 2;
                    y1 = height / 2;
                } else {
                    String[] pos = curInstruction.trim().split(",");
                    if (isAbsolute) {
                        x1 = Integer.valueOf(pos[0].trim());
                        if (pos.length > 1) {
                            y1 = Integer.valueOf(pos[1].trim());
                        }
                    } else {
                        x1 = x + Integer.valueOf(pos[0].trim());
                        if (pos.length > 1) {
                            y1 = y + Integer.valueOf(pos[1].trim());
                        }
                    }
                }

                LOG.info("[{}] Move cursor to (Absolute) : {} | {}", action, x1, y1);
                robot.mouseMove(x1, y1);

                LOG.info("[{}] Sleeping 1s", action);
                Thread.sleep(1000);

                // Display position after move.
                loc = MouseInfo.getPointerInfo().getLocation();
                x = loc.x;
                y = loc.y;
                LOG.info("[{}] New Position :  {} | {}", action, x, y);
            }
        }
        return STATUS_OK;
    }

    private void performJustTheClick(String action, Robot robot) {
        switch (action) {
            case "doubleClick":
                LOG.info("[{}] perform double click", action);
                performLeftClick(robot);
                robot.delay(50); // delay between clicks
                performLeftClick(robot);
                break;
            case "rightClick":
                LOG.info("[{}] perform right click", action);
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                break;
            case "click":
                LOG.info("[{}] perform click", action);
                performLeftClick(robot);
                break;
            default:
                // rien
                break;
        }
    }

    private void performLeftClick(Robot robot) {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private String performEndVideo(String videoFileName, boolean withFile) throws IOException, InterruptedException {
        /**
         * The aim of that action is to put back the status of the robot to a
         * stable situation. For example, we stop pressing the mouse button in
         * case it was pressed.
         */
        // Stop Video recording
        new ProcessBuilder("pkill", "-f", CMD_STOPVIDEO).start();
        // Wait that process close correctly.
        Thread.sleep(2000);

        // get and encode video file to Base64
        File file = new File(videoFileName);
        // File exist
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        // Not too big and not too small
        long size = file.length();
        if (size < 10) {
            LOG.warn("Video file '{}' is too small !!", videoFileName);
            return null;
        }
        if (size > 20 * 1024 * 1024) { // 20 MB
            LOG.warn("Video file '{}' is too big !!", videoFileName);
            return null;
        }
        LOG.info("Prepare the video file back to Cerberus");
        // Base64 Encoding
        byte[] fileContent = Files.readAllBytes(Paths.get(videoFileName));
        String videoBase64 = Base64.getEncoder().encodeToString(fileContent);
        // Clean file mp4
        file.delete();
        if (withFile) {
            return videoBase64;
        }
        return null;
    }

    private String performEndExecution(String action, Robot robot) throws InterruptedException, AWTException, IOException {
        /**
         * The aim of that action is to put back the status of the robot to a
         * stable situation. For example, we stop pressing the mouse button in
         * case it was pressed.
         */
        LOG.info("Clean the robot for next execution");
        // Release all buttons pressed
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        // Move back the cursor to the middle of the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width / 2;
        int y = screenSize.height / 2;
        robot.mouseMove(x, y);

        LOG.info("----------------------------------------------------------------------");
        return STATUS_OK;
    }

    private List<PointMatch> findAllPatternsOnScreen(String action, Robot robot,
            Mat template,
            double minSimilarity,
            int xOffset,
            int yOffset) throws Exception {

        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Mat screenshot = new Mat();
        screenshot = bufferedImageToMat(robot.createScreenCapture(screenRect));

        Mat result = new Mat();
        matchTemplate(screenshot, template, result, TM_CCOEFF_NORMED);

        List<PointMatch> finalMatches = new ArrayList<>();
        List<PointMatch> matches = new ArrayList<>();

        // On copie result pour modification
        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
        org.bytedeco.opencv.opencv_core.Point maxLoc = new org.bytedeco.opencv.opencv_core.Point();

        Rect rect = new Rect();
        // On va rechercher toutes les occurrences
        int i = 1;
        while (true) {

//            LOG.debug(i++);
            minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

            if (maxVal.get() >= minSimilarity) {
                // Point that match the template with the offset
                Point match = new Point(
                        (int) maxLoc.x() + (template.cols() / 2) + xOffset,
                        (int) maxLoc.y() + (template.rows() / 2) + yOffset
                );
                // Point that match the template without the offset (used to identify the position of the template)
                Point templateULCorner = new Point(
                        (int) maxLoc.x(),
                        (int) maxLoc.y()
                );
                PointMatch pointMatch = new PointMatch(match, maxVal.get(), templateULCorner, template.cols(), template.rows());
                matches.add(pointMatch);
                LOG.debug("[{}] Found occurrence at {} with similarity {} - cols {} rows {}", action, match, maxVal.get(), template.cols(), template.rows());

                // Supprimer la zone trouvée pour ne pas la détecter à nouveau
                int w = template.cols();
                int h = template.rows();
                rect.x((int) maxLoc.x());
                rect.y((int) maxLoc.y());
                rect.width(w);
                rect.height(h);
//                Rect rect = new Rect((int) maxLoc.x(), (int) maxLoc.y(), w, h);
                rectangle(result, rect, new Scalar(-1, -1, -1, 0), FILLED, LINE_8, 0);

            } else {
                break; // plus d’occurrences
            }
        }

        // Clean recurent rectangle that overlap.
        finalMatches = cleanPointsThatOverlap(matches, template.rows(), template.cols());
        if (matches.isEmpty()) {
            LOG.debug("[{}] No occurrences found with similarity >= {}", action, minSimilarity);
        } else {
            LOG.debug("[{}] {} occurrence(s) found with similarity >= {}. {} kept after cleaning", action, matches.size(), minSimilarity, finalMatches.size());
        }
        return finalMatches;
    }

    private List<PointMatch> cleanPointsThatOverlap(List<PointMatch> initPoints, int height, int width) {
        List<PointMatch> filtered = new ArrayList<>();

        double minDistance = Math.min(height, width);

        for (PointMatch p : initPoints) {
            boolean tooClose = false;
            for (PointMatch kept : filtered) {
                double dx = p.point.getX() - kept.point.getX();
                double dy = p.point.getY() - kept.point.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < minDistance) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    private Color getColor(Color startingColor, int iteration) {
        if (iteration <= 0) {
            return startingColor;
        }

        // enlighter factor (0 → init color, 1 → white)
        double factor = Math.min(iteration * 0.4, 1.0);
//        double factor = 1 - Math.exp(-iteration * 0.25);
        int r = startingColor.getRed();
        int g = startingColor.getGreen();
        int b = startingColor.getBlue();

        int newR = (int) (r + (255 - r) * factor);
        int newG = (int) (g + (255 - g) * factor);
        int newB = (int) (b + (255 - b) * factor);

        return new Color(newR, newG, newB);
    }

    private BufferedImage highLightPointMatches(Robot robot, List<PointMatch> matches1,
            List<PointMatch> matches2, int duration, String extraMessage, String extraMessage2) throws InterruptedException {
        // duration = -1 --> Not debug screenshots
        // duration = 0 --> debug but not highlight
        // duration > 0 --> debug + hightlight
        if (duration < 0) {
            return null;
        }

        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenCapture = robot.createScreenCapture(screenRect);

//        return null;
        DebugOverlay overlay = new DebugOverlay(screenCapture);
        int i = 0;
        for (PointMatch match : matches1) {
            double score = match.score;
            Rectangle r = new Rectangle(
                    (int) match.templateTLCorner.x,
                    (int) match.templateTLCorner.y,
                    match.templateCols,
                    match.templateRow
            );
            overlay.addZone(r, match.point, score, getColor(Color.RED, i++), extraMessage);
        }
        i = 0;
        for (PointMatch match : matches2) {
            double score = match.score;
            Rectangle r = new Rectangle(
                    (int) match.templateTLCorner.x,
                    (int) match.templateTLCorner.y,
                    match.templateCols,
                    match.templateRow
            );
            overlay.addZone(r, match.point, score, getColor(Color.BLUE, i++), extraMessage2);
        }
//        if (duration >= 1) {
//            overlay.setVisible(true);
//            Thread.sleep(duration * 1000);
//            overlay.setVisible(false);
//            overlay.dispose();
//        }
        return overlay.drawDebug();
    }

    private BufferedImage highLightPointMatches(Robot robot, List<PointMatch> matches1, int duration, String extraMessage) throws InterruptedException {
        return highLightPointMatches(robot, matches1, new ArrayList<PointMatch>(), duration, extraMessage, "");
    }

    private static Mat bufferedImageToMat(BufferedImage bi) {

        BufferedImage converted
                = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        converted.getGraphics().drawImage(bi, 0, 0, null);

        byte[] pixels = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();

        Mat mat = new Mat(converted.getHeight(), converted.getWidth(), CV_8UC3);
        mat.data().put(pixels);

        return mat;
    }

    private void typeKeyboard(String action, Robot robot, String text, String modifier, double delay) {
        if (!"".equals(modifier)) {
            int keyCodeModif = KeyCodeEnum.getAwtKeyCode(modifier);
            LOG.info("[{}] Type key {} --> {} with delay {} ms", action, modifier, keyCodeModif, (int) (delay * 1000));
            robot.keyPress(keyCodeModif);
            robot.delay((int) (delay * 1000));
        }
        for (char c : text.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            LOG.info("[{}] Type key {} --> {} with delay {} ms", action, c, keyCode, (int) (delay * 1000));
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
            robot.delay((int) (delay * 1000));
        }
        if (!"".equals(modifier)) {
            int keyCodeModif = KeyCodeEnum.getAwtKeyCode(modifier);
            LOG.info("[{}] Release key {} --> {} with delay {} ms", action, modifier, keyCodeModif, (int) (delay * 1000));
            robot.keyRelease(keyCodeModif);
        }
    }

    private String convertTextToType(String text) {
        for (KeyCodeEnum en : values()) {
            text = text.replace(en.getKeyName(), KeyEvent.getKeyText(en.getKeyCode()));
        }
        return text;
    }

    private String getMessageFromListPoints(List<PointMatch> p) {
        String mess = "";
        if (p.size() == 1) {
            return MESSAGE_1ELEMENTFOUND;
        } else if (p.size() > 1) {
            return MESSAGE_MANYELEMENTFOUND.replace("%NB%", String.valueOf(p.size()));
        } else {
            return MESSAGE_ELEMENTNOTFOUND;
        }
    }

    private String getMessageFromListPoints(List<PointMatch> p, List<PointMatch> p2) {
        String mess = "";
        if ((p.size() == 1) && (p2.size() == 1)) {
            return MESSAGE_DND_1ELEMENTFOUND;
        } else {
            return MESSAGE_DND_MANYELEMENTFOUND.replace("%NB1%", String.valueOf(p.size())).replace("%NB2%", String.valueOf(p2.size()));
        }
    }

    private String getScreenshotInBase64(String rootPictureFolder) {
        String picture = "";
        String screenshotPath = rootPictureFolder + File.separator + "Screenshot.png";
        InputStream istream = null;
        try {

            Robot robot = new Robot();

            Rectangle screenRect = new Rectangle(
                    Toolkit.getDefaultToolkit().getScreenSize());

            BufferedImage screenCapture = robot.createScreenCapture(screenRect);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenCapture, "png", baos);

            picture = Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException ex) {
            LOG.warn(ex, ex);
        } catch (AWTException ex) {
            LOG.error(ex, ex);
        } catch (Exception ex) {
            LOG.error(ex, ex);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ex) {
                    LOG.warn(ex, ex);
                }
            }
        }
        return picture;
    }

}
