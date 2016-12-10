/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Screen;
import org.sikuli.script.App;
import org.sikuli.basics.Settings;

/**
 *
 * @author bcivel
 */
public class SikuliAction {

    int doAction(String action, String picture, String text) throws FindFailed {
        int result = 0;
        Screen s = new Screen();

        switch (action) {
            case "openApp":
                App app = new App(text);
                app.open();
                result=1;
                break;
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
                result = 1;
                break;
            case "type":
                result = s.click(picture);
                if (result == 1){
                result = s.paste(text);
                }
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
            case "verifyTextInPage":
                Settings.OcrTextSearch = true;
                Settings.OcrTextRead = true;
                //result = find(text);
                break;
            case "takeScreenshot":
                s.capture(s.getBounds());
                break;
            //TODO Send picture to Cerberus
        }
        return result;
    }
}
