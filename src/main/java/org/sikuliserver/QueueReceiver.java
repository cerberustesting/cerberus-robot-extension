/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

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
import org.sikuliserver.version.Infos;

/**
 *
 * @author bcivel
 */
public class QueueReceiver {

    private static final Logger LOG = LogManager.getLogger(QueueReceiver.class);

    public static void main(String[] args) throws IOException {

        try {

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
                formatter.printHelp("utility-name", options);

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
                LOG.info("Set HighlightElement parameter to " + cmd.getOptionValue("highlightElement") + " seconds");
            }

            /**
             * Display help if -h present
             */
            if (cmd.hasOption("help")) {
                formatter.printHelp("utility-name", options);
                System.exit(1);
                return;
            }

            /*
             * Start the server
             */
            Infos infos = new Infos();
            LOG.info(infos.getProjectNameAndVersion() + " - Build " + infos.getProjectBuildId() + " - Http Server Launching on port : " + portParam);
            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(Integer.parseInt(portParam));
            server.setConnectors(new Connector[]{connector});

            ServletHandler servletHandler = new ServletHandler();

            server.setHandler(servletHandler);
            servletHandler.addServletWithMapping(ExecuteSikuliAction.class, "/extra/ExecuteSikuliAction");
            LOG.info("Servlet listening on : /extra/ExecuteSikuliAction");

            server.start();
            server.join();
            LOG.warn("Server Stopped.");

        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
    }

    private static void setLogLevelToDebug() {
        Configurator.setLevel(System.getProperty("log4j.logger"), Level.DEBUG);
        LOG.debug("Debug mode enabled");
    }

}
