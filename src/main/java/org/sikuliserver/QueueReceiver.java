/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

import java.io.File;
import org.sikuliserver.sikuli.ExecuteSikuliAction;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.sikuliserver.filemanagement.ExecuteFilemanagementAction;
import org.sikuliserver.management.ExecuteManagementAction;
import org.sikuliserver.version.Infos;

/**
 *
 * @author bcivel
 */
public class QueueReceiver {

    private static final Logger LOG = LogManager.getLogger(QueueReceiver.class);

    public static void main(String[] args) throws IOException {

        try {

            Infos infos = new Infos();

            /**
             * Parse Arguments
             */
            Options options = new Options();

            Option port = new Option("p", "port", true, "Start the server on this [INTEGER] <port>");
            port.setRequired(false);
            options.addOption(port);

            Option debug = new Option("d", "debug", false, "Start the server in debug mode");
            debug.setRequired(false);
            options.addOption(debug);

            Option highlight = new Option("e", "highlightElement", true, "Highlight focussed element during this [INTEGER] <number of seconds>");
            highlight.setRequired(false);
            options.addOption(highlight);

            Option authorisedFolderScope = new Option("a", "authorisedFolderScope", true, "Limit the scope of manipulation of files on the given folder>");
            authorisedFolderScope.setRequired(false);
            options.addOption(authorisedFolderScope);

            Option help = new Option("h", "help", false, "Display the help message");
            help.setRequired(false);
            options.addOption(help);

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd;

            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp(infos.getProjectNameAndVersion(), options);

                System.exit(1);
                return;
            }

            /**
             * Change port if specified
             */
            String portParam = "5555";
            if (cmd.hasOption("port")) {
                portParam = cmd.getOptionValue("port");
            }

            /**
             * Set DebugMode if specified
             */
            if (cmd.hasOption("debug")) {
                LOG.info("Activating Debug Mode.");
                setLogLevelToDebug();
            }
            /**
             * Set Highlight Element if specified
             */
            if (cmd.hasOption("highlightElement")) {
                System.setProperty("highlightElement", cmd.getOptionValue("highlightElement"));
                LOG.info("Set highlightElement parameter to " + cmd.getOptionValue("highlightElement") + " seconds");
            }
            /**
             * Set Authorised Folder Scope if specified
             */
            if (cmd.hasOption("authorisedFolderScope")) {
                System.setProperty("authorisedFolderScope", cmd.getOptionValue("authorisedFolderScope"));
                LOG.info("Set authorisedFolderScope parameter to " + cmd.getOptionValue("authorisedFolderScope"));
            }
            String sauthorisedFolderScope = System.getProperty("authorisedFolderScope");
            if (sauthorisedFolderScope == null || "".equals(sauthorisedFolderScope)) {
                sauthorisedFolderScope = "";
                if (System.getProperty("java.io.tmpdir") != null) {
                    sauthorisedFolderScope = System.getProperty("java.io.tmpdir");
                } else {
                    String sep = "" + File.separatorChar;
                    if (sep.equalsIgnoreCase("/")) {
                        sauthorisedFolderScope = "/tmp";
                    } else {
                        sauthorisedFolderScope = "C:";
                    }
                    LOG.warn("Java Property (java.io.tmpdir) for temporary folder not defined. Default to :" + sauthorisedFolderScope);
                }
                System.setProperty("authorisedFolderScope", sauthorisedFolderScope);
            }

            /**
             * Display help if -h present
             */
            if (cmd.hasOption("help")) {
                formatter.printHelp(infos.getProjectNameAndVersion(), options);
                System.exit(1);
                return;
            }

            /*
             * Start the server
             */
            LOG.info(infos.getProjectNameAndVersion() + " - Build " + infos.getProjectBuildId() + " - Http Server Launching on port : " + portParam);
            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(Integer.parseInt(portParam));
            server.setConnectors(new Connector[]{connector});

            ServletHandler servletHandler = new ServletHandler();

            server.setHandler(servletHandler);
            servletHandler.addServletWithMapping(ExecuteSikuliAction.class, "/extra/ExecuteSikuliAction");
            LOG.info("Servlet listening on : /extra/ExecuteSikuliAction");
            servletHandler.addServletWithMapping(ExecuteFilemanagementAction.class, "/extra/ExecuteFilemanagementAction");
            LOG.info("Servlet listening on : /extra/ExecuteFilemanagementAction | Limited to scope : '{}'", System.getProperty("authorisedFolderScope"));
            servletHandler.addServletWithMapping(ExecuteManagementAction.class, "/extra/ExecuteManagementAction");
            LOG.info("Servlet listening on : /extra/ExecuteManagementAction");

            server.start();
            server.join();
            LOG.warn("Server Stopped.");

        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
    }

    private static void setLogLevelToDebug() {
        Logger logger = LogManager.getRootLogger();
        Configurator.setAllLevels(logger.getName(), Level.DEBUG);
        Configurator.setLevel(LOG.getName(), Level.DEBUG);
        Configurator.setAllLevels(logger.getName(), Level.DEBUG);
//        Configurator.setLevel(System.getProperty("log4j.logger"), Level.DEBUG);
        LOG.debug("Debug mode enabled");
    }

}
