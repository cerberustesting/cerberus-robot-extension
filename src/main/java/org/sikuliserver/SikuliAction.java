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
import org.sikuli.script.Env;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;

/**
 *
 * @author bcivel
 */
public class SikuliAction {

    private static final Logger LOG = LogManager.getLogger(SikuliAction.class);

    JSONObject doAction(String action, String picture, String text) throws FindFailed {
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
                        //If picture is defined, click on it before pasting the text
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
                            case "Key.TAB":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.TAB);
                                } else {
                                    res = s.type(Key.TAB);
                                }
                                break;
                            case "Key.SHIFT":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.SHIFT);
                                } else {
                                    res = s.type(Key.SHIFT);
                                }
                                break;
                            case "Key.DELETE":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.DELETE);
                                } else {
                                    res = s.type(Key.DELETE);
                                }
                                break;
                            case "Key.ENTER":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.ENTER);
                                } else {
                                    res = s.type(Key.ENTER);
                                }
                                break;
                            case "Key.ESC":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.ESC);
                                } else {
                                    res = s.type(Key.ESC);
                                }
                                break;
                            case "Key.BACKSPACE":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.BACKSPACE);
                                } else {
                                    res = s.type(Key.BACKSPACE);
                                }
                                break;
                            case "Key.INSERT":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.INSERT);
                                } else {
                                    res = s.type(Key.INSERT);
                                }
                                break;
                            case "Key.LEFT":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.LEFT);
                                } else {
                                    res = s.type(Key.LEFT);
                                }
                                break;
                            case "Key.RIGHT":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.RIGHT);
                                } else {
                                    res = s.type(Key.RIGHT);
                                }
                                break;
                            case "Key.DOWN":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
                                    res = s.type(picture, Key.DOWN);
                                } else {
                                    res = s.type(Key.DOWN);
                                }
                                break;
                            case "Key.UP":
                                if (!"".equals(picture)) {
                                    if (highlightElement) {
                                        s.find(picture).highlight(numberOfSeconds);
                                    }
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
