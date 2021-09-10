/*
import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainWindow extends JFrame {

    public MainWindow(){
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(20,50);
        setSize(500,500);
        setTitle("MainWindow");
        initGUI();
    }

    private void initGUI(){
        Container container = getContentPane();
        GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);
        JPanel linesPanel = new JPanel(){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponents(g);
                g.setColor(Color.RED);
                g.drawLine(20,80,20,200);
                g.drawLine(30,200,140,200);
            }
        };

        JTextArea textArea = new JTextArea("Ovo je proizvoljni tekst\nI gledam je li ovo dobroo\n");
        textArea.setEnabled(false);
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    setVisible(false);
                    dispose();
                }
            }
        };
        container.add(linesPanel);
        container.add(textArea);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }

}
 */
