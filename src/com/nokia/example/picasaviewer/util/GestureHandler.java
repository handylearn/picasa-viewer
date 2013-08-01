/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.util;

import com.nokia.mid.ui.frameanimator.FrameAnimator;
import com.nokia.mid.ui.frameanimator.FrameAnimatorListener;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureListener;
import com.nokia.mid.ui.gestures.GestureRegistrationManager;

import org.tantalum.util.L;

import com.nokia.example.picasaviewer.ui.GestureCanvas;

/**
 * Acts as a proxy between the GestureCanvas and the gesture framework of the
 * Nokia UI API.
 */
public final class GestureHandler
    implements FrameAnimatorListener,
               GestureListener
{
    // Constants
    public static final int FRAME_ANIMATOR_VERTICAL = FrameAnimator.FRAME_ANIMATOR_VERTICAL;
    public static final int FRAME_ANIMATOR_HORIZONTAL = FrameAnimator.FRAME_ANIMATOR_HORIZONTAL;
    public static final int FRAME_ANIMATOR_FREE_ANGLE = FrameAnimator.FRAME_ANIMATOR_FREE_ANGLE;
    public static final int FRAME_ANIMATOR_FRICTION_LOW = FrameAnimator.FRAME_ANIMATOR_FRICTION_LOW;
    public static final int FRAME_ANIMATOR_FRICTION_MEDIUM = FrameAnimator.FRAME_ANIMATOR_FRICTION_MEDIUM;
    public static final int FRAME_ANIMATOR_FRICTION_HIGH = FrameAnimator.FRAME_ANIMATOR_FRICTION_HIGH;

    // Members
    private final FrameAnimator animator = new FrameAnimator();
    private GestureCanvas canvas = null;
    private GestureInteractiveZone giz = null;

    /**
     * Constructor.
     */
    public GestureHandler() {
        try {
            giz = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_ALL);
        }
        catch (IllegalArgumentException e) {
            //#debug
            L.e(L.class.getName(), "Can not register GESTURE_ALL, backwards compatibility fallback", e);
            
            // GESTURE_ALL had a different value in SDK 1.1 and 1.0
            giz = new GestureInteractiveZone(63);
        }
    }

    /**
     * @see com.nokia.mid.ui.gestures.GestureListener#gestureAction(Object,
     * GestureInteractiveZone, GestureEvent)
     */
    public void gestureAction(final Object container,
                              final GestureInteractiveZone gestureInteractiveZone,
                              final GestureEvent gestureEvent)
    {
        switch (gestureEvent.getType()) {
            case GestureInteractiveZone.GESTURE_PINCH:
                canvas.gesturePinch(
                        gestureEvent.getPinchDistanceStarting(),
                        gestureEvent.getPinchDistanceCurrent(),
                        gestureEvent.getPinchDistanceChange(),
                        gestureEvent.getPinchCenterX(),
                        gestureEvent.getPinchCenterY(),
                        gestureEvent.getPinchCenterChangeX(),
                        gestureEvent.getPinchCenterChangeY());
                break;
            case GestureInteractiveZone.GESTURE_TAP:
                canvas.gestureTap(
                        gestureEvent.getStartX(), 
                        gestureEvent.getStartY());
                break;
            case GestureInteractiveZone.GESTURE_LONG_PRESS:
                canvas.gestureLongPress(
                        gestureEvent.getStartX(), 
                        gestureEvent.getStartY());
                break;
            case GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED:
                canvas.gestureLongPressRepeated(
                        gestureEvent.getStartX(), 
                        gestureEvent.getStartY());
                break;
            case GestureInteractiveZone.GESTURE_DRAG:
                canvas.gestureDrag(
                        gestureEvent.getStartX(),
                        gestureEvent.getStartY(),
                        gestureEvent.getDragDistanceX(),
                        gestureEvent.getDragDistanceY());
                break;
            case GestureInteractiveZone.GESTURE_DROP:
                canvas.gestureDrop(
                        gestureEvent.getStartX(),
                        gestureEvent.getStartY(),
                        gestureEvent.getDragDistanceX(),
                        gestureEvent.getDragDistanceY());
                break;
            case GestureInteractiveZone.GESTURE_FLICK:
                canvas.gestureFlick(
                        gestureEvent.getStartX(),
                        canvas.getScrollY(),
                        gestureEvent.getFlickDirection(),
                        gestureEvent.getFlickSpeed(),
                        gestureEvent.getFlickSpeedX(),
                        gestureEvent.getFlickSpeedY());
                break;
            default:
                break;
        }
    }

    /**
     * @see com.nokia.mid.ui.frameanimator.FrameAnimatorListener#animate(
     * FrameAnimator, int, int, short, short, short, boolean)
     */
    public void animate(final FrameAnimator animator,
                        final int x,
                        final int y,
                        final short delta,
                        final short deltaX,
                        final short deltaY,
                        final boolean lastFrame)
    {
        //#debug
        L.i(L.class.getName(), "animate, y=" + y + " deltaY=" + deltaY);
        
        canvas.animate(y, deltaY, lastFrame);
    }

    /**
     * Sets the gesture canvas for this handler.
     * @param canvas The gesture canvas associated with this handler.
     */
    public void setCanvas(final GestureCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Stops the animation.
     */
    public void stopAnimator() {
        System.out.println("GestureHandler.stopAnimator()");
        animator.stop();
    }

    /**
     * Animates drag.
     * @param x The new X coordinate.
     * @param y The new Y coordinate.
     */
    public void animateDrag(final int x, final int y)
        throws IllegalArgumentException
    {
        //#debug
        L.i(L.class.getName(), "animate drag, y=" + y + ", x=" + x);
        animator.drag(x, y);
    }

    /**
     * Registers for gesture and animation events when the parent becomes
     * visible.
     */
    public void register(final int x, final int y) {
        //#debug
        L.i("Canvas " + canvas.getTitle(), "register");
        
        onCanvasSizeChanged();
        GestureRegistrationManager.register(canvas, giz);
        GestureRegistrationManager.setListener(canvas, this);
        animator.register(x, y, (short) 0, (short) 0, this);
    }

    /**
     * Initiates a kintetic scroll.
     * 
     * @see com.nokia.mid.ui.frameanimator.FrameAnimator#kineticScroll(int, int, int, float)
     */
    public void kineticScroll(final int startSpeed,
                              final int direction,
                              final int friction,
                              final float angle)
    {
        //#debug
        L.i(L.class.getName(), "animate start kinetic scroll, startSpeed=" + startSpeed);
        
        animator.kineticScroll(startSpeed, direction, friction, angle);
    }

    /**
     * Updates the rectangle bouding the gesture area based on the new size of
     * the canvas.
     */
    public void onCanvasSizeChanged() {
        
        //#debug
        L.i(L.class.getName(), 
            "onCanvasSizeChanged(): " + canvas.getWidth() +
            "x" + canvas.getHeight());
        
        giz.setRectangle(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
