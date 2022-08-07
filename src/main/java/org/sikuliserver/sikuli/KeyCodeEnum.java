/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver.sikuli;

import org.sikuli.script.Key;

/**
 *
 * @author vertigo
 */
public enum KeyCodeEnum {
    NOT_VALID(0, "NOT_VALID", ""),
    SPACE(1, "Key.SPACE", Key.SPACE),
    ENTER(2, "Key.ENTER", Key.ENTER),
    BACKSPACE(3, "Key.BACKSPACE", Key.BACKSPACE),
    TAB(4, "Key.TAB", Key.TAB),
    ESC(5, "Key.ESC", Key.ESC),
    UP(6, "Key.UP", Key.UP),
    RIGHT(7, "Key.RIGHT", Key.RIGHT),
    DOWN(8, "Key.DOWN", Key.DOWN),
    LEFT(9, "Key.LEFT", Key.LEFT),
    PAGE_UP(10, "Key.PAGE_UP", Key.PAGE_UP),
    PAGE_DOWN(11, "Key.PAGE_DOWN", Key.PAGE_DOWN),
    DELETE(12, "Key.DELETE", Key.DELETE),
    END(13, "Key.END", Key.END),
    HOME(14, "Key.HOME", Key.HOME),
    INSERT(15, "Key.INSERT", Key.INSERT),
    F1(16, "Key.F1", Key.F1),
    F2(17, "Key.F2", Key.F2),
    F3(18, "Key.F3", Key.F3),
    F4(19, "Key.F4", Key.F4),
    F5(20, "Key.F5", Key.F5),
    F6(21, "Key.F6", Key.F6),
    F7(22, "Key.F7", Key.F7),
    F8(23, "Key.F8", Key.F8),
    F9(24, "Key.F9", Key.F9),
    F10(25, "Key.F10", Key.F10),
    F11(26, "Key.F11", Key.F11),
    F12(27, "Key.F12", Key.F12),
    F13(28, "Key.F13", Key.F13),
    F14(29, "Key.F14", Key.F14),
    F15(30, "Key.F15", Key.F15),
    SHIFT(31, "Key.SHIFT", Key.SHIFT),
    CTRL(32, "Key.CTRL", Key.CTRL),
    ALT(33, "Key.ALT", Key.ALT),
    ALTGR(34, "Key.ALTGR", Key.ALTGR),
    META(35, "Key.META", Key.META),
    CMD(36, "Key.CMD", Key.CMD),
    WIN(37, "Key.WIN", Key.F12),
    PRINTSCREEN(38, "Key.PRINTSCREEN", Key.PRINTSCREEN),
    SCROLL_LOCK(39, "Key.SCROLL_LOCK", Key.SCROLL_LOCK),
    PAUSE(40, "Key.PAUSE", Key.PAUSE),
    CAPS_LOCK(41, "Key.CAPS_LOCK", Key.CAPS_LOCK),
    NUM0(42, "Key.NUM0", Key.NUM0),
    NUM1(43, "Key.NUM1", Key.NUM1),
    NUM2(44, "Key.NUM2", Key.NUM2),
    NUM3(45, "Key.NUM3", Key.NUM3),
    NUM4(46, "Key.NUM4", Key.NUM4),
    NUM5(47, "Key.NUM5", Key.NUM5),
    NUM6(48, "Key.NUM6", Key.NUM6),
    NUM7(49, "Key.NUM7", Key.NUM7),
    NUM8(50, "Key.NUM8", Key.NUM8),
    NUM9(51, "Key.NUM9", Key.NUM9),
    SEPARATOR(52, "Key.SEPARATOR", Key.SEPARATOR),
    NUM_LOCK(53, "Key.NUM_LOCK", Key.NUM_LOCK),
    ADD(54, "Key.ADD", Key.ADD),
    MINUS(55, "Key.MINUS", Key.MINUS),
    MULTIPLY(56, "Key.MULTIPLY", Key.MULTIPLY),
    DIVIDE(57, "Key.DIVIDE", Key.DIVIDE),
    DECIMAL(58, "Key.DECIMAL", Key.DECIMAL),
    CONTEXT(59, "Key.CONTEXT", Key.CONTEXT),
    NEXT(60, "Key.NEXT", Key.NEXT),;

    private final int code;
    private final String keyName;
    private final String keyCode;

    private KeyCodeEnum(int code, String keyName, String keyCode) {
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
    public static String getSikuliKeyCode(String keyName) {
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

    public String getKeyCode() {
        return keyCode;
    }

}
