import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.AWTKeyStroke.getAWTKeyStroke;
import static java.awt.event.KeyEvent.SHIFT_DOWN_MASK;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author KIIT
 */
public class TyperXWorker
{

    Robot robot;
    TyperXFrame ui;
    private AtomicInteger isRunning;
    public static final int RUNNING = 1;
    public static final int STOPPED = 0;
    private int startTime = 5;
    private int strokeDelay = 8;

    public TyperXWorker(TyperXFrame frame) throws AWTException
    {
        this.robot = new Robot();
        this.ui = frame;
        this.isRunning = new AtomicInteger(STOPPED);
    }

    public static AWTKeyStroke getKeyStroke(char c)
    {
        String upper = "`~'\"!@#$%^&*()_+{}|:<>?";
        String lower = "`~'\"1234567890-=[]\\;,./";

        int index = upper.indexOf(c);
        if (index != -1) {
            int keyCode;
            boolean shift = false;
            switch (c) {
                // these chars need to be handled specially because
                // they don't directly translate into the correct keycode
                case '~':
                case '\"':
                    shift = true;
                case '`':
                    keyCode = KeyEvent.VK_BACK_QUOTE;
                    break;
                case '\'':
                    keyCode = KeyEvent.VK_QUOTE;
                    break;
                default:
                    keyCode = (int) Character.toUpperCase(lower.charAt(index));
                    shift = true;
            }
            return getAWTKeyStroke(keyCode, shift ? SHIFT_DOWN_MASK : 0);
        }
        return getAWTKeyStroke((int) Character.toUpperCase(c), 0);
    }


    private void pressKey(char c, int ms)
    {
        AWTKeyStroke keyStroke = getKeyStroke(c);
        int keyCode = keyStroke.getKeyCode();
        boolean shift = Character.isUpperCase(c) || keyStroke.getModifiers() == (InputEvent.SHIFT_MASK | SHIFT_DOWN_MASK);
        if (shift) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }


        try{
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
            if (ms > 0) {
                Thread.sleep(ms);
            }
        } catch (Exception e) {
//                System.out.println("Error printing "+ KeyEvent.getKeyText(keyCode));
//                e.printStackTrace();
        }


        if (shift) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }

    }


    private void sendKeys(String keys)
    {
        Runnable run = () -> {

            for (int i = 0; i < startTime; i++) {
                if (isRunning.get() == STOPPED) {
                    break;
                }
                this.ui.lableInfo.setText("Starting typing in " + (startTime - i) + " sec");
                try{
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TyperXFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.ui.lableInfo.setText("Running  now..");
            char[] charArray = keys.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (isRunning.get() == STOPPED) {
                    break;
                }
                try{
                    pressKey(c, strokeDelay);
                    int progress = (i * 100) / charArray.length;
                    this.ui.progress.setValue(progress);
                } catch (Exception ex) {

                    Logger.getLogger(TyperXFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ui.startUI();
            this.isRunning.set(STOPPED);
        };
        Thread th = new Thread(run);
        th.start();
    }

    public void startRequest(String text)
    {
        if (isRunning.compareAndSet(STOPPED, RUNNING)) {
            ui.stopUI();
            this.sendKeys(text);
        }

    }

    public void stopRequest()
    {
        if (this.isRunning.compareAndSet(RUNNING, STOPPED)) {
            ui.startUI();
        }
    }


    public int getStartTime()
    {
        return startTime;
    }

    public void setStartTime(int startTime)
    {
        this.startTime = startTime;
    }

    public int getStrokeDelay()
    {
        return strokeDelay;
    }

    public void setStrokeDelay(int strokeDelay)
    {
        this.strokeDelay = strokeDelay;
    }
}
