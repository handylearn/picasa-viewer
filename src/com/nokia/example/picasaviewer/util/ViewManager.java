/**
 * Copyright (c) 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.util;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.tantalum.util.L;

import com.nokia.example.picasaviewer.PicasaViewer;
import com.nokia.example.picasaviewer.ui.DetailCanvas;
import com.nokia.example.picasaviewer.ui.FeaturedCanvas;
import com.nokia.example.picasaviewer.ui.SearchCanvas;

/**
 * Responsible for switching between views and handling showing and hiding of
 * the category bar.
 */
public class ViewManager {
    public static final int FEATURED_VIEW_INDEX = 0;
    public static final int SEARCH_VIEW_INDEX = 1;
    public static final int DETAILS_VIEW_INDEX = 2;
    public static final int INITIAL_VIEW_INDEX = FEATURED_VIEW_INDEX;
    
    private static final int NO_VIEW = -1;
    private static final int AMOUNT_OF_VIEWS = 3;
    
    private final PicasaViewer picasaViewer;
    private final Display display;
    private Displayable[] displayables = new Displayable[AMOUNT_OF_VIEWS];
    private CategoryBarHandler categoryBarHandler;
    private boolean categoryBarSupported = false;
    private int currentViewIndex = NO_VIEW;
    private int previousViewIndex = NO_VIEW;

    /**
     * Constructor. Instantiates views that are used in the application.
     * 
     * The constructor initializes all views the application needs. 
     * These views are reused. This kind of behavior frees from instantiating 
     * the views again.
     * 
     * Shuts down the application if views cannot be instantiated.
     * @param picasaViewer 
     */
    public ViewManager(PicasaViewer picasaViewer) {
        this.picasaViewer = picasaViewer;
        this.display = Display.getDisplay(picasaViewer);
        this.categoryBarSupported = createCategoryBar();
        
        try {
            // Initialize views
            displayables[FEATURED_VIEW_INDEX] = new FeaturedCanvas(this);
            displayables[SEARCH_VIEW_INDEX] = new SearchCanvas(this);
            displayables[DETAILS_VIEW_INDEX] = new DetailCanvas(this);
        }
        catch(Exception e) {
            L.e("Could not initialize views.", "Shutting down.", e);
            picasaViewer.notifyDestroyed();
        }
    }

    /**
     * Switches to view defined by index. Index must be one of pre defined
     * indices listed in ViewManager's static final fields.
     * @param index 
     */
    public void showView(int index) {
        /*
         * As this application has only one view that implements back key
         * functionality, we implement simple back stepping by just saving the
         * previous view in a variable. In applications with multiple levels
         * of views, the opened views can be stored in Stack for back stepping.
         */
        previousViewIndex = currentViewIndex;
        setCurrent(displayables[index]);
        currentViewIndex = index;
    }

    /**
     * Makes a rough assumption that in the application when stepping back
     * from the main view (that has a category bar) we always shut down
     * the application as the main view is the first view. Thus there
     * is nothing where to go back.
     */
    public void goBackFromTabbedView() {
        picasaViewer.notifyDestroyed();
    }

    /**
     * Show the view which was shown before the currently shown view.
     */
    public void goBack() {
        // Check if previous contains a valid index.
        if (previousViewIndex > ViewManager.NO_VIEW) {
            display.setCurrent(displayables[previousViewIndex]);
            currentViewIndex = previousViewIndex;
            previousViewIndex = ViewManager.NO_VIEW;
        } else {
            // No previous view. This indicates that we can shut down.
            picasaViewer.notifyDestroyed();
        }
    }

    /**
     * Convenience method for displaying the initial view.
     */
    public void showInitialView() {
        showView(ViewManager.INITIAL_VIEW_INDEX);
    }

    /**
     * @return The Displayables owned by this manager.
     */
    public Displayable[] getDisplayables() {
        return displayables;
    }

    /**
     * @return The width of the current view.
     */
    public int getWidth() {
        int width = 0;
        Displayable displayable = display.getCurrent();
        
        if(displayable != null) {
            width = displayable.getWidth();
        }
        else {
            L.i("Could not get width from current Display", "");
            picasaViewer.notifyDestroyed();
        }
        
        return width;
    }

    /**
     * A convenience method for getting the display of the current MIDlet.
     * @return 
     */
    public Display getDisplay() {
        return Display.getDisplay(picasaViewer);
    }

    /**
     * Shows the given displayable.
     * @param displayable A Displayable instance.
     */
    private void setCurrent(final Displayable displayable) {
        display.setCurrent(displayable);
    }    

    /**
     * @return True if the category bar is supported, false otherwise.
     */
    public boolean supportsCategoryBar() {
        return categoryBarSupported;
    }

    /** 
     * @return The category bar handler instance or null if not available.
     */
    public CategoryBarHandler getCategoryBarHandler() {
        return categoryBarHandler;
    }

    /**
     * Creates a category bar if platform supports it.
     * @return True if the category bar was successfully created, false
     * otherwise.
     */
    private boolean createCategoryBar() {
        boolean categoryBarCreated = false;
        
        try {
            // Check if CategoryBarHandler can be instantiated
            categoryBarHandler = (CategoryBarHandler) Class.forName(
                    "com.nokia.example.picasaviewer.util.CategoryBarHandler")
                    .newInstance();
            
            /*
             * Pass ViewManager as parameter to allow category bar to switch
             * views on its own.
             */
            categoryBarHandler.initialize(this);
            categoryBarCreated = true;
        }
        catch (Throwable t) {
            //#debug
            L.i("Can not set category bar handler", "normal before SDK 2.0");
            // As we ended here, the fallbacks need to be used.
        }
        
        return categoryBarCreated;
    }
}
