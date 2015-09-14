/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Screen;

/**
 *
 * @author bcivel
 */
public class SikuliAction {

    int doAction(String action, String picture, String text) throws FindFailed {
        int result = 0;
        Screen s = new Screen();

        switch (action) {
            case "click":
                result = s.click(picture);
                break;
            case "doubleClick":
                result = s.doubleClick(picture);
                break;
            case "rightClick":
                result = s.rightClick(picture);
                break;
            case "mouseOver":
                result = s.hover(picture);
                break;
            case "wait":
                s.wait(picture);
                break;
            case "type":
                result = s.paste(picture, text);
                break;
            case "keyPress":
                switch (text) {
                    case "Key.TAB":
                        result = s.type(picture, Key.TAB);
                        break;
                    case "Key.SHIFT":
                        result = s.type(picture, Key.SHIFT);
                        break;
                }
                ;
                break;
            case "verifyElementPresent":
                s.exists(picture);
                break;
            case "takeScreenshot":
                s.capture(s.getBounds());
                break;
            //TODO Send picture to Cerberus
        }
        return result;
    }
}
