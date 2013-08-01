/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import com.nokia.mid.ui.LCDUIUtil;

import com.nokia.example.picasaviewer.PicasaViewer;
import com.nokia.example.picasaviewer.common.PicasaStorage;
import com.nokia.example.picasaviewer.util.ViewManager;

/**
 * Class for displaying the featured images at Picasa web albums. Relies on the
 * ImageGridCanvas class.
 */
public final class FeaturedCanvas 
        extends ImageGridCanvas
{
    private static final float CATEGORY_BAR_OPACITY = 0.7f;
    private CustomCategoryBar customCategoryBar = null;
    private Command searchCommand = null;
    private Command exitCommand = null;
    private Command refreshCommand = null;

    /**
     * Constructor.
     * @param viewManager 
     */
    public FeaturedCanvas(ViewManager viewManager) {
        super(viewManager);
        
        if (viewManager.supportsCategoryBar()) {
            try {
                com.nokia.mid.ui.VirtualKeyboard.hideOpenKeypadCommand(true);
                
                if (viewManager.getCategoryBarHandler().hasCustomCategoryBar()) {
                    customCategoryBar =
                        (CustomCategoryBar)viewManager.getCategoryBarHandler().getCategoryBar();
                    customCategoryBar.setOpacity(CATEGORY_BAR_OPACITY);
                    
                    /* We need to turn on the full screen mode. Otherwise our
                     * custom category bar will appear on top of the other bar
                     * with Refresh command button on it. Luckily, we can
                     * restore the status bar by explicitly setting the object
                     * trait.
                     */
                    setFullScreenMode(true);
                    LCDUIUtil.setObjectTrait(this, "nokia.ui.canvas.status_zone", Boolean.TRUE);
                }
            }
            catch (Exception e) {
                //#debug
                L.e("Can not initialize", "Update icon image", e);
            }
        }
        else {
            // Create commands that would have otherwise been in the category
            // bar
            searchCommand = new Command("Search", Command.SCREEN, 1);
            exitCommand = new Command("Exit", Command.EXIT, 0);
            
            addCommand(searchCommand);
            addCommand(exitCommand);
        }
        
        // Refresh is initialized whether there is a category bar or not
        refreshCommand = new Command("Refresh", Command.OK, 0);
        addCommand(refreshCommand);
        
        if (PicasaViewer.HAS_ONE_KEY_BACK) {
            exitCommand = new Command("Exit", Command.EXIT, 0);
            addCommand(exitCommand);
        }
        
        try {
            PicasaStorage.init(getWidth());
            loadFeed(null, StaticWebCache.GET_ANYWHERE).join(200);
        }
        catch (Exception ex) {
            //#debug
            L.e("Slow initial feed load", null, ex);
        }
    }

    /**
     * Displays the category bar.
     * @see javax.microedition.lcdui.Canvas#showNotify()
     */
    public void showNotify() {
        if (viewManager.supportsCategoryBar()) {
            viewManager.getCategoryBarHandler().setVisibility(true);
            
            if (customCategoryBar != null) {
                customCategoryBar.setOpacity(CATEGORY_BAR_OPACITY);
                repaint();
            }
        }
    }

    /**
     * @see javax.microedition.lcdui.CommandListener#commandAction(
     * javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command == refreshCommand) {
            refresh(null, StaticWebCache.GET_WEB);
        }
        else if (command == searchCommand) {
            viewManager.showView(ViewManager.SEARCH_VIEW_INDEX);
        }
        else if (command == exitCommand) {
            viewManager.goBackFromTabbedView();
        }
    }

    /**
     * @see javax.microedition.lcdui.Canvas#paint(Graphics)
     */
    public void paint(final Graphics graphics) {
        checkThatScrollDoesNotExceedBoundaries();
        drawGrid(graphics, 0);
        
        if (customCategoryBar != null) {
            customCategoryBar.paint(graphics, getHeight() - CustomCategoryBar.HEIGHT);
        }
    }

    /**
     * Reloads the content on pinch.
     * @see GestureCanvas#gesturePinch(int, int, int, int, int, int, int)
     */
    public void gesturePinch(int pinchDistanceStarting,
                             int pinchDistanceCurrent,
                             int pinchDistanceChange,
                             int centerX,
                             int centerY,
                             int centerChangeX,
                             int centerChangeY)
    {
        refresh(null, StaticWebCache.GET_WEB);
    }


    // Methods for forwarding touch events to custom category bar ->

    protected void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);
        
        if (customCategoryBar != null) {
            customCategoryBar.onPointerPressed(x, y);
            repaint();
        }
    }

    protected void pointerDragged(int x, int y) {
        if (customCategoryBar != null) {
            customCategoryBar.onPointerDragged(x, y);
            repaint();
        }
    }

    protected void pointerReleased(int x, int y) {
        if (customCategoryBar != null) {
            customCategoryBar.onPointerReleased(x, y);
            repaint();
        }
    }

    // <- Methods for forwarding touch events to custom category bar
}
