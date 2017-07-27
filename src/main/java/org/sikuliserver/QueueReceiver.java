/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver;

import java.io.IOException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 *
 * @author bcivel
 */
public class QueueReceiver {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(QueueReceiver.class);

    public static void main(String[] args) throws IOException {

        try {
            int port = 5555;
            if (args.length > 1 && args[0].equals("-p")) {
                port = Integer.valueOf(args[1]);
            }

            LOG.info("Launching a HttpServer on port : " + port);
            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(port);
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
}
