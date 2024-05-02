import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Main Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 100);

        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("First Menu");
        mb.add(m1);
        JMenuItem m11 = new JMenuItem("Item 1");
        m1.add(m11);
        m11.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Child Frame");
                frame.setSize(400, 100);

                JPanel panel = new JPanel();
                JLabel label = new JLabel("Enter Text");
                JTextField tf = new JTextField(10);
                JButton send = new JButton("Save");
                send.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        label.setText(tf.getText());
                    }
                });
                panel.add(label);
                panel.add(tf);
                panel.add(send);

                frame.getContentPane().add(BorderLayout.SOUTH, panel);
                frame.setVisible(true);
            }
        });
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.setVisible(true);
    }
}