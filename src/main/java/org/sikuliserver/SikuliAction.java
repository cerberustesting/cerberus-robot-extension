/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Screen;
import org.sikuli.script.App;
import org.sikuli.basics.Settings;
import org.sikuli.script.ScreenImage;

/**
 *
 * @author bcivel
 */
public class SikuliAction {

    private static final Logger LOG = LogManager.getLogger(SikuliAction.class);

    JSONObject doAction(String action, String picture, String text, String minSimilarity) throws FindFailed {
        JSONObject result = new JSONObject();

        boolean highlightElement = false;
        int numberOfSeconds = 2;

        if (System.getProperty("highlightElement") != null) {
            highlightElement = true;
            try {
                numberOfSeconds = Integer.valueOf(System.getProperty("highlightElement"));
                LOG.info("Set highlightElement to " + numberOfSeconds + " seconds");
            } catch (Exception ex) {
                LOG.warn("Exception parsing highlightElement argument : " + ex);
                LOG.warn("Set highlightElement to its default value : 2 seconds");
            }

        }

        try {
            /**
             * Result Object init with result KO
             */
            String status = "Failed";
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
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (highlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            if (1 == s.find(text).click()) {
                                status = "OK";
                            }
                        } else {
                            if (highlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.click(picture)) {
                                status = "OK";
                            }
                        }
                        break;
                    case "doubleClick":
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (highlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            if (1 == s.find(text).doubleClick()) {
                                status = "OK";
                            }
                        } else {
                            if (highlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.doubleClick(picture)) {
                                status = "OK";
                            }
                        }
                        break;
                    case "rightClick":
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (highlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            if (1 == s.find(text).rightClick()) {
                                status = "OK";
                            }
                        } else {
                            if (highlightElement) {
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
                            if (highlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            if (1 == s.find(text).hover()) {
                                status = "OK";
                            }
                        } else {
                            if (highlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            if (1 == s.hover(picture)) {
                                status = "OK";
                            }
                        }
                        break;
                    case "wait":
                        if ("".equals(picture)) {
                            Settings.OcrTextSearch = true;
                            Settings.OcrTextRead = true;
                            if (highlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            s.wait(text);
                            status = "OK";
                        } else {
                            if (highlightElement) {
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
                            if (highlightElement) {
                                s.find(text).highlight(numberOfSeconds);
                            }

                            s.waitVanish(text);
                            status = "OK";
                        } else {
                            if (highlightElement) {
                                s.find(picture).highlight(numberOfSeconds);
                            }
                            s.waitVanish(picture);
                            status = "OK";
                        }
                        break;
                    case "paste":
                        // If picture is defined, click on it before pasting the text
                        if (!"".equals(picture)) {
                            if (highlightElement) {
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
                                res = type(s, picture, Key.SPACE, numberOfSeconds, highlightElement);
                                break;
                            case "Key.ENTER":
                                res = type(s, picture, Key.ENTER, numberOfSeconds, highlightElement);
                                break;
                            case "Key.BACKSPACE":
                                res = type(s, picture, Key.BACKSPACE, numberOfSeconds, highlightElement);
                                break;
                            case "Key.TAB":
                                res = type(s, picture, Key.TAB, numberOfSeconds, highlightElement);
                                break;
                            case "Key.ESC":
                                res = type(s, picture, Key.ESC, numberOfSeconds, highlightElement);
                                break;
                            case "Key.UP":
                                res = type(s, picture, Key.UP, numberOfSeconds, highlightElement);
                                break;
                            case "Key.RIGHT":
                                res = type(s, picture, Key.RIGHT, numberOfSeconds, highlightElement);
                                break;
                            case "Key.DOWN":
                                res = type(s, picture, Key.DOWN, numberOfSeconds, highlightElement);
                                break;
                            case "Key.LEFT":
                                res = type(s, picture, Key.LEFT, numberOfSeconds, highlightElement);
                                break;
                            case "Key.PAGE_UP":
                                res = type(s, picture, Key.PAGE_UP, numberOfSeconds, highlightElement);
                                break;
                            case "Key.PAGE_DOWN":
                                res = type(s, picture, Key.PAGE_DOWN, numberOfSeconds, highlightElement);
                                break;
                            case "Key.DELETE":
                                res = type(s, picture, Key.DELETE, numberOfSeconds, highlightElement);
                                break;
                            case "Key.END":
                                res = type(s, picture, Key.END, numberOfSeconds, highlightElement);
                                break;
                            case "Key.HOME":
                                res = type(s, picture, Key.HOME, numberOfSeconds, highlightElement);
                                break;
                            case "Key.INSERT":
                                res = type(s, picture, Key.INSERT, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F1":
                                res = type(s, picture, Key.F1, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F2":
                                res = type(s, picture, Key.F2, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F3":
                                res = type(s, picture, Key.F3, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F4":
                                res = type(s, picture, Key.F4, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F5":
                                res = type(s, picture, Key.F5, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F6":
                                res = type(s, picture, Key.F6, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F7":
                                res = type(s, picture, Key.F7, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F8":
                                res = type(s, picture, Key.F8, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F9":
                                res = type(s, picture, Key.F9, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F10":
                                res = type(s, picture, Key.F10, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F11":
                                res = type(s, picture, Key.F11, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F12":
                                res = type(s, picture, Key.F12, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F13":
                                res = type(s, picture, Key.F13, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F14":
                                res = type(s, picture, Key.F14, numberOfSeconds, highlightElement);
                                break;
                            case "Key.F15":
                                res = type(s, picture, Key.F15, numberOfSeconds, highlightElement);
                                break;
                            case "Key.SHIFT":
                                res = type(s, picture, Key.SHIFT, numberOfSeconds, highlightElement);
                                break;
                            case "Key.CTRL":
                                res = type(s, picture, Key.CTRL, numberOfSeconds, highlightElement);
                                break;
                            case "Key.ALT":
                                res = type(s, picture, Key.ALT, numberOfSeconds, highlightElement);
                                break;
                            case "Key.ALTGR":
                                res = type(s, picture, Key.ALTGR, numberOfSeconds, highlightElement);
                                break;
                            case "Key.META":
                                res = type(s, picture, Key.META, numberOfSeconds, highlightElement);
                                break;
                            case "Key.CMD":
                                res = type(s, picture, Key.CMD, numberOfSeconds, highlightElement);
                                break;
                            case "Key.WIN":
                                res = type(s, picture, Key.WIN, numberOfSeconds, highlightElement);
                                break;
                            case "Key.PRINTSCREEN":
                                res = type(s, picture, Key.PRINTSCREEN, numberOfSeconds, highlightElement);
                                break;
                            case "Key.SCROLL_LOCK":
                                res = type(s, picture, Key.SCROLL_LOCK, numberOfSeconds, highlightElement);
                                break;
                            case "Key.PAUSE":
                                res = type(s, picture, Key.PAUSE, numberOfSeconds, highlightElement);
                                break;
                            case "Key.CAPS_LOCK":
                                res = type(s, picture, Key.CAPS_LOCK, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM0":
                                res = type(s, picture, Key.NUM0, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM1":
                                res = type(s, picture, Key.NUM1, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM2":
                                res = type(s, picture, Key.NUM2, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM3":
                                res = type(s, picture, Key.NUM3, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM4":
                                res = type(s, picture, Key.NUM4, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM5":
                                res = type(s, picture, Key.NUM5, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM6":
                                res = type(s, picture, Key.NUM6, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM7":
                                res = type(s, picture, Key.NUM7, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM8":
                                res = type(s, picture, Key.NUM8, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM9":
                                res = type(s, picture, Key.NUM9, numberOfSeconds, highlightElement);
                                break;
                            case "Key.SEPARATOR":
                                res = type(s, picture, Key.SEPARATOR, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NUM_LOCK":
                                res = type(s, picture, Key.NUM_LOCK, numberOfSeconds, highlightElement);
                                break;
                            case "Key.ADD":
                                res = type(s, picture, Key.ADD, numberOfSeconds, highlightElement);
                                break;
                            case "Key.MINUS":
                                res = type(s, picture, Key.MINUS, numberOfSeconds, highlightElement);
                                break;
                            case "Key.MULTIPLY":
                                res = type(s, picture, Key.MULTIPLY, numberOfSeconds, highlightElement);
                                break;
                            case "Key.DIVIDE":
                                res = type(s, picture, Key.DIVIDE, numberOfSeconds, highlightElement);
                                break;
                            case "Key.DECIMAL":
                                res = type(s, picture, Key.DECIMAL, numberOfSeconds, highlightElement);
                                break;
                            case "Key.CONTEXT":
                                res = type(s, picture, Key.CONTEXT, numberOfSeconds, highlightElement);
                                break;
                            case "Key.NEXT":
                                res = type(s, picture, Key.NEXT, numberOfSeconds, highlightElement);
                                break;
                        }
                        if (1 == res) {
                            status = "OK";
                        }
                        break;
                    case "exists":
                        if (highlightElement) {
                            s.find(picture).highlight(numberOfSeconds);
                        }
                        if (s.exists(picture) != null) {
                            status = "OK";
                        }
                        break;
                    case "notExists":
                        if (s.exists(picture) == null) {
                            status = "OK";
                        }
                        break;
                    case "findText":
                        Settings.OcrTextSearch = true;
                        Settings.OcrTextRead = true;
                        //Region r = new Region(s.x, s.y, s.w, s.h);
                        if (highlightElement) {
                            s.find(text).highlight(numberOfSeconds);
                        }
                        if (s.find(text) != null) {
                            status = "OK";
                        }
                        break;
                    case "capture":
                        String screenshotInBase64 = getScreenshotInBase64();
                        status = "OK";
                        result.put("screenshot", screenshotInBase64);
                        break;
                }

            } catch (Exception ex) {
                LOG.warn(ex);
            } finally {
                //Update the status
                result.put("status", status);
            }
        } catch (JSONException ex) {
            LOG.warn(ex);
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

    public String getScreenshotInBase64() {
        String picture = "";
        String screenshotPath = "picture" + File.separator + "Screenshot.png";
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
