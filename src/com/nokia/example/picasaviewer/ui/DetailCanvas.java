/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.ui;

import com.nokia.example.picasaviewer.PicasaViewer;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import com.nokia.example.picasaviewer.common.PicasaImageObject;
import com.nokia.example.picasaviewer.common.PicasaStorage;
import com.nokia.example.picasaviewer.util.ViewManager;

/**
 * The class displaying the larger image, title and photographer.
 */
public final class DetailCanvas 
    extends GestureCanvas
{
    // Constants
    private static final int PADDING = 5;
    private static final double R = 12;

    // Members
    private final Command backCommand = new Command("Back", Command.BACK, 0);
    private volatile Image image = null;
    private Vector titleLines = null;
    private final int width;
    private int fontHeight = 0;

    /**
     * Constructor.
     * @param viewManager
     */
    public DetailCanvas(ViewManager viewManager) {
        super(viewManager);
        
        yC = 50.0;
        width = getWidth();
        titleLines = new Vector();
        fontHeight = Font.getDefaultFont().getHeight();
        
        setFullScreenMode(true);
        
        // This view always has a back command as category bar is not shown
        addCommand(backCommand);
    }

    /**
     * @see javax.microedition.lcdui.CommandListener#commandAction(
     * javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command.getCommandType() == Command.BACK) {
            viewManager.goBack();
        }
    }

    /**
     * Resets the scroll position and hides category bar when the view is shown.
     * @see javax.microedition.lcdui.Canvas#showNotify()
     */
    public void showNotify() {
        scrollY = 0;
        
        if (viewManager.supportsCategoryBar()) {
            viewManager.getCategoryBarHandler().setVisibility(false);
        }
        
        top = -calculateHeight() + getHeight();
    }

    /**
     * @see GestureCanvas#hideNotify()
     */
    public void hideNotify() {
        image = null;
        stopSpinner();
        super.hideNotify();
        titleLines.removeAllElements();
    }

    /**
     * @see GestureCanvas#sizeChanged(int, int)
     */
    public void sizeChanged(int w, int h) {
        super.sizeChanged(w, h);
    }

    /**
     * @see javax.microedition.lcdui.Canvas#paint(javax.microedition.lcdui.Graphics)
     */
    public void paint(final Graphics graphics) {
        checkThatScrollDoesNotExceedBoundaries();
        
        final PicasaImageObject selectedImage = PicasaStorage.getSelectedImage();
        
        if (titleLines.isEmpty()) {
            splitToLines(titleLines, selectedImage.title,
                Font.getDefaultFont(), width - 2 * PADDING);
        }
        
        //#debug
        L.i("Paint DetailCanvas", selectedImage.imageUrl);
        
        graphics.setColor(0x000000);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(0xffffff);
        
        boolean startingSpin = false;
        
        // If we do not have the image and we are not loading it, start loading it
        if (image == null && !isSpinning()) {
            startSpinner();
            startingSpin = true;
            
            try {
                PicasaStorage.imageCache.getAsync(
                    selectedImage.imageUrl,
                    Task.HIGH_PRIORITY,
                    StaticWebCache.GET_ANYWHERE,
                    new Task() {
                        public Object exec(final Object in) {
                            if (in != null
                                && selectedImage == PicasaStorage.getSelectedImage())
                            {
                                image = (Image) in;
                                stopSpinner();
                                top = -calculateHeight() + getHeight();
                            }
                            
                            return in;
                        }
                    }).join(100);
            }
            catch (TimeoutException ex) {
                // Normal for slow load
            }
            catch (Exception ex) {
                //#debug
                L.e("Can not join image load", selectedImage.imageUrl, ex);
            }
        }
        
        if (isSpinning()) {
            if (!startingSpin) {
                drawSpinner(graphics);
            }
        }
        else if (image != null) {
            // Done, draw image
            graphics.drawImage(image, getWidth() / 2, scrollY,
                Graphics.TOP | Graphics.HCENTER);
        }
        
        int textY = (image == null) ? 
                (int) yC + ((int) R) << 1 : 
                image.getHeight() + scrollY;
        
        graphics.setColor(0xFFFFFF);
        
        // Paint the description text
        for (int i = 0; i < titleLines.size(); i++) {
            graphics.drawString((String) titleLines.elementAt(i),
                PADDING, textY,
                Graphics.LEFT | Graphics.TOP);
            textY += fontHeight;
        }
        
        // Paint the name of the author
        graphics.drawString(selectedImage.author, PADDING, textY,
            Graphics.LEFT | Graphics.TOP);
        
        drawBackIcon(graphics);
    }

    /**
     * @see GestureCanvas#gestureTap(int, int)
     */
    public boolean gestureTap(int startX, int startY) {
        boolean tapWasHandledAndConsumed = false;
        
        if (!PicasaViewer.HAS_ONE_KEY_BACK) {
            viewManager.goBack();
            tapWasHandledAndConsumed = true;
        }
        
        return tapWasHandledAndConsumed;
    }

    /**
     * Calculates the height of the view for the scrolling boundary.
     * @return The height of the view.
     */
    private int calculateHeight() {
        int height = image == null ? 0 : image.getHeight();
        height += titleLines == null ? 0 : titleLines.size() * fontHeight;
        height += fontHeight; // Author
        height += PADDING * 2;
        
        if (height < getHeight()) {
            height = getHeight();
        }
        
        System.out.println("DetailsCanvas.calculateHeight(): " + height);
        return height;
    }

    /**
     * Split a string in to several lines of text which will display within a
     * maximum width.
     * 
     * @param vector
     * @param text
     * @param font
     * @param maxWidth 
     */
    private void splitToLines(
            final Vector vector,
            final String text,
            final Font font,
            final int maxWidth)
    {
        int lastSpace = 0;
        
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                lastSpace = i;
            }
            
            final int len = font.stringWidth(text.substring(0, i));
            
            if (len > maxWidth) {
                vector.addElement(text.substring(0, lastSpace + 1).trim());
                splitToLines(vector,
                             text.substring(lastSpace + 1),
                             font,
                             maxWidth);
                return;
            }
        }
        
        vector.addElement(text.trim());
    } 
}
