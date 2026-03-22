package org.cerberus.robot.extension.sikuli;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugOverlay extends JWindow {

    private static final Logger LOG = LogManager.getLogger(DebugOverlay.class);

    private BufferedImage screenCapture;

    private BufferedImage result;

    static class Zone {

        Rectangle rect;
        double score;
        Color color;
        String extraMessage;
        Point cursor;

        Zone(Rectangle rect, Point cursor, double score, Color color, String extraMessage) {
            this.rect = rect;
            this.score = score;
            this.color = color;
            this.extraMessage = extraMessage;
            this.cursor = cursor;
        }
    }

    private final List<Zone> zones = new ArrayList<>();

    public DebugOverlay(BufferedImage screenCapture) {

        this.screenCapture = screenCapture;

//        setBackground(new Color(0, 0, 0, 1));
//        setAlwaysOnTop(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screen.width, screen.height);

        setContentPane(new OverlayPanel());
    }

    public void addZone(Rectangle r, Point cursor, double score, Color c, String extraMessage) {
        zones.add(new Zone(r, cursor, score, c, extraMessage));
        repaint();
    }

    public BufferedImage drawDebug() {

        if (result == null
                || result.getWidth() != screenCapture.getWidth()
                || result.getHeight() != screenCapture.getHeight()) {

            result = new BufferedImage(
                    screenCapture.getWidth(),
                    screenCapture.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
        }

        Graphics2D g2 = result.createGraphics();

        try {

            g2.drawImage(screenCapture, 0, 0, null);

            for (Zone z : zones) {
                g2.setStroke(new BasicStroke(3));
                // Draw rectangle around the zone
                g2.setColor(z.color);
                g2.drawRect(z.rect.x, z.rect.y, z.rect.width, z.rect.height);

                // Draw the score & action avove the rectangle
                g2.setColor(z.color);
                g2.drawString(
                        String.format("score=%.3f - %s", z.score, z.extraMessage),
                        z.rect.x,
                        z.rect.y - 5
                );

                // Draw a cross to display the precise pixel where the action will be done
                g2.setStroke(new BasicStroke(1));
                int x = z.cursor.x;
                int y = z.cursor.y;
                g2.drawLine(x - 10, y, x + 10, y);
                g2.drawLine(x, y - 10, x, y + 10);
            }

        } finally {
            g2.dispose();
        }

        return result;
    }

    class OverlayPanel extends JPanel {

        OverlayPanel() {
            setOpaque(false);
//            setOpacity((float) 0.2);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            try {

                g2.drawImage(screenCapture, null, 0, 0);

                for (Zone z : zones) {
                    g2.setStroke(new BasicStroke(3));
                    // Draw rectangle around the zone
                    g2.setColor(z.color);
                    g2.drawRect(z.rect.x, z.rect.y, z.rect.width, z.rect.height);

                    // Draw the score & action avove the rectangle
                    g2.setColor(z.color);
                    g2.drawString(
                            String.format("score=%.3f - %s", z.score, z.extraMessage),
                            z.rect.x,
                            z.rect.y - 5
                    );

                    // Draw a cross to display the precise pixel where the action will be done
                    g2.setStroke(new BasicStroke(1));
                    int x = z.cursor.x;
                    int y = z.cursor.y;
                    g2.drawLine(x - 10, y, x + 10, y);
                    g2.drawLine(x, y - 10, x, y + 10);
                }
            } finally {
                g2.dispose();
            }

        }
    }
}
