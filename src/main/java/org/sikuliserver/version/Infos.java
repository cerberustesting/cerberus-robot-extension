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
package org.sikuliserver.version;

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
public final class Infos {

    /**
     * {@link ResourceBundle} to the <code>infos</code> translation file
     */
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Infos.class);

    /**
     * Version separator
     */
    public static final String VERSION_SEPARATOR = "-";

    /**
     * Access to the single instance of this class
     */
    private static final Infos INSTANCE = new Infos();

    /**
     * Gets the single class instance
     *
     * @return the single class instance
     */
    public static Infos getInstance() {
        return INSTANCE;
    }

    /**
     * The project name
     */
    private String projectName;

    /**
     * The project version
     */
    private String projectVersion;

    /**
     * Concatenation between project name and version
     */
    private String projectNameAndVersion;

    /**
     * Gets the project name
     *
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the project version
     *
     * @return the project version
     */
    public String getProjectVersion() {
        return projectVersion;
    }

    /**
     * Gets the concatenation between the project name and version
     *
     * @return the concatenation between the project name and version
     * @see #getProjectName()
     * @see #getProjectVersion()
     */
    public String getProjectNameAndVersion() {
        return projectNameAndVersion;
    }

    /**
     * Private constructor as singleton class
     */
    public Infos() {
        init();
    }

    /**
     * Initialisation process
     */
    private void init() {

        try {
            final Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("infos.properties"));
            projectName = properties.getProperty("project.name");
            projectVersion = properties.getProperty("project.version");
            projectNameAndVersion = projectName + VERSION_SEPARATOR + projectVersion;
        } catch (IOException ex) {
            LOG.error(ex, ex);
        }
    }

}
