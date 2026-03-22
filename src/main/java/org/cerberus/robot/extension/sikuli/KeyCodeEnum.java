/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.robot.extension.sikuli;

//import org.sikuli.script.Key;

import java.awt.event.KeyEvent;


/**
 *
 * @author vertigo
 */
public enum KeyCodeEnum {
    NOT_VALID(0, "NOT_VALID", 0),
    SPACE(1, "Key.SPACE", KeyEvent.VK_SPACE),
    ENTER(2, "Key.ENTER", KeyEvent.VK_ENTER),
    BACKSPACE(3, "Key.BACKSPACE", KeyEvent.VK_BACK_SPACE),
    TAB(4, "Key.TAB", KeyEvent.VK_TAB),
    ESC(5, "Key.ESC", KeyEvent.VK_ESCAPE),
    UP(6, "Key.UP", KeyEvent.VK_UP),
    RIGHT(7, "Key.RIGHT", KeyEvent.VK_RIGHT),
    DOWN(8, "Key.DOWN", KeyEvent.VK_DOWN),
    LEFT(9, "Key.LEFT", KeyEvent.VK_LEFT),
    PAGE_UP(10, "Key.PAGE_UP", KeyEvent.VK_PAGE_UP),
    PAGE_DOWN(11, "Key.PAGE_DOWN", KeyEvent.VK_PAGE_DOWN),
    DELETE(12, "Key.DELETE", KeyEvent.VK_DELETE),
    END(13, "Key.END", KeyEvent.VK_END),
    HOME(14, "Key.HOME", KeyEvent.VK_HOME),
    INSERT(15, "Key.INSERT", KeyEvent.VK_INSERT),
    F1(16, "Key.F1", KeyEvent.VK_F1),
    F2(17, "Key.F2", KeyEvent.VK_F2),
    F3(18, "Key.F3", KeyEvent.VK_F3),
    F4(19, "Key.F4", KeyEvent.VK_F4),
    F5(20, "Key.F5", KeyEvent.VK_F5),
    F6(21, "Key.F6", KeyEvent.VK_F6),
    F7(22, "Key.F7", KeyEvent.VK_F7),
    F8(23, "Key.F8", KeyEvent.VK_F8),
    F9(24, "Key.F9", KeyEvent.VK_F9),
    F10(25, "Key.F10", KeyEvent.VK_F10),
    F11(26, "Key.F11", KeyEvent.VK_F11),
    F12(27, "Key.F12", KeyEvent.VK_F12),
    F13(28, "Key.F13", KeyEvent.VK_F13),
    F14(29, "Key.F14", KeyEvent.VK_F14),
    F15(30, "Key.F15", KeyEvent.VK_F15),
    SHIFT(31, "Key.SHIFT", KeyEvent.VK_SHIFT),
    CTRL(32, "Key.CTRL", KeyEvent.VK_CONTROL),
    ALT(33, "Key.ALT", KeyEvent.VK_ALT),
    ALTGR(34, "Key.ALTGR", KeyEvent.VK_ALT_GRAPH),
    META(35, "Key.META", KeyEvent.VK_META),
    CMD(36, "Key.CMD", KeyEvent.VK_META),
    WIN(37, "Key.WIN", KeyEvent.VK_F12),
    PRINTSCREEN(38, "Key.PRINTSCREEN", KeyEvent.VK_PRINTSCREEN),
    SCROLL_LOCK(39, "Key.SCROLL_LOCK", KeyEvent.VK_SCROLL_LOCK),
    PAUSE(40, "Key.PAUSE", KeyEvent.VK_PAUSE),
    CAPS_LOCK(41, "Key.CAPS_LOCK", KeyEvent.VK_CAPS_LOCK),
    NUM0(42, "Key.NUM0", KeyEvent.VK_NUMPAD0),
    NUM1(43, "Key.NUM1", KeyEvent.VK_NUMPAD1),
    NUM2(44, "Key.NUM2", KeyEvent.VK_NUMPAD2),
    NUM3(45, "Key.NUM3", KeyEvent.VK_NUMPAD3),
    NUM4(46, "Key.NUM4", KeyEvent.VK_NUMPAD4),
    NUM5(47, "Key.NUM5", KeyEvent.VK_NUMPAD5),
    NUM6(48, "Key.NUM6", KeyEvent.VK_NUMPAD6),
    NUM7(49, "Key.NUM7", KeyEvent.VK_NUMPAD7),
    NUM8(50, "Key.NUM8", KeyEvent.VK_NUMPAD8),
    NUM9(51, "Key.NUM9", KeyEvent.VK_NUMPAD9),
    SEPARATOR(52, "Key.SEPARATOR", KeyEvent.VK_SEPARATOR),
    NUM_LOCK(53, "Key.NUM_LOCK", KeyEvent.VK_NUM_LOCK),
    ADD(54, "Key.ADD", KeyEvent.VK_ADD),
    MINUS(55, "Key.MINUS", KeyEvent.VK_MINUS),
    MULTIPLY(56, "Key.MULTIPLY", KeyEvent.VK_MULTIPLY),
    DIVIDE(57, "Key.DIVIDE", KeyEvent.VK_DIVIDE),
    DECIMAL(58, "Key.DECIMAL", KeyEvent.VK_DECIMAL),
    CONTEXT(59, "Key.CONTEXT", KeyEvent.VK_CONTEXT_MENU),
    NEXT(60, "Key.NEXT", KeyEvent.VK_PAGE_DOWN),;

    private final int code;
    private final String keyName;
    private final int keyCode;

    private KeyCodeEnum(int code, String keyName, int keyCode) {
        this.code = code;
        this.keyName = keyName;
        this.keyCode = keyCode;
    }

    /**
     * Gets the Sikuli key code with basis on the name of the key
     *
     * @param keyName - name of the key
     * @return keyCode if property is defined in the enumeration, false if not
     */
    public static int getAwtKeyCode(String keyName) {
        for (KeyCodeEnum en : values()) {
            if (en.getKeyName().compareTo(keyName) == 0) {
                return en.getKeyCode();
            }
        }
        return NOT_VALID.getKeyCode();
    }

    public int getCode() {
        return code;
    }

    public String getKeyName() {
        return keyName;
    }

    public int getKeyCode() {
        return keyCode;
    }

}
