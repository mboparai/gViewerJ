/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osl.java.gurbani.frontend;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Boparai
 */
public class AudioPlayer extends javax.swing.JFrame {

    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;
    Canvas canvas = null;
    /**
     * Creates new form AudioPlayer
     */
    public AudioPlayer() {
        initComponents();
        initializeScreen();
        initializeEnvironment();
    }
    
    public void initializeEnvironment(){
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), ".");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        
        canvas = new Canvas();
        canvas.setBackground(Color.black);
        this.add(canvas, BorderLayout.CENTER);
    
        factory = new MediaPlayerFactory();
        mediaPlayer = factory.newEmbeddedMediaPlayer(new DefaultFullScreenStrategy(this));
        mediaPlayer.setVideoSurface(factory.newVideoSurface(canvas));

        mediaPlayer.setPlaySubItems(true);
    }
    
    public void initializeScreen(){
        this.setLocationRelativeTo(this.getOwner());
        this.setSize(500, 100);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setVisible(true);
    }
    
    public void play(String url){
        mediaPlayer.playMedia(url);
        new Thread(){

            @Override
            public void run() {
                super.run();
                ignite();
            }
        }.start();
    }

    public void ignite(){
        Graphics g = canvas.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.red);
        g2.setStroke(new BasicStroke(3));
        //g.drawLine(50, 50, 400, 75);
        
        while(mediaPlayer.isPlaying()){
            try {
                Thread.sleep(500);
                g2.setColor(new Color((int)Math.random()*255, (int)Math.random()*255, (int)Math.random()*255));
                g2.drawLine((int)Math.random()*500, (int)Math.random()*50, (int)Math.random()*500, (int)Math.random()*50);
            } catch (InterruptedException ex) {
                Logger.getLogger(AudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        mediaPlayer.stop();
        mediaPlayer.release();
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AudioPlayer().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
