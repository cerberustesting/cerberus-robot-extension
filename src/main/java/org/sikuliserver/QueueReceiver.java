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

            Option port = new Option("p", "port", true, "port");
            port.setRequired(false);
            options.addOption(port);

            Option debug = new Option("d", "debug", false, "debug");
            debug.setRequired(false);
            options.addOption(debug);
            
            Option highlight = new Option("h", "highlightElement", true, "highlightElement");
            highlight.setRequired(false);
            options.addOption(highlight);

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
            if (cmd.hasOption("port")){
            portParam = cmd.getOptionValue("port");
            } 
            
            /**
             * Set DebugMode if specified
             */
            if (cmd.hasOption("debug")){
                setLogLevelToDebug();
            }
            /**
             * Set Highlight Element if specified
             */
            if (cmd.hasOption("highlightElement")){
                System.setProperty("highlightElement", cmd.getOptionValue("highlightElement"));
            }

            
            /*
             * Start the server
             */
            LOG.info("Launching a HttpServer on port : " + port);
            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(Integer.parseInt(portParam));
            server.setConnectors(new Connector[]{connector});

            ServletHandler servletHandler = new ServletHandler();

            server.setHandler(servletHandler);
            servletHandler.addServletWithMapping(ExecuteSikuliAction.class, "/extra/ExecuteSikuliAction");

            server.start();
            server.join();
            LOG.warn("Server Stopped.");

        } catch (Exception ex) {
            LOG.warn(ex);
        }
    }

    private static void setLogLevelToDebug() {
        Configurator.setLevel(System.getProperty("log4j.logger"), Level.DEBUG);
        LOG.debug("debug enabled");
    }

}
