/**
 * Copyright (c) 2013 Nokia Corporation.
 */

package com.nokia.example.picasaviewer.ui;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

import com.nokia.mid.ui.CanvasGraphicsItem;
import com.nokia.mid.ui.KeyboardVisibilityListener;
import com.nokia.mid.ui.TextEditor;
import com.nokia.mid.ui.TextEditorListener;
import com.nokia.mid.ui.VirtualKeyboard;

/**
 * A search bar component with close to native look and feel.
 */
public class SearchBar
    extends CanvasGraphicsItem
    implements KeyboardVisibilityListener,
               TextEditorListener
{
    // Constants
    public static final int UNDEFINED = -1;
    
    private static final String SEARCH_ICON_URI = "/search-bar-search-icon.png";
    private static final String DEFAULT_HINT_TEXT = "Search";
 
    private static final int BACKGROUND_COLOR = 0x00000000;
    private static final int TEXT_COLOR = 0xf4f4f4;
    private static final int HINT_TEXT_COLOR = 0x717171;
    private static final int TEXT_LINE_COLOR = 0xc8c8c8;
    private static final int HIGHLIGHT_COLOR = 0x29a7cc;
    
    private static final int TEXT_FIELD_MAX_SIZE = 24;
    private static final int DEFAULT_HEIGHT = 35;
    private static final int MARGIN = 5;

    // Members
    private Listener listener = null; 
    private TextEditor searchTextEditor = null;
    private Image searchIconImage = null;
    private final Font font = Font.getDefaultFont();
    private String searchTerm = "";
    private String hintText = DEFAULT_HINT_TEXT;
    private final int fontHeight = font.getHeight();
    private int width = 0;
    private int searchIconImageWidth = 0;
    private int positionX = 0;
    private int positionY = 0;
    private int pointerX = UNDEFINED;
    private int pointerY = UNDEFINED;
    private boolean focused = false;
    private boolean iconPressed = false;

    /**
     * 
     * @param parent
     * @param listener
     * @param width
     * @param positionX
     * @param positionY
     * @throws NullPointerException
     */
    protected SearchBar(Canvas parent, Listener listener, int width,
                        int positionX, int positionY)
        throws NullPointerException
    {
        super(width, DEFAULT_HEIGHT);
        
        if (parent == null || listener == null) {
            throw new NullPointerException("Arguments cannot be null!");
        }
        
        this.listener = listener;
        this.width = width;
        this.positionX = positionX;
        this.positionY = positionY;
        
        try {
            searchIconImage = Image.createImage(SEARCH_ICON_URI);
            searchIconImageWidth = searchIconImage.getWidth();
        }
        catch (IOException e) {
            System.out.println("SearchBar.SearchBar(): Failed to load some of the image assets!");
        }
        catch (NullPointerException e) {
        }
        
        VirtualKeyboard.setVisibilityListener(this);
        
        // Create the text editor
        searchTextEditor =
            TextEditor.createTextEditor("", TEXT_FIELD_MAX_SIZE,
                TextField.NON_PREDICTIVE,
                this.width - searchIconImageWidth - MARGIN * 3 - positionX,
                DEFAULT_HEIGHT - MARGIN * 2);
        searchTextEditor.insert(searchTerm, 0);
        searchTextEditor.setForegroundColor(TEXT_COLOR);
        searchTextEditor.setBackgroundColor(BACKGROUND_COLOR);
        searchTextEditor.setParent(parent); // Canvas to draw on
        searchTextEditor.setPosition(positionX + MARGIN, positionY + MARGIN);
        searchTextEditor.setTouchEnabled(false);
        searchTextEditor.setTextEditorListener(this);
        
        setParent(parent);
        setPosition(positionX, positionY);
    }

    /** 
     * @param hintText The hint text to set (e.g. "Type here").
     */
    public void setHintText(String hintText) {
        this.hintText = hintText;
        repaint();
    }

    /**
     * Sets the focus of this item.
     * @param focused If true, will launch the keyboard and change appearance.
     * If false, will hide the keyboard.
     */
    public void setFocused(boolean focused) {
        if (this.focused != focused) {
            System.out.println("SearchBar.setFocused(): " + focused);
            this.focused = focused;
            
            try {
                if (focused) {
                	searchTextEditor.setVisible(true);
                	searchTextEditor.setFocus(true);
                }
                else {
                    searchTextEditor.setFocus(false);
                }
            }
            catch (IllegalStateException e) {
            }
            
            repaint();
            listener.onSearchBarFocusedChanged(focused);
        }
    }

    /** 
     * @return True if this search bar has the focus.
     */
    public boolean getHasFocus() {
        return focused;
    }

    /** 
     * @return The search term.
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * Clears the search term.
     */
    public void clear() {
        if (searchTerm.length() > 0) {
            searchTerm = "";
            searchTextEditor.setContent("");
            setFocused(false);
            repaint();
            listener.onSearchTermChanged("");
        }
    }

    /**
     * Erases the last character of the search term.
     */
    public void eraseLastCharacter() {
        if (searchTerm.length() > 0) {
            searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
            searchTextEditor.setContent(searchTerm);
            repaint();
            listener.onSearchTermChanged(searchTerm);
        }
    }

    /**
     * @see com.nokia.mid.ui.TextEditorListener#inputAction(com.nokia.mid.ui.TextEditor, int)
     */
    public void inputAction(TextEditor textEditor, int actions) {
        System.out.println("SearchBar.inputAction(): " + actions);
        
        if ((actions & TextEditorListener.ACTION_CONTENT_CHANGE) != 0) {
            searchTerm = textEditor.getContent().trim();
            repaint();
            listener.onSearchTermChanged(searchTerm);
        }
    }

    protected void onPointerPressed(int x, int y) {
        pointerX = x;
        pointerY = y;
        
        if (x + positionX > width - MARGIN * 2 - searchIconImageWidth
            && y > positionY && y < positionY + DEFAULT_HEIGHT)
        {
            iconPressed = true;
        }
    }

    protected void onPointerDragged(int x, int y) {
        if (pointerX < 0 || pointerY < 0) {
            return;
        }
        
        pointerX = x;
        pointerY = y;
        
        if (iconPressed
            && x + positionX < width - MARGIN * 2 - searchIconImageWidth
            && y > positionY && y < positionY + DEFAULT_HEIGHT)
        {
            iconPressed = false;
        }
    }

    protected void onPointerReleased(int x, int y) {
        if (pointerX >= positionX
            && pointerX + positionX <= width - MARGIN * 2 - searchIconImageWidth
            && pointerY >= positionY
            && pointerY <= positionY + DEFAULT_HEIGHT)
        {
            setFocused(true);
            pointerX = UNDEFINED;
            pointerY = UNDEFINED;
        }
        
        if (iconPressed
            && x + positionX > width - MARGIN * 2 - searchIconImageWidth
            && y > positionY && y < positionY + DEFAULT_HEIGHT)
        {
            if (searchTerm.length() > 0) {
                listener.onExecuteSearchRequest();
            }
            
            iconPressed = false;
        }
    }

    /**
     * @see com.nokia.mid.ui.CanvasGraphicsItem#paint(javax.microedition.lcdui.Graphics)
     */
    protected void paint(Graphics graphics) {
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, DEFAULT_HEIGHT);
        
        if (searchTerm.length() == 0) {
            if (hintText != null) {
                graphics.setColor(HINT_TEXT_COLOR);
                graphics.drawString(hintText, MARGIN, MARGIN, Graphics.TOP | Graphics.LEFT);
            }
        }
        
        if (searchIconImage != null) {
            graphics.drawImage(searchIconImage,
                width - MARGIN - searchIconImage.getWidth(), MARGIN,
                Graphics.TOP | Graphics.LEFT);
        }
        
        if (focused) {
            graphics.setColor(HIGHLIGHT_COLOR);
        }
        else {
            graphics.setColor(TEXT_LINE_COLOR);
        }
        
        graphics.drawLine(0, fontHeight + MARGIN * 2, width, fontHeight + MARGIN * 2);
    }

    /**
     * @see com.nokia.mid.ui.KeyboardVisibilityListener#hideNotify(int)
     */
    public void hideNotify(int keyboardCategory) {
        System.out.println("SearchBar.hideNotify()");
        setFocused(false);
    }

    /**
     * @see com.nokia.mid.ui.KeyboardVisibilityListener#showNotify(int)
     */
    public void showNotify(int keyboardCategory) {
        System.out.println("SearchBar.showNotify()");
    }

    /**
     * An interface for SearchBar listener.
     */
    public interface Listener {
        void onSearchBarFocusedChanged(boolean focused);
        void onSearchTermChanged(String searchTerm);
        void onExecuteSearchRequest();
    }
}
