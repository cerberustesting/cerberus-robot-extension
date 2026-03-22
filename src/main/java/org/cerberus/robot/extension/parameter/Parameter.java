/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.robot.extension.parameter;

import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;

/**
 * Contains information about project.
 *
 * <p>
 * Singleton class to use thanks to the {@link #getInstance()} method.
 * </p>
 *
 * @author abourdon
 */
public final class Parameter {

    /**
     * {@link ResourceBundle} to the <code>infos</code> translation file
     */
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Parameter.class);

    /**
     * Access to the single instance of this class
     */
    private static final Parameter INSTANCE = new Parameter();

    /**
     * Gets the single class instance
     *
     * @return the single class instance
     */
    public static Parameter getInstance() {
        return INSTANCE;
    }

    /**
     * The project version
     */
    private int highlightElement;

    /**
     * Concatenation between project name and version
     */
    private String authorisedFolderScope;

    public int getHighlightElement() {
        return highlightElement;
    }

    public void setHighlightElement(int highlightElement) {
        this.highlightElement = highlightElement;
    }

    public String getAuthorisedFolderScope() {
        return authorisedFolderScope;
    }

    public void setAuthorisedFolderScope(String authorisedFolderScope) {
        this.authorisedFolderScope = authorisedFolderScope;
    }

    /**
     * Private constructor as singleton class
     */
    public Parameter() {
        init();
    }

    /**
     * Initialisation process
     */
    private void init() {

    }

}
