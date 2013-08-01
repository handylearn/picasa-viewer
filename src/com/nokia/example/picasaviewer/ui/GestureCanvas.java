/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.ui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.tantalum.util.L;

import com.nokia.example.picasaviewer.PicasaViewer;
import com.nokia.example.picasaviewer.util.CategoryBarHandler;
import com.nokia.example.picasaviewer.util.GestureHandler;
import com.nokia.example.picasaviewer.util.ViewManager;

/**
 * A helper for creating gesture-based UIs that also work on older Series 40
 * phones.
 */
public abstract class GestureCanvas 
        extends Canvas 
        implements CommandListener
{
    // Constants
    protected static final int SPIN_SPEED = 100; // ms per animation frame
    private static final double R = 12;
    private static final int[] COLORS = {
        0x000000, 
        0xffffff, 
        0xdddddd, 
        0xbbbbbb, 
        0x999999, 
        0x777777, 
        0x333333};
    private static final int DOTS = COLORS.length;
    private static final double STEP = (2 * Math.PI) / DOTS;
    private static final double CIRCLE = (2 * Math.PI);
    private static final int TOUCH_MARGIN = 10;

    // Members
    protected static final Timer spinTimer = new Timer();
    private static TimerTask spinTimerTask = null;
    protected static Image backIconImage;
    protected final ViewManager viewManager;
    protected GestureHandler gestureHandler = null;
    protected CategoryBarHandler categoryBarHandler = null;
    protected int friction = GestureHandler.FRAME_ANIMATOR_FRICTION_LOW;
    protected int scrollY = 0;
    protected int top = -getHeight();
    protected boolean animating = false;
    protected boolean showCategoryBar = false; // Must be set explicitly
    protected double yC = 0;
    protected double xC = 0;
    private double angle = 0;
    private int startDot = 0;
    private int yOffset = 0;

    /**
     * Static construction.
     */
    static {
        if (PicasaViewer.HAS_ONE_KEY_BACK) {
            backIconImage = null;
        }
        else {
            try {
                backIconImage = Image.createImage("/back.png");
            }
            catch (IOException e) {
                //#debug
                L.e("Can not create back icon", null, e);
            }
        }
    }

    /**
     * Constructor.
     * @param viewManager The view manager instance.
     */
    public GestureCanvas(ViewManager viewManager) {
        this.viewManager = viewManager;
        
        if (viewManager.supportsCategoryBar()) {
            categoryBarHandler = viewManager.getCategoryBarHandler();
        }
        
        setCommandListener(this);
        
        try {
            gestureHandler =
                (GestureHandler) Class.forName(
                    "com.nokia.example.picasaviewer.util.GestureHandler")
                        .newInstance();
            gestureHandler.setCanvas(this);
            gestureHandler.register(0, scrollY);
        }
        catch (Throwable e) {
            //#debug
            L.e("GestureHandler", "can not instantiate", e);
        }
        
        xC = getWidth() / 2;
        yC = getHeight() / 2;
    }

    /**
     * @see javax.microedition.lcdui.Canvas#hideNotify()
     */
    public void hideNotify() {
        animating = false;
        stopSpinner();
    }

    /**
     * @see javax.microedition.lcdui.Canvas#sizeChanged(int, int)
     */
    public void sizeChanged(final int w, final int h) {
        gestureHandler.onCanvasSizeChanged();
        xC = w / 2;
        yC = h / 2;
    }

    /** 
     * @return The Y coordinate of the scroll position.
     */
    public int getScrollY() {
        return scrollY;
    }

    /**
     * Updates the animation. Paints the canvas based on the new scroll
     * position. 
     * @param y
     * @param deltaY
     * @param lastFrame True if there are no more frames to animate.
     */
    public void animate(final int y,
                        final short deltaY,
                        final boolean lastFrame)
    {
        scrollY += deltaY;
        animating = !lastFrame;
        repaint();
    }

    /**
     * Makes the Y coordinate of the scroll position stays within the
     * boundaries.
     */
    protected void checkThatScrollDoesNotExceedBoundaries() {
        if (scrollY < top) {
            //#debug
            L.i("checkScroll", "bang top scrollY=" + scrollY + " top=" + top);
            scrollY = top;
        }
        else if (scrollY > 0) {
            //#debug
            L.i("checkScroll", "bang bottom scrollY=" + scrollY);
            scrollY = 0;
        }
    }

    /**
     * Starts the spinner.
     */
    protected final synchronized void startSpinner() {
        if (spinTimerTask == null) {
            spinTimerTask = new TimerTask() {
                public void run() {
                    repaint();
                }
            };
            
            spinTimer.scheduleAtFixedRate(spinTimerTask,
                                          SPIN_SPEED, 
                                          SPIN_SPEED);
        }
    }

    /**
     * Stops the spinner.
     * @return True if the spinner was stopped.
     */
    protected final synchronized boolean stopSpinner() {
        boolean stopped = spinTimerTask != null;
        
        if (stopped) {
            spinTimerTask.cancel();
            spinTimerTask = null;
        }
        
        repaint();
        return stopped;
    }

    /** 
     * @return True if the spinner is spinning. False otherwise.
     */
    protected final synchronized boolean isSpinning() {
        return spinTimerTask != null;
    }

    /**
     * Draws the spinner.
     * @param graphics The Graphics instance.
     */
    protected void drawSpinner(final Graphics graphics) {
        if (isSpinning()) {
            for (int i = 0; i < DOTS; i++) {
                int x = (int) (xC + R * Math.cos(angle));
                int y = (int) (yC + R * Math.sin(angle));
                
                graphics.setColor(COLORS[(i + startDot) % DOTS]);
                graphics.fillRoundRect(x, y, 6, 6, 3, 3);
                
                angle = (angle - STEP) % CIRCLE;
            }
            
            startDot = ++startDot % DOTS;
        }
    }

    /**
     * Stops the animator in case e.g. flick animation is ongoing.
     * @see javax.microedition.lcdui.Canvas#pointerPressed(int, int)
     */
    protected void pointerPressed(int x, int y) {
        yOffset = 0;
        
        if (animating) {
            animating = false;
            gestureHandler.stopAnimator();
        }
    }

    //-------------------------------------------------------------------------
    // Gesture management methods ->

    /**
     * Handles the pinch gesture. No default implementation.
     * 
     * @param pinchDistanceStarting
     * @param pinchDistanceCurrent
     * @param pinchDistanceChange
     * @param centerX
     * @param centerY
     * @param centerChangeX
     * @param centerChangeY
     */
    public void gesturePinch(int pinchDistanceStarting,
                             int pinchDistanceCurrent,
                             int pinchDistanceChange,
                             int centerX,
                             int centerY,
                             int centerChangeX,
                             int centerChangeY)
    {
    }

    /**
     * Always call the super.gestureTap() in the hierarchy and accept if the
     * parent class has caught and consumed the tap event.
     *
     * @param startX The X coordinate of the tap event.
     * @param startY The Y coordinate of the tap event.
     * @return True of the tap was caught, handled and consumed.
     */
    public boolean gestureTap(int startX, int startY) {
        boolean gestureHandled = false;
        
        if (animating) {
            animating = false;
            gestureHandler.stopAnimator();
            gestureHandled = true;
        }
        else if (categoryBarHandler != null
                && categoryBarHandler.hasCustomCategoryBar()
                && categoryBarHandler.getCategoryBar().getVisibility()
                && startY > getHeight() - CustomCategoryBar.HEIGHT)
        {
            // Don't propagate taps when they happen on top of the category bar
            gestureHandled = true;
        }
        
        return gestureHandled;
    }

    /**
     * @param startX The X coordinate of the long press.
     * @param startY The Y coordinate of the long press.
     */
    public void gestureLongPress(int startX, int startY) {
    }

    /**
     * Long, long press.
     *
     * @param startX The X coordinate of the long press.
     * @param startY The Y coordinate of the long press.
     */
    public void gestureLongPressRepeated(int startX, int startY) {
    }

    /**
     * @param startX The start X coordinate of the drag.
     * @param startY The start Y coordinate of the drag.
     * @param dragDistanceX The distance and direction of the drag on X axis.
     * @param dragDistanceY The distance and direction of the drag on Y axis.
     */
    public void gestureDrag(int startX,
                            int startY,
                            int dragDistanceX,
                            int dragDistanceY)
    {
        // Uncomment the following to print details to console
        /*System.out.println("GestureCanvas.gestureDrag(): From ["
            + startX + ", " + startY + "] with delta [" + dragDistanceX + ", "
            + dragDistanceY + "]");*/
        yOffset += dragDistanceY;
        
        if (categoryBarHandler != null
            && categoryBarHandler.hasCustomCategoryBar()
            && categoryBarHandler.getCategoryBar().getVisibility()
            && startY + yOffset > getHeight() - CustomCategoryBar.HEIGHT - TOUCH_MARGIN)
        {
            // Don't animate when the drag happens on top of the category bar
            return;
        }
        
        animate(scrollY, (short)dragDistanceY, true);
    }

    /**
     * End of drag event with a drop event.
     *
     * @param startX
     * @param startY
     * @param dragDistanceX
     * @param dragDistanceY
     */
    public void gestureDrop(int startX,
                            int startY,
                            int dragDistanceX,
                            int dragDistanceY)
    {
        System.out.println("GestureCanvas.gestureDrop()");
        yOffset = 0;
    }

    /**
     * The user's finger was still moving when the lifted it from the screen
     *
     * The default implementation does kinetic scrolling on both X and Y. You
     * can reduce the computational load and thus slightly increase the frame
     * rate by overriding this if you are only interested in animation on one
     * axis.
     *
     * @param startX
     * @param startY
     * @param flickDirection
     * @param flickSpeed
     * @param flickSpeedX
     * @param flickSpeedY
     */
    public void gestureFlick(int startX,
                             int startY,
                             float flickDirection,
                             int flickSpeed,
                             int flickSpeedX,
                             int flickSpeedY)
    {
        yOffset = 0;
        animating = true;
        gestureHandler.kineticScroll(flickSpeed,
                                     GestureHandler.FRAME_ANIMATOR_FREE_ANGLE,
                                     friction,
                                     flickDirection);
    }

    // <- Gesture management methods
    //-------------------------------------------------------------------------

    /**
     * Draws a soft back key if hardware back key is not available.
     * @param graphics The Graphics instance.
     */
    protected void drawBackIcon(final Graphics graphics) {
        if (!PicasaViewer.HAS_ONE_KEY_BACK) {
            graphics.drawImage(
                    backIconImage, 
                    getWidth(), 
                    getHeight(), 
                    Graphics.BOTTOM | Graphics.RIGHT);
        }
    }
}
