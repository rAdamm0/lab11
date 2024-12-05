package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 2L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel scene = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    final Observer observe = new Observer();
    final Changing change = new Changing();

    /**
     * Builds a new CGUI
     */
    public AnotherConcurrentGUI(){
        super();
        final Dimension screeDimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int)(screeDimension.getWidth()*WIDTH_PERC) , (int)(screeDimension.getHeight()*HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(scene);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);

        
        new Thread(change).start();
        new Thread(observe).start();

        stop.addActionListener((e)->change.stopCounting());
        up.addActionListener((e)->change.increase());
        down.addActionListener((e)->change.decrease());

        
    }

    private class Changing implements Runnable{

        private volatile boolean stop;
        private volatile boolean decreasing;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.scene.setText(nextText));
                    if(decreasing){
                        this.counter--;
                     }else{
                        this.counter++;
                     }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
            try {
            SwingUtilities.invokeAndWait(()->AnotherConcurrentGUI.this.up.setEnabled(false));
            SwingUtilities.invokeAndWait(()->AnotherConcurrentGUI.this.down.setEnabled(false));
                
            } catch (InvocationTargetException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        public void stopCounting(){
            this.stop = true;
        }

        public void increase(){
            this.decreasing = false;
        }

        public void decrease(){
            this.decreasing = true;
        }

    }

    private class Observer implements Runnable{

        @Override
        public void run() {
            try {
                Thread.sleep(10000);    
                SwingUtilities.invokeAndWait(()->AnotherConcurrentGUI.this.change.stopCounting());
                
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }
}
