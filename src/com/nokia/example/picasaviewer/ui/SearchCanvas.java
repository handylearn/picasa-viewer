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

import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import com.nokia.example.picasaviewer.PicasaViewer;
import com.nokia.example.picasaviewer.common.PicasaImageObject;
import com.nokia.example.picasaviewer.common.PicasaStorage;
import com.nokia.example.picasaviewer.util.ViewManager;

/**
 * Class for displaying the search bar and search results.
 */
public final class SearchCanvas 
        extends ImageGridCanvas
        implements SearchBar.Listener
{
    private static final int MARGIN = 5;
    private Command featuredCommand = null;
    private Command deleteCommand = null;
    private Command exitCommand = null;
    private SearchBar searchBar = null;
    private CustomCategoryBar customCategoryBar = null;

    /**
     * Constructor.
     * @param viewManager
     */
    public SearchCanvas(ViewManager viewManager) {
        super(viewManager);
        
        statusBarVisible = false;
        
        if (viewManager.supportsCategoryBar()) {
            if (viewManager.getCategoryBarHandler().hasCustomCategoryBar()) {
                customCategoryBar =
                    (CustomCategoryBar)viewManager.getCategoryBarHandler().getCategoryBar();
            }
            
            this.setFullScreenMode(true);
        }
        else {
            // Category bar is not supported. For backwards compatibility.
            featuredCommand = new Command("Featured", Command.SCREEN, 0);
            addCommand(featuredCommand);
        }
        
        if (PicasaViewer.HAS_ONE_KEY_BACK) {
            exitCommand = new Command("Exit", Command.EXIT, 0);
            addCommand(exitCommand);
        }
        
        searchBar = new SearchBar(this, this, getWidth() - MARGIN * 2, MARGIN, MARGIN);
        searchBar.setVisible(true);
        
        headerHeight = searchBar.getHeight();
        
        final String keyboard = System.getProperty("com.nokia.keyboard.type");
        
        if ("PhoneKeypad".equals(keyboard)) {
            deleteCommand = new Command("Delete", Command.CANCEL, 0);
            addCommand(deleteCommand);
        }
    }

    /**
     * @see javax.microedition.lcdui.CommandListener#commandAction(
     * javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
     */
    public void commandAction(Command command, Displayable d) {
        //#debug
        L.i("SearchCanvas Command action", "Command " + command);
        
        if (command == featuredCommand) {
            viewManager.showView(ViewManager.FEATURED_VIEW_INDEX);
        }
        else if (command.getCommandType() == Command.CANCEL) {
            L.i("Cancel command", "");
            searchBar.eraseLastCharacter();
        }
        else if (command == exitCommand) {
            viewManager.goBackFromTabbedView();
        }
    }

    /**
     * @see javax.microedition.lcdui.Canvas#showNotify()
     */
    public void showNotify() {
        if ((!viewManager.supportsCategoryBar() || imageObjectModel.isEmpty())
            && searchBar.getSearchTerm().length() == 0)
        {
            /*
             * Throw an event forward on the UI thread
             * 
             * SDK 1.0 and 1.1 phones don't use an on-screen keyboard, so enter
             * edit mode right away so the user can just start typing
             */
            PlatformUtils.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    searchBar.setFocused(true);
                }
            });
        }
        else if (viewManager.supportsCategoryBar()) {
            viewManager.getCategoryBarHandler().setVisibility(true);
            
            if (viewManager.getCategoryBarHandler().hasCustomCategoryBar()) {
                repaint();
            }
        }
        
        super.showNotify();
    }

    /**
     * @see GestureCanvas#hideNotify()
     */
    public void hideNotify() {
        disableKeyboard(true);
        super.hideNotify();
    }

    /**
     * @see javax.microedition.lcdui.Canvas#paint(javax.microedition.lcdui.Graphics)
     */
    public void paint(final Graphics graphics) {
        checkThatScrollDoesNotExceedBoundaries();
        
        drawGrid(graphics, headerHeight);
        
        graphics.setColor(0x000000);
        graphics.fillRect(0, 0, getWidth(), searchBar.getHeight() + MARGIN * 2);
        
        if (customCategoryBar != null) {
            customCategoryBar.paint(graphics, getHeight() - CustomCategoryBar.HEIGHT);
        }
    }

    /**
     * @see ImageGridCanvas#gestureTap(int, int)
     */
    public boolean gestureTap(int startX, int startY) {
        if (!super.gestureTap(startX, startY)) {
            if (viewManager.supportsCategoryBar()
                && viewManager.getCategoryBarHandler().hasCustomCategoryBar()
                && startY > getHeight() - CustomCategoryBar.HEIGHT)
            {
                // The tap was on top of category bar. Don't propagate this.
                return true;
            }
            
            final int index = getItemIndex(startX, startY);
            
            if (index >= 0) {
                // An image item was tapped
                
                if (searchBar.getHasFocus()) {
                    // Hide the keyboard
                    PlatformUtils.getInstance().runOnUiThread(new Runnable() {
                        public void run() {
                            disableKeyboard(true);
                        }
                    });
                }
                else {
                    // Keyboard is not active
                    if (imageObjectModel.size() > index) {
                        PicasaStorage.setSelectedImage(
                                (PicasaImageObject) imageObjectModel.elementAt(index));
                        
                        viewManager.showView(ViewManager.DETAILS_VIEW_INDEX);
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }

    /**
     * Starts the search.
     * @return The search task.
     */
    private Task startSearch() {
        disableKeyboard(false);
        scrollY = 0;
        return loadFeed(searchBar.getSearchTerm(), StaticWebCache.GET_WEB);
    }

    /**
     * Disables the keyboard.
     * @param force True, if the keyboard must be disabled for certain-
     * @return True if the keyboard was disabled.
     */
    private boolean disableKeyboard(final boolean force) {
        if (!force && !viewManager.supportsCategoryBar()) {
            return false;
        }
        
        searchBar.setFocused(false);
        removeCommand(deleteCommand);
        deleteCommand = null;
        
        return true;
    }


    /**
     * @see SearchBar.Listener#onSearchBarFocusedChanged(boolean)
     */
    public void onSearchBarFocusedChanged(boolean focused) {
        System.out.println("SearchCanvas.onSearchBarFocusedChanged(): " + focused);
    }

    /**
     * @see SearchBar.Listener#onSearchTermChanged(java.lang.String)
     */
    public void onSearchTermChanged(String searchTerm) {
        System.out.println("SearchCanvas.onSearchTermChanged(): \"" + searchTerm + "\"");
        refresh(searchTerm, StaticWebCache.GET_LOCAL);
    }

    /**
     * @see com.nokia.example.picasaviewer.ui.SearchBar.Listener#onExecuteSearchRequest()
     */
    public void onExecuteSearchRequest() {
        System.out.println("SearchCanvas.onExecuteSearchRequest()");
        startSearch();
    }


    // Methods for forwarding touch events to custom category bar and search bar ->

    protected void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);
        
        if (customCategoryBar != null) {
            customCategoryBar.onPointerPressed(x, y);
            repaint();
        }
        
        searchBar.onPointerPressed(x, y);
    }

    protected void pointerDragged(int x, int y) {
        if (customCategoryBar != null) {
            customCategoryBar.onPointerDragged(x, y);
            repaint();
        }
        
        searchBar.onPointerDragged(x, y);
    }

    protected void pointerReleased(int x, int y) {
        if (customCategoryBar != null) {
            customCategoryBar.onPointerReleased(x, y);
            repaint();
        }
        
        searchBar.onPointerReleased(x, y);
    }

    // <- Methods for forwarding touch events to custom category bar and search bar
}
