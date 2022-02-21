/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

//import java.awt.MouseInfo;
//import java.awt.Point;
//import java.awt.PointerInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import static org.sikuliserver.KeyCodeEnum.values;

/**
 *
 * @author bcivel
 */
public class SikuliAction {

    private static final Logger LOG = LogManager.getLogger(SikuliAction.class);

    JSONObject doAction(String action, String picture, String picture2, String text, String text2, String minSimilarity, int numberOfSecondsHighlightElement, String rootPictureFolder) throws FindFailed {
        JSONObject result = new JSONObject();

        Settings.DebugLogs = true;
        Settings.InfoLogs = true;
        Settings.ProfileLogs = true;
        Settings.TraceLogs = true;
        Settings.ActionLogs = true;
        Settings.UserLogs = true;

        boolean doHighlightElement = false;
        int numberOfSeconds = 0;
        if (System.getProperty("highlightElement") != null) {
            try {
                numberOfSeconds = Integer.valueOf(System.getProperty("highlightElement"));
            } catch (Exception ex) {
                LOG.warn("Exception parsing highlightElement argument : " + ex);
            }
        }
        if (numberOfSecondsHighlightElement > numberOfSeconds) {
            numberOfSeconds = numberOfSecondsHighlightElement;
        }
        if (numberOfSeconds > 0) {
            doHighlightElement = true;
        }

        try {
            /**
             * Result Object init with result KO
             */
            String status = "Failed";
            String message = null;
            String stacktrace = null;
            Screen s = new Screen();

            if (minSimilarity != null) {
                LOG.debug("Setting MinSimilarity to : " + minSimilarity);
                try {
                    Double newMinSimilarity = Double.parseDouble(minSimilarity);
                    Settings.MinSimilarity = Double.parseDouble(minSimilarity);
                } catch (Exception e) {
                    LOG.error("minSimilarity parameter format is not valid : " + String.valueOf(minSimilarity) + " - Should be un double format (ex : 0.7). Value default to 0.7");
                    minSimilarity = "0.7";
                }

            } else {
                LOG.info("minSimilarity parameter format is not defined. Value default to 0.7");
                minSimilarity = "0.7";
            }

            /**
             * Switch on Action. Do the action. If no exception raised, or
             * result from sikuli = 1, update status to OK
             */
            try {
                switch (action) {
                    case "openApp":
                        LOG.info("Opening Application : " + text);
                        App app = new App(text);
                        app.open();
                        status = "OK";
                        break;
                    case "closeApp":
                        LOG.info("Closing Application : " + text);
                        App openApp = new App(text);
                        openApp.close();
                        status = "OK";
                        break;
                    case "click":
                        // Simple click
                        if ("".equals(picture) && "".equals(text)) {
                            LOG.debug("Simple Click Action.");
                            Location loc = Mouse.at();
                            loc.click();
                            Thread.sleep(500);
                            status = "OK";
                        }
                        // click on a picture
                        if (!"".equals(picture)) {
                            LOG.debug("Click on a picture Action.");
                            if (doHighlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.click(picture)) {
                                status = "OK";
                            }
                        }
                        // click on a text
                        if (!"".equals(text)) {
                            LOG.debug("Click on a text Action.");
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (doHighlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }
                            if (1 == s.find(text).click()) {
                                status = "OK";
                            }
                        }
                        break;
                    case "dragAndDrop":
                        Settings.OcrTextSearch = true;
                        Settings.OcrTextRead = true;

                        // DragAndDrop on picture
                        Match elementDrag = !"".equals(picture) ? s.find(picture) : s.find(text);
                        Match elementDrop = !"".equals(picture2) ? s.find(picture2) : s.find(text2);

                        if (doHighlightElement) {
                            elementDrag.highlight(numberOfSeconds);
                            elementDrop.highlight(numberOfSeconds);
                        }
                        if (1 == s.dragDrop(elementDrag, elementDrop)) {
                            status = "OK";
                        }
                        break;
                    case "doubleClick":
                        // Simple click
                        if ("".equals(picture) && "".equals(text)) {
                            LOG.debug("Simple Doubleclick Action.");
                            Location loc = Mouse.at();
                            loc.click();
                            Thread.sleep(50);
                            loc.click();
                            Thread.sleep(500);
                            status = "OK";
                        }
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (doHighlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            if (1 == s.find(text).doubleClick()) {
                                status = "OK";
                            }
                        } else {
                            if (doHighlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.doubleClick(picture)) {
                                status = "OK";
                            }
                        }
                        break;
                    case "rightClick":
                        // Simple click
                        if ("".equals(picture) && "".equals(text)) {
                            LOG.debug("Simple Rightclick Action.");
                            Location loc = Mouse.at();
                            loc.rightClick();
                            Thread.sleep(500);
                            status = "OK";
                        }
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (doHighlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            if (1 == s.find(text).rightClick()) {
                                status = "OK";
                            }
                        } else {
                            if (doHighlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.rightClick(picture)) {
                                status = "OK";
                            }
                        }
                        break;
                    case "mouseOver":
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (doHighlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            if (1 == s.find(text).hover()) {
                                status = "OK";
                            }
                        } else {
                            if (doHighlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.hover(picture)) {
                                status = "OK";
                            }
                        }
                        break;
                    case "mouseDown":
                        Mouse.down(Button.LEFT);
                        status = "OK";
                        break;
                    case "mouseUp":
                        Mouse.up(Button.LEFT);
                        status = "OK";
                        break;
                    case "mouseMove":
                        String[] instructions = text.split(";");
                        for (String instruction : instructions) {
                            String curInstruction = instruction.trim();
                            if (!curInstruction.isEmpty()) {

                                // Display position before move.
                                Location loc = Mouse.at();
                                int x = (int) loc.getX();
                                int y = (int) loc.getY();
                                LOG.info("Current Position : " + x + " | " + y);

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
                                    Location tmpPos = s.getBottomRight();
                                    x1 = tmpPos.getX() / 2;
                                    y1 = tmpPos.getY() / 2;
                                } else {
                                    String[] pos = curInstruction.trim().split(",");
                                    x1 = Integer.valueOf(pos[0].trim());
                                    if (pos.length > 1) {
                                        y1 = Integer.valueOf(pos[1].trim());
                                    }
                                }

                                if (isAbsolute) {
                                    LOG.info("Move (Absolute) : " + x1 + " | " + y1);
                                    Location newPos = new Location(x1, y1);
                                    Mouse.move(newPos);
                                } else {
                                    LOG.info("Move (Relative) : " + x1 + " | " + y1);
                                    Mouse.move(x1, y1);
                                }

                                status = "OK";
                                Thread.sleep(1000);

                                // Display position after move.
                                loc = Mouse.at();
                                x = (int) loc.getX();
                                y = (int) loc.getY();
                                LOG.info("New Position : " + x + " | " + y);
                            }
                        }
                        break;
                    case "wait":
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (doHighlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            s.wait(text);
                            status = "OK";
                        } else {
                            if (doHighlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            s.wait(picture);
                            status = "OK";
                        }
                        break;
                    case "waitVanish":
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (doHighlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            s.waitVanish(text);
                            status = "OK";
                        } else {
                            if (doHighlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            s.waitVanish(picture);
                            status = "OK";
                        }
                        break;
                    case "paste":
                        // If picture is defined, click on it before pasting the text
                        if (!"".equals(picture)) {
                            if (doHighlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.paste(picture, text)) {
                                status = "OK";
                            }
                        } else {
                            if (1 == s.paste(text)) {
                                status = "OK";
                            }
                        }
                        break;
                    case "type":
                        int res = 0;
                        LOG.info("About to key :'" + text + "' with modifier '" + text2 + "'");
                        text = convertTextToType(text);
                        text2 = convertTextToType(text2);
                        res = type(s, picture, text, text2, numberOfSeconds, doHighlightElement);
                        LOG.info("Key done :'" + text + "' with modifier '" + text2 + "'");
                        if (1 == res) {
                            status = "OK";
                        }
                        break;
                    case "exists":
                        if (s.exists(new Pattern(picture).similar(Double.parseDouble(minSimilarity))) != null) {
                            status = "OK";
                            // We found the picture so we can hightlight it.
                            if (doHighlightElement) {
                                LOG.debug("Highlighting Element.");
                                s.find(picture).highlight(numberOfSeconds);
                            }
                        }
                        break;
                    case "notExists":
                        if (s.exists(picture) != null) {
                            status = "KO";
                            // We found the picture so we can hightlight it.
                            if (doHighlightElement) {
                                LOG.debug("Highlighting Element.");
                                s.find(picture).highlight(numberOfSeconds);
                            }
                        }
                        break;
                    case "findText":
                        Settings.OcrTextSearch = true;
                        Settings.OcrTextRead = true;
                        //Region r = new Region(s.x, s.y, s.w, s.h);
                        if (doHighlightElement) {
                            s.find(text).highlight(numberOfSeconds);
                        }
                        if (s.find(text) != null) {
                            status = "OK";
                        }
                        break;
                    case "capture":
                        String screenshotInBase64 = getScreenshotInBase64(rootPictureFolder);
                        status = "OK";
                        result.put("screenshot", screenshotInBase64);
                        break;
                    case "endExecution":
                        /**
                         * The aim of that action is to put back the status of
                         * the robot to a stable situation. For example, we stop
                         * pressing the mouse button in case it was pressed.
                         */
                        Mouse.up(Button.LEFT);
                        status = "OK";
                        break;
                }

//            } catch (FindFailed ex) {
//                LOG.info(ex);
//                throw ex;
//            } catch (FindFailed ex) {
//                message = "Failed finding element '" + picture + "'";
//                if (minSimilarity != null) {
//                    message += " with minSimilarity: " + minSimilarity;
//                }
//                message += " | " + ex.toStringShort();
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                ex.printStackTrace(pw);
//
//                stacktrace = sw.toString();
//                    LOG.info(message);
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

                result.put("status", "Failed");
                result.put("message", message);
                result.put("stacktrace", stacktrace);
            } catch (JSONException ex1) {
                LOG.error(ex1, ex1);
            }
        }
        return result;
    }

    private int type(Screen s, String picture, String text, String text2, int numberOfSeconds, boolean highlightElement) throws FindFailed {
        int result = 0;
        if (!"".equals(picture)) {
            if (highlightElement) {
                s.find(picture).highlight(numberOfSeconds);
            }
            if (!"".equals(text2)) {
                result = s.type(picture, text, text2);
            } else {
                result = s.type(picture, text);
            }
        } else {
            if (!"".equals(text2)) {
                result = s.type(null, text, text2);
            } else {
                result = s.type(null, text);
            }
        }
        return result;
    }

    private String convertTextToType(String text) {
        for (KeyCodeEnum en : values()) {
            text = text.replace(en.getKeyName(), en.getKeyCode());
        }
        return text;
    }

    private String getScreenshotInBase64(String rootPictureFolder) {
        String picture = "";
        String screenshotPath = rootPictureFolder + File.separator + "Screenshot.png";
        InputStream istream = null;
        try {
            Screen s = new Screen();
            ScreenImage screenshot = s.capture(s.getBounds());
            ImageIO.write(screenshot.getImage(), "PNG", new File(screenshotPath));

            /**
             * Get Picture from URL and convert to Base64
             */
            istream = new FileInputStream(new File(screenshotPath));

            /**
             * Encode in Base64
             */
            byte[] bytes = IOUtils.toByteArray(istream);
            picture = Base64.encodeBase64URLSafeString(bytes);

        } catch (IOException ex) {
            LOG.warn(ex);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ex) {
                    LOG.warn(ex);
                }
            }
        }
        return picture;
    }

}
