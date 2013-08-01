/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.ui;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.L;

import com.nokia.example.picasaviewer.common.PicasaImageObject;
import com.nokia.example.picasaviewer.common.PicasaStorage;
import com.nokia.example.picasaviewer.util.ViewManager;


/**
 * Class responsible for drawing a grid of images fetched from Picasa web
 * albums.
 */
public abstract class ImageGridCanvas 
        extends GestureCanvas
{
    protected final Hashtable images = new Hashtable();
    protected final Vector imageObjectModel = new Vector(); // Access only from UI thread
    protected final ViewManager viewManager;
    protected final int imageSide;
    protected int headerHeight = 0;
    protected boolean statusBarVisible = true;

    /**
     * Constructor.
     * @param viewManager
     */
    public ImageGridCanvas(ViewManager viewManager) {
        super(viewManager);
        this.viewManager = viewManager;
        imageSide = getWidth() / 2;
        headerHeight = 0;
    }

    /**
     * @see com.nokia.example.picasaviewer.ui.GestureCanvas#gestureTap(int, int)
     */
    public boolean gestureTap(int startX, int startY) {
        boolean handled = false;
        
        if (!super.gestureTap(startX, startY)) {
            final int index = getItemIndex(startX, startY);
            
            if (index >= 0 && index < imageObjectModel.size()) {
                PicasaImageObject picasaImageObject =
                        (PicasaImageObject) imageObjectModel.elementAt(index);
                PicasaStorage.setSelectedImage(picasaImageObject);
                
                //#debug
                L.i("select image", 
                        PicasaStorage.getSelectedImage().toString());
                
                viewManager.showView(ViewManager.DETAILS_VIEW_INDEX);
                handled = true;
            }
        }
        
        return handled;
    }

    /**
     * 
     * @param url
     * @param getType
     */
    public void refresh(final String url, final int getType) {
        top = -getHeight();
        repaint();
        loadFeed(url, getType);
    }

    /**
     * @param search
     * @param getType
     * @return
     */
    public Task loadFeed(final String search, final int getType) {
        //#debug
        L.i("loadFeed", search);

        final Task task = new LoadFeedTask(search, getType);
        
        PicasaStorage.getImageObjects(
                search, 
                Task.HIGH_PRIORITY, 
                getType, 
                task);
        
        if (getType != StaticWebCache.GET_LOCAL) {
            startSpinner();
        }
        
        return task;
    }

    /**
     * Draw images, starting at the specified Y
     *
     * @param startY
     */
    public void drawGrid(final Graphics g, final int startY) {
        g.setColor(0x000000);
        g.fillRect(0, startY, getWidth(), getHeight() - startY);
        
        for (int i = 0; i < imageObjectModel.size(); i++) {
            int xPosition = i % 2 * (getWidth() / 2);
            int yPosition = startY + scrollY + ((i - i % 2) / 2) * imageSide;
            
            if (yPosition > getHeight()) {
                break;
            }
            
            // If image is in RAM
            if (images.containsKey(imageObjectModel.elementAt(i))) {
                g.drawImage(
                        (Image) (images.get(imageObjectModel.elementAt(i))), 
                        xPosition, 
                        yPosition, 
                        Graphics.LEFT | Graphics.TOP);
            }
            else {
                // If there were no results
                PicasaImageObject picasaImageObject =
                        (PicasaImageObject) imageObjectModel.elementAt(i);
                
                if (picasaImageObject.thumbUrl.length() == 0) {
                    g.setColor(0xFFFFFF);
                    g.drawString(
                            "No Result.", 
                            0, 
                            headerHeight, 
                            Graphics.TOP | Graphics.LEFT);
                    
                }
                else {
                    // Start loading the image, draw a placeholder
                    PicasaStorage.imageCache.getAsync(
                            picasaImageObject.thumbUrl,
                            Task.NORMAL_PRIORITY, 
                            StaticWebCache.GET_ANYWHERE, 
                            new ImageResult(picasaImageObject));
                    
                    g.setColor(0x111111);
                    g.fillRect(xPosition, yPosition, imageSide, imageSide);
                }
            }
        }
        
        drawSpinner(g);
    }

    /**
     * Return the image index based on the X and Y coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    protected int getItemIndex(int x, int y) {
        if (y > headerHeight) {
            int row = (-scrollY + y - headerHeight) / imageSide;
            int column = x < getWidth() / 2 ? 0 : 1;
            return (row * 2 + column);
        }
        return -1;
    }

    /**
     * Object for adding images to the hash map when they're loaded.
     */
    protected final class ImageResult extends Task {
        private final Object key;
        
        public ImageResult(Object key) {
            System.out.println("ImageResult::ImageResult(): " + key.toString());
            this.key = key;
        }
        
        public Object exec(final Object in) {
            System.out.println("ImageResult::exec()");
            
            if (in != null) {
                images.put(key, in);
                repaint();
            }
            
            return in;
        }
    }

    /**
     * Loads the feed.
     */
    private class LoadFeedTask extends Task {
        private String search;
        private int getType;
        
        public LoadFeedTask(String search, int getType) {
            this.search = search;
            this.getType = getType; 
        }
        
        protected Object exec(Object in) {
            
            if (in != null) {
                //#debug
                L.i("Load feed success, type=" + getType, search);
                
                scrollY = 0;
                imageObjectModel.removeAllElements();
                images.clear();
                
                L.i("Vector in", in.toString());
                
                final Vector newModel = (Vector) in;
                
                for (int i = 0; i < newModel.size(); i++) {
                    imageObjectModel.addElement(newModel.elementAt(i));
                    String thumbUrl = 
                            ((PicasaImageObject) imageObjectModel.elementAt(i))
                            .thumbUrl;
                    try {
                        PicasaStorage.imageCache.prefetch(thumbUrl);
                    }
                    catch (FlashDatabaseException e) {
                        L.e("Could not fetch image", thumbUrl, e);
                    }
                }
                
                top = -((imageObjectModel.size() * imageSide) / 2 - 
                        getHeight() / 2) + 
                        imageSide - 20;
                
                stopSpinner();
            }
            else {
                L.i("Variable in is null", "");
            }
            
            return in;
        }
        
        public void onCanceled() {
            //#debug
            L.i("Load feed canceled, type=" + getType, search);
            
            if (getType == StaticWebCache.GET_LOCAL) {
                imageObjectModel.removeAllElements();
                images.clear();
                top = -getHeight();
            }
            
            stopSpinner();
        }
    }
}
