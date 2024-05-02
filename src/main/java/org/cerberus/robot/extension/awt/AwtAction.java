/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.awt;

//import java.awt.MouseInfo;
//import java.awt.Point;
//import java.awt.PointerInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.swing.core.BasicComponentFinder;
import org.assertj.swing.core.ComponentFinder;
import org.assertj.swing.driver.JMenuItemMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.cerberus.robot.extension.sikuli.KeyCodeEnum;
import org.json.JSONException;
import org.json.JSONObject;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.cerberus.robot.extension.sikuli.KeyCodeEnum.values;

/**
 *
 * @author bcivel
 */
public class AwtAction {

    private static final Logger LOG = LogManager.getLogger(AwtAction.class);

    JSONObject doAction(String action, String text, String text2, Double typeDelay) throws FindFailed {

        JSONObject result = new JSONObject();

        Settings.DebugLogs = true;
        Settings.InfoLogs = true;
        Settings.ProfileLogs = true;
        Settings.TraceLogs = true;
        Settings.ActionLogs = true;
        Settings.UserLogs = true;

        boolean doHighlightElement = false;
        int numberOfSeconds = 0;

        try {
            /**
             * Result Object init with result KO
             */
            String status = "Failed";
            String message = null;
            String stacktrace = null;
            Screen s = new Screen();

            /**
             * Switch on Action. Do the action. If no exception raised, or
             * result from sikuli = 1, update status to OK
             */
            try {
                switch (action) {

                    case "ping":
                        LOG.info("Ping.");
                        status = "OK";
                        break;
                    case "openApp":
                        LOG.info("Opening Application : " + text);
                        /*App app = new App(text);
                        app.open();*/
                    {


                        try {
                            File jar = new File("C:\\java\\Dlp\\Src\\Project\\Logistique\\JAVA_Logistique\\target\\Logistique-0.0.0-SNAPSHOT-jar-with-dependencies.jar");

                            addToClasspath(jar);
                            //Set<Class> classesFromJarFile = getClassesFromJarFile(jar);

                            //Class classToLoad = child.loadClass("FctMenu.Logistique.Menu.MenuLogistique");
                            Class classToLoad = Class.forName("FctMenu.Logistique.Menu.MenuLogistique");
                            //Class classToLoad = classesFromJarFile.stream().filter(aClass -> "FctMenu.Logistique.Menu.MenuLogistique".equals(aClass.getName())).findFirst().orElseThrow(Exception::new);
                            Method method = classToLoad.getDeclaredMethod("main", String[].class);
                            Object instance = classToLoad.newInstance();
                            final Object[] args = new Object[1];
                            args[0] = new String[] { "1", "2"};
                            Object r = method.invoke(instance, args);
                        } catch (Throwable e){
                            e.printStackTrace();
                        }


                        //ApplicationLauncher.application(MenuLogistique.class).start();


                        /*App openApp = new App(text);
                        openApp.open(30);
                        finder ne fonctionne pas avec cette façon de faire
                        */

                        ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

                        JMenuItem webMenu = (JMenuItem) finder.find(new JMenuItemMatcher("Web"));
                        GuiActionRunner.execute(() -> webMenu.doClick());
                        JMenuItem dde = (JMenuItem) finder.find(webMenu, new JMenuItemMatcher("Infos Commande web"));
                        GuiActionRunner.execute(() -> dde.doClick());
                    }
                        status = "OK";
                        break;
                    case "closeApp":
                        LOG.info("Closing Application : " + text);
                        App openApp = new App(text);
                        openApp.close();
                        status = "OK";
                        break;

                    // All actions that require a single element.
                    case "click":
                    case "doubleClick":
                    case "rightClick":
                    case "mouseOver":
                        break;

                    case "dragAndDrop":
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
                        break;

                    case "waitVanish":
                        break;

                    case "paste":
                        // If picture is defined, click on it before pasting the text
                            if (1 == s.paste(text)) {
                                status = "OK";
                            }
                        break;

                    case "type":
                        int res = 0;
                        LOG.info("About to key :'" + text + "' with modifier '" + text2 + "'");
                        text = convertTextToType(text);
                        text2 = convertTextToType(text2);
                        LOG.info("Key done :'" + text + "' with modifier '" + text2 + "'");
                        if (1 == res) {
                            status = "OK";
                        }
                        break;

                    case "exists":
                            // TEXT
                            if (s.existsText(text) != null) {
                                status = "OK";
                                // We found the picture so we can hightlight it.
                                if (doHighlightElement) {
                                    LOG.debug("Highlighting Element.");
                                    s.findText(text).highlight(numberOfSeconds);
                                }
                            }

                        break;

                    case "notExists":
                        // PICTURE
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
                        /*String screenshotInBase64 = getScreenshotInBase64(rootPictureFolder);
                        status = "OK";
                        result.put("screenshot", screenshotInBase64);*/
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

    public static Set<String> getClassNamesFromJarFile(File givenFile) throws IOException {
        Set<String> classNames = new HashSet<>();
        try (JarFile jarFile = new JarFile(givenFile)) {
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry jarEntry = e.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    classNames.add(className);
                }
            }
            return classNames;
        }
    }

    URLClassLoader child;

    public Set<Class> getClassesFromJarFile(File jarFile) throws Exception {
        Set<String> classNames = getClassNamesFromJarFile(jarFile);
        Set<Class> classes = new HashSet<>(classNames.size());

        addPath("jar:file:" + jarFile + "!/");

        child = new URLClassLoader(
                new URL[] { new URL("jar:file:" + jarFile + "!/")},
                this.getClass().getClassLoader()
        );
        return classes;
    }

    public static void addPath(String s) throws Exception {
        File f = new File(s);
        URL u = f.toURL();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{u});
    }

    public static void addToClasspath(File file) {
        try {
            URL url = file.toURI().toURL();

            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    private String getMessageWitthOffset(String message, Region region, int xOffset, int yOffset, Double minSimilarity) {
        String extraMessage1 = "";
        if (region == null) {
            extraMessage1 = "";
        } else {
            extraMessage1 = "Element (" + region.getCenter().getX() + "," + region.getCenter().getY() + ")";
        }
        String extraMessage2 = "";
        if (xOffset == 0 && yOffset == 0) {
            extraMessage2 = "";
        } else {
            extraMessage2 = "Offset (" + xOffset + "," + yOffset + ")";
        }
        String extraMessage3 = "";
        if (minSimilarity != null) {
            extraMessage3 = "Similarity (" + minSimilarity + ")";
        }
        if (!extraMessage1.equals("")) {
            message = message + " - " + extraMessage1;
        }
        if (!extraMessage2.equals("")) {
            message = message + " - " + extraMessage2;
        }
        if (!extraMessage3.equals("")) {
            message = message + " - " + extraMessage3;
        }
        return message;
    }

    private int type(Screen s, String picture, String text, String text2, int numberOfSeconds, boolean highlightElement, Double typeDelay) throws FindFailed {
        LOG.info("Setting Type Delay to : " + typeDelay);
        Settings.TypeDelay = typeDelay;
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
