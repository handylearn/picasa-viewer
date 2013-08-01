/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.util;

import java.io.IOException;

import javax.microedition.lcdui.Image;

import org.tantalum.util.L;

import com.nokia.mid.ui.CategoryBar;
import com.nokia.mid.ui.ElementListener;
import com.nokia.mid.ui.IconCommand;

import com.nokia.example.picasaviewer.PicasaViewer;
import com.nokia.example.picasaviewer.ui.CustomCategoryBar;

/**
 * Handles category bar's commands, visibility and instantiation.
 */
public class CategoryBarHandler
    implements ElementListener
{
    // Constants
    private static final String HOME_ICON_URI = "/home.png";
    private static final String SEARCH_ICON_URI = "/search.png";

    // Members
    private static CategoryBar categoryBar = null;
    private ViewManager viewManager = null;
    private IconCommand featuredIconCommand = null;
    private IconCommand searchIconCommand = null;
    private boolean categoryBarIsCustom = false;

    /**
     * Constructor.
     */
    public CategoryBarHandler() {
    }

    /**
     * Initializes the category bar.
     * @param viewManager 
     */
    public void initialize(ViewManager viewManager) {
        this.viewManager = viewManager;
        
        if (PicasaViewer.HAS_ONE_KEY_BACK) {
            System.out.println("CategoryBarHandler.initialize(): Creating a custom category bar.");
            Image[] unselectedIcons = new Image[2];
            
            try {
                unselectedIcons[0] = Image.createImage(HOME_ICON_URI);
                unselectedIcons[1] = Image.createImage(SEARCH_ICON_URI);
            }
            catch (IOException e) {
            }
            
            categoryBar = CustomCategoryBar.getNewInstance(null, unselectedIcons);
            categoryBarIsCustom = true;
        }
        else {
            System.out.println("CategoryBarHandler.initialize(): Creating a standard category bar.");
            featuredIconCommand = createIconCommand(HOME_ICON_URI, "Home", IconCommand.ICON_OK);
            searchIconCommand = createIconCommand(SEARCH_ICON_URI, "Search", IconCommand.ICON_OK);
            
            final IconCommand[] iconCommands = 
                {featuredIconCommand, searchIconCommand};
            
            categoryBar = new CategoryBar(iconCommands, true);
        }
        
        categoryBar.setVisibility(true);
        categoryBar.setElementListener(this);
    }

    /**
     * @see com.nokia.mid.ui.ElementListener#notifyElementSelected(CategoryBar, int)
     */
    public void notifyElementSelected(CategoryBar categoryBar, int index) {
        System.out.println("CategoryBarHandler.notifyElementSelected(): " + index);
        
        switch (index) {
            case ElementListener.BACK:
                viewManager.goBackFromTabbedView();
                break;
            case 0:
                categoryBar.setSelectedIndex(index);
                viewManager.showView(ViewManager.FEATURED_VIEW_INDEX);
                break;
            case 1:
                categoryBar.setSelectedIndex(index);
                viewManager.showView(ViewManager.SEARCH_VIEW_INDEX);
                break;
            default:
                break;
        }
    }

    /**
     * Sets category bar visibility.
     * @param visibility
     */
    public void setVisibility(boolean visibility) {
        categoryBar.setVisibility(visibility);
    }

    /** 
     * @return The category bar instance.
     */
    public CategoryBar getCategoryBar() {
        return categoryBar;
    }

    /** 
     * @return True if the category bar is custom.
     */
    public boolean hasCustomCategoryBar() {
        return categoryBarIsCustom;
    }

    /**
     * Creates an IconCommand with the given details.
     * @param pathToImage Path to the image file to use with the command.
     * @param name Name for the command.
     * @return
     */
    private IconCommand createIconCommand(
            String pathToImage, 
            String name, 
            int commandType) {
        IconCommand iconCommand;
        
        try {
            Image unselectedIcon = Image.createImage(pathToImage);
            Image selectedIcon = ImageUtils.drawMaskedImage(
                    unselectedIcon, 
                    viewManager.getDisplay());
            
            iconCommand = new IconCommand(
                    name,
                    name,
                    unselectedIcon,
                    selectedIcon,
                    IconCommand.SCREEN,
                    1);
        } catch (IOException e) {
            L.e("Could not create image using path", pathToImage, e);
            iconCommand = new IconCommand(
                    name,
                    name,
                    IconCommand.SCREEN, 
                    1, 
                    commandType);
        }
        
        return iconCommand;
    }
}
