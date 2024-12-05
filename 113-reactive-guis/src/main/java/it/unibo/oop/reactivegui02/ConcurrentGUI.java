package it.unibo.oop.reactivegui02;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Second example of reactive GUI.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class ConcurrentGUI extends JFrame {
    
    private static final long serialVersionUID = 2L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel scene = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");

    /**
     * Builds a new CGUI
     */
    public ConcurrentGUI(){
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

        final Changing change = new Changing();
        new Thread(change).start();

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
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.scene.setText(nextText));
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
            SwingUtilities.invokeAndWait(()->ConcurrentGUI.this.up.setEnabled(false));
            SwingUtilities.invokeAndWait(()->ConcurrentGUI.this.down.setEnabled(false));
                
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

}
