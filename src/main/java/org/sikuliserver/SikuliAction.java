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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
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

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SikuliAction.class);

    JSONObject doAction(String action, String picture, String text) throws FindFailed {
        JSONObject result = new JSONObject();

        try {
            /**
             * Result Object init with result KO
             */
            String status = "Failed";
            Screen s = new Screen();

            /**
             * Switch on Action. Do the action. If no exception raised, or
             * result from sikuli = 1, update status to OK
             */
            try {
                switch (action) {
                    case "openApp":
                        App app = new App(text);
                        app.open();
                        status = "OK";
                        break;
                    case "closeApp":
                        App openApp = new App(text);
                        openApp.close();
                        status = "OK";
                        break;
                    case "click":
                        if (1 == s.click(picture)) {
                            status = "OK";
                        }
                        break;
                    case "doubleClick":
                        if (1 == s.doubleClick(picture)) {
                            status = "OK";
                        }
                        break;
                    case "rightClick":
                        if (1 == s.rightClick(picture)) {
                            status = "OK";
                        }
                        break;
                    case "mouseOver":
                        if (1 == s.hover(picture)) {
                            status = "OK";
                        }
                        break;
                    case "wait":
                        s.wait(picture);
                        status = "OK";
                        break;
                    case "waitVanish":
                        s.waitVanish(picture);
                        status = "OK";
                        break;
                    case "paste":
                        //If picture is defined, click on it before pasting the text
                        if (!"".equals(picture)) {
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
                            case "Key.TAB":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.TAB);
                                } else {
                                    res = s.type(Key.TAB);
                                }
                                break;
                            case "Key.SHIFT":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.SHIFT);
                                } else {
                                    res = s.type(Key.SHIFT);
                                }
                                break;
                            case "Key.DELETE":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.DELETE);
                                } else {
                                    res = s.type(Key.DELETE);
                                }
                                break;
                            case "Key.ENTER":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.ENTER);
                                } else {
                                    res = s.type(Key.ENTER);
                                }
                                break;
                            case "Key.ESC":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.ESC);
                                } else {
                                    res = s.type(Key.ESC);
                                }
                                break;
                            case "Key.BACKSPACE":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.BACKSPACE);
                                } else {
                                    res = s.type(Key.BACKSPACE);
                                }
                                break;
                            case "Key.INSERT":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.INSERT);
                                } else {
                                    res = s.type(Key.INSERT);
                                }
                                break;
                            case "Key.LEFT":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.LEFT);
                                } else {
                                    res = s.type(Key.LEFT);
                                }
                                break;
                            case "Key.RIGHT":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.RIGHT);
                                } else {
                                    res = s.type(Key.RIGHT);
                                }
                                break;
                            case "Key.DOWN":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.DOWN);
                                } else {
                                    res = s.type(Key.DOWN);
                                }
                                break;
                            case "Key.UP":
                                if (!"".equals(picture)) {
                                    res = s.type(picture, Key.UP);
                                } else {
                                    res = s.type(Key.UP);
                                }
                                break;
                        }
                        if (1 == res) {
                            status = "OK";
                        }
                        ;
                        break;
                    //DEPRECATED >> Replaced by exists
                    //To Remove after Cerberus Release
                    case "verifyElementPresent":
                        if (s.exists(picture) != null) {
                            status = "OK";
                        }
                        break;
                    case "exists":
                        if (s.exists(picture) != null) {
                            status = "OK";
                        }
                        break;
                    //DEPRECATED >> Replaced by findText
                    //To Remove after Cerberus Release
                    case "verifyTextInPage":
                        Settings.OcrTextSearch = true;
                        Settings.OcrTextRead = true;
                        if (s.findText(text) != null) {
                            status = "OK";
                        }
                        break;
                    case "findText":
                        Settings.OcrTextSearch = true;
                        Settings.OcrTextRead = true;
                        if (s.findText(text) != null) {
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

    public String getScreenshotInBase64() {
        String picture = "";
        String screenshotPath = "picture" + File.separator + "Screenshot.png";
        try {
            Screen s = new Screen();
            ScreenImage screenshot = s.capture(s.getBounds());
            ImageIO.write(screenshot.getImage(), "PNG", new File(screenshotPath));

            /**
             * Get Picture from URL and convert to Base64
             */
            InputStream istream = new FileInputStream(new File(screenshotPath));

            /**
             * Encode in Base64
             */
            byte[] bytes = IOUtils.toByteArray(istream);
            picture = Base64.encodeBase64URLSafeString(bytes);

        } catch (IOException ex) {
            LOG.warn(ex);
        }
        return picture;
    }
}
