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
                Settings.MinSimilarity = Double.parseDouble(minSimilarity);
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
                        switch (text) {
                            case "Key.SPACE":
                            case " ":
                                res = type(s, picture, Key.SPACE, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.ENTER":
                                res = type(s, picture, Key.ENTER, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.BACKSPACE":
                                res = type(s, picture, Key.BACKSPACE, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.TAB":
                                res = type(s, picture, Key.TAB, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.ESC":
                                res = type(s, picture, Key.ESC, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.UP":
                                res = type(s, picture, Key.UP, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.RIGHT":
                                res = type(s, picture, Key.RIGHT, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.DOWN":
                                res = type(s, picture, Key.DOWN, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.LEFT":
                                res = type(s, picture, Key.LEFT, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.PAGE_UP":
                                res = type(s, picture, Key.PAGE_UP, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.PAGE_DOWN":
                                res = type(s, picture, Key.PAGE_DOWN, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.DELETE":
                                res = type(s, picture, Key.DELETE, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.END":
                                res = type(s, picture, Key.END, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.HOME":
                                res = type(s, picture, Key.HOME, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.INSERT":
                                res = type(s, picture, Key.INSERT, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F1":
                                res = type(s, picture, Key.F1, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F2":
                                res = type(s, picture, Key.F2, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F3":
                                res = type(s, picture, Key.F3, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F4":
                                res = type(s, picture, Key.F4, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F5":
                                res = type(s, picture, Key.F5, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F6":
                                res = type(s, picture, Key.F6, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F7":
                                res = type(s, picture, Key.F7, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F8":
                                res = type(s, picture, Key.F8, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F9":
                                res = type(s, picture, Key.F9, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F10":
                                res = type(s, picture, Key.F10, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F11":
                                res = type(s, picture, Key.F11, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F12":
                                res = type(s, picture, Key.F12, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F13":
                                res = type(s, picture, Key.F13, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F14":
                                res = type(s, picture, Key.F14, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.F15":
                                res = type(s, picture, Key.F15, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.SHIFT":
                                res = type(s, picture, Key.SHIFT, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.CTRL":
                                res = type(s, picture, Key.CTRL, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.ALT":
                                res = type(s, picture, Key.ALT, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.ALTGR":
                                res = type(s, picture, Key.ALTGR, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.META":
                                res = type(s, picture, Key.META, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.CMD":
                                res = type(s, picture, Key.CMD, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.WIN":
                                res = type(s, picture, Key.WIN, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.PRINTSCREEN":
                                res = type(s, picture, Key.PRINTSCREEN, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.SCROLL_LOCK":
                                res = type(s, picture, Key.SCROLL_LOCK, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.PAUSE":
                                res = type(s, picture, Key.PAUSE, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.CAPS_LOCK":
                                res = type(s, picture, Key.CAPS_LOCK, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM0":
                                res = type(s, picture, Key.NUM0, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM1":
                                res = type(s, picture, Key.NUM1, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM2":
                                res = type(s, picture, Key.NUM2, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM3":
                                res = type(s, picture, Key.NUM3, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM4":
                                res = type(s, picture, Key.NUM4, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM5":
                                res = type(s, picture, Key.NUM5, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM6":
                                res = type(s, picture, Key.NUM6, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM7":
                                res = type(s, picture, Key.NUM7, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM8":
                                res = type(s, picture, Key.NUM8, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM9":
                                res = type(s, picture, Key.NUM9, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.SEPARATOR":
                                res = type(s, picture, Key.SEPARATOR, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NUM_LOCK":
                                res = type(s, picture, Key.NUM_LOCK, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.ADD":
                                res = type(s, picture, Key.ADD, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.MINUS":
                                res = type(s, picture, Key.MINUS, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.MULTIPLY":
                                res = type(s, picture, Key.MULTIPLY, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.DIVIDE":
                                res = type(s, picture, Key.DIVIDE, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.DECIMAL":
                                res = type(s, picture, Key.DECIMAL, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.CONTEXT":
                                res = type(s, picture, Key.CONTEXT, numberOfSeconds, doHighlightElement);
                                break;
                            case "Key.NEXT":
                                res = type(s, picture, Key.NEXT, numberOfSeconds, doHighlightElement);
                                break;
                        }
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
                LOG.error(ex, ex);
            }
        }
        return result;
    }

    private int type(Screen s, String picture, String text, int numberOfSeconds, boolean highlightElement) throws FindFailed {
        int result = 0;
        if (!"".equals(picture)) {
            if (highlightElement) {
                s.find(picture).highlight(numberOfSeconds);
            }
            result = s.type(picture, text);
        } else {
            result = s.type(text);
        }
        return result;
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
