/**
 * Copyright (c) 2013 Nokia Corporation.
 */

package com.nokia.example.picasaviewer.ui;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nokia.mid.ui.CanvasGraphicsItem;
import com.nokia.mid.ui.CategoryBar;
import com.nokia.mid.ui.ElementListener;
import com.nokia.mid.ui.IconCommand;

import com.nokia.example.picasaviewer.util.ImageUtils;

/**
 * A custom category bar.
 */
public class CustomCategoryBar
    extends CategoryBar
{
    // Constants
    public static final int HEIGHT = 44;
    private static final int WIDTH = 240;
    private static final int UNDEFINED = -1;

    // Members
    private CustomCategoryBarRenderer _renderer = null;
    private Canvas _canvas = null;
    private ElementListener _listener = null;
    private int _tabCount = 0;
    private int _y = 0;
    private int _pressedIndex = UNDEFINED;
    private boolean _setSelectedIndexAutomatically = false;
    private boolean _notifyIndexChangedOnlyWhenReleased = true;

    /**
     * Creates a new custom category bar instance.
     * @param canvas The canvas which renders the category bar. Can be null if
     * the bar is painted manually.
     * @param unselectedIcons The unselected icons.
     * @return A newly created instance.
     */
    public static CustomCategoryBar getNewInstance(Canvas canvas,
                                                   Image[] unselectedIcons)
    {
        Image[] selectedIcons = createSelectedIcons(unselectedIcons);
        String[] labels = new String[unselectedIcons.length];
        
        for (int i = 0; i < labels.length; ++i) {
            labels[i] = new String();
        }
        
        return new CustomCategoryBar(canvas, unselectedIcons, selectedIcons, labels);
    }

    /**
     * Constructor.
     */
    private CustomCategoryBar(Canvas canvas,
                              Image[] unselectedIcons,
                              Image[] selectedIcons,
                              String[] labels)
    {
        super(unselectedIcons, selectedIcons, labels, CategoryBar.ELEMENT_MODE_STAY_SELECTED);
        _canvas = canvas;
        _tabCount = unselectedIcons.length;
        _renderer = new CustomCategoryBarRenderer(WIDTH, HEIGHT, unselectedIcons, selectedIcons);
    }

    /**
     * @see com.nokia.mid.ui.CategoryBar#setVisibility(boolean)
     */
    public void setVisibility(boolean visible) {
        _renderer._isVisible = visible;
        
        if (_canvas != null) {
            _renderer.setVisible(visible);
        }
    }

    /**
     * @see com.nokia.mid.ui.CategoryBar#getVisibility()
     */
    public boolean getVisibility() {
        return _renderer._isVisible;
    }

    /**
     * @see com.nokia.mid.ui.CategoryBar#setElementListener(com.nokia.mid.ui.ElementListener)
     */
    public void setElementListener(ElementListener listener) {
        super.setElementListener(listener);
        _listener = listener;
    }

    /**
     * @see com.nokia.mid.ui.CategoryBar#setSelectedIndex(int)
     */
    public void setSelectedIndex(int index) {
        super.setSelectedIndex(index);
        _renderer.repaint();
    }

    //-------------------------------------------------------------------------
    // New methods ->

    /**
     * Sets the opacity of the category bar.
     * @param opacity The opacity to set.
     */
    public void setOpacity(float opacity) {
        if (opacity >= 0 && opacity <= 1) {
            _renderer.setOpacity(opacity, true);
        }
    }

    /** 
     * @param set If true will call setSelectedIndex() automatically when
     * notifying the listener.
     */
    public void setSelectedIndexAutomatically(boolean set) {
        _setSelectedIndexAutomatically = set;
    }

    /** 
     * @param notify If true will notify the listener of index change only when
     * the index changes with pointerReleased event. If false, will notify even
     * with pointerPressed and pointerDragged events.
     */
    public void setNotifyIndexChangedOnlyWhenReleased(boolean notify) {
        _notifyIndexChangedOnlyWhenReleased = notify;
    }

    public void onPointerPressed(int x, int y) {
        if (y > _y && y < _y + HEIGHT) {
            final int index = x / _renderer._tabWidth;
            
            if (getSelectedIndex() != index) {
                if (_notifyIndexChangedOnlyWhenReleased) {
                    _pressedIndex = index;
                }
                else {
                    notifyIndexChanged(index);
                }
            }
            
            if (_renderer._setOpacity < 1 && _renderer._actualOpacity != 1.0f) {
                _renderer.setOpacity(1.0f, false);
            }
        }
    }

    public void onPointerDragged(int x, int y) {
        if (y > _y && y < _y + HEIGHT) {
            final int index = x / _renderer._tabWidth;
            
            if (getSelectedIndex() != index
                && !_notifyIndexChangedOnlyWhenReleased)
            {
                notifyIndexChanged(index);
            }
            
            if (_notifyIndexChangedOnlyWhenReleased) {
                _pressedIndex = index;
            }
            
            if (_renderer._setOpacity < 1 && _renderer._actualOpacity != 1.0f) {
                _renderer.setOpacity(1.0f, false);
            }
        }
        else {
            if (_renderer._actualOpacity != _renderer._setOpacity) {
                _renderer.setOpacity(_renderer._setOpacity, false);
            }
            
            _pressedIndex = UNDEFINED;
        }
    }

    public void onPointerReleased(int x, int y) {
        if (y > _y && y < _y + HEIGHT) {
            final int index = x / _renderer._tabWidth;
            
            if (getSelectedIndex() != index) {
                notifyIndexChanged(index);
            }
        }
        
        if (_renderer._setOpacity < 1
            && _renderer._actualOpacity != _renderer._setOpacity)
        {
            _renderer.setOpacity(_renderer._setOpacity, false);
        }
        
        _pressedIndex = UNDEFINED;
    }

    /**
     * Paints the category bar.
     * @param graphics The Graphics instance.
     * @param y The Y coordinate of the top-left corner.
     */
    public void paint(Graphics graphics, int y) {
        if (_canvas == null && _y != y) {
            _y = y;
        }
        
        _renderer.paint(graphics, _y);
    }

    /**
     * @see com.nokia.mid.ui.CategoryBar#setSelectedIndex(int)
     */
    private void notifyIndexChanged(int index) {
        if (_setSelectedIndexAutomatically) {
            setSelectedIndex(index);
        }
        
        if (_listener != null) {
            _listener.notifyElementSelected(this, index);
        }
    }

    /**
     * Creates the selected icons based on the unselected ones.
     * @param unselectedIcons The unselected icons.
     * @return The newly created selected icons image assets.
     */
    private static Image[] createSelectedIcons(Image[] unselectedIcons) {
        final int length = unselectedIcons.length;
        Image[] selectedIcons = new Image[length];
        
        for (int i = 0; i < length; ++i) {
            // Highlight color is 0x29a7cc (RGB: 41, 167, 204)
            selectedIcons[i] = ImageUtils.substractRgb(unselectedIcons[i], 214, 88, 51);
        }
        
        return selectedIcons;
    }

    // <- New methods
    //-------------------------------------------------------------------------

    /* Visibility of the constructors hidden since we do not want to allow to
     * create the factory from the outside
     */
    protected CustomCategoryBar(IconCommand[] elements, boolean useLongLabel) {
        super(elements, useLongLabel);
    }
    protected CustomCategoryBar(IconCommand[] elements, boolean useLongLabel, int mode) {
        super(elements, useLongLabel, mode);
    }
    protected CustomCategoryBar(Image[] unselectedIcons, Image[] selectedIcons, String[] labels) {
        super(unselectedIcons, selectedIcons, labels);
    }
    protected CustomCategoryBar(Image[] unselectedIcons, Image[] selectedIcons, String[] labels, int mode) {
        super(unselectedIcons, selectedIcons, labels, mode);
    }


    /**
     * For rendering our custom category bar.
     */
    private class CustomCategoryBarRenderer
        extends CanvasGraphicsItem
    {
        // Constants
        private final String[] IMAGE_URIS = {
            "/selected-tab-left-corner-edge.png",
            "/selected-tab-left-edge.png",
            "/selected-tab-right-corner-edge.png",
            "/selected-tab-right-edge.png",
            "/selected-tab-texture.png",
            "/tab-left-corner-edge.png",
            "/tab-right-corner-edge.png",
            "/tab-texture.png"
        };
        
        private static final int SELECTED_TAB_LEFT_CORNER_EDGE = 0;
        private static final int SELECTED_TAB_LEFT_EDGE = 1;
        private static final int SELECTED_TAB_RIGHT_CORNER_EDGE = 2;
        private static final int SELECTED_TAB_RIGHT_EDGE = 3;
        private static final int SELECTED_TAB_TEXTURE = 4;
        private static final int TAB_LEFT_CORNER_EDGE = 5;
        private static final int TAB_RIGHT_CORNER_EDGE = 6;
        private static final int TAB_TEXTURE_FULL = 7;
        private static final int TAB_TEXTURE_WITH_ONE_EDGE = 8;
        private static final int TAB_TEXTURE_WITH_TWO_EDGES = 9;
        private static final int IMAGE_ASSET_COUNT = 10;
        
        // Members 
        private Image _opaqueImages[] = null;
        private Image _translucentImages[] = null;
        private Image _currentImages[] = null;
        private Image _unselectedIcons[] = null;
        private Image _selectedIcons[] = null;
        private Image _translucentIcons[] = null;
        private Image _currentIcons[] = null;
        private float _setOpacity = 1.0f;
        private float _actualOpacity = _setOpacity;
        private float _storedOpacity = UNDEFINED;
        private int _edgeImageWidth = 0;
        private int _selectedTabWidthWithoutEdges = 0;
        private int _tabWidth = 0;
        private boolean _isVisible = false;

        /**
         * Constructor.
         * @param width
         * @param height
         * @param unselectedIcons
         * @param selectedIcons
         */
        private CustomCategoryBarRenderer(int width,
                                          int height,
                                          Image[] unselectedIcons,
                                          Image[] selectedIcons)
        {
            super(width, height);
            _unselectedIcons = unselectedIcons;
            _selectedIcons = selectedIcons;
            _currentIcons = unselectedIcons;
            createImages();
            
            if (_canvas != null) {
                setParent(_canvas);
                _y = _canvas.getHeight() - HEIGHT;
                setPosition(0, _y);
            }
        }

        /**
         * Sets the opacity of the category bar. Calling this method has no cost
         * if the opacity below value 1 was set previously as the translucent
         * image assets are already created. However, if the opacity value
         * differs from the previous translucent value, the image assets need to
         * be re-created.
         * @param opacity The opacity to set.
         * @param stick If true, will consider the given opacity non-temporary.
         */
        public void setOpacity(final float opacity, final boolean stick) {
            if (opacity < 0 || opacity > 1) {
                // Invalid opacity
            	return;
            }
            
            if (opacity == 1.0f) {
                System.out.println("CustomCategoryBarRenderer.setOpacity(): Setting to opaque.");
                _currentImages = _opaqueImages;
                _currentIcons = _unselectedIcons;
            }
            else if (_storedOpacity != opacity) {
                System.out.println("CustomCategoryBarRenderer.setOpacity(): New opacity: " + opacity);
                createAndTakeInUseTranslucentImages();
            }
            else {
                // Use the existing assets
                System.out.println("CustomCategoryBarRenderer.setOpacity(): Using stored opacity: " + _setOpacity);
                _currentImages = _translucentImages;
                _currentIcons = _translucentIcons;
            }
            
            _actualOpacity = opacity;
            
            if (stick) {
                _setOpacity = opacity;
            }
            
            repaint();
        }

        /**
         * @see com.nokia.mid.ui.CanvasGraphicsItem#paint(javax.microedition.lcdui.Graphics)
         */
        protected void paint(Graphics graphics) {
            paint(graphics, 0);
        }

        /**
         * For convenience.
         * @param graphics The Graphics instance.
         * @param y The Y coordinate of the bar.
         */
        protected void paint(Graphics graphics, final int y) {
            if (!_isVisible) {
                return;
            }
            
            final int selectedIndex = getSelectedIndex();
            int x = 0;
            final int anchor = Graphics.TOP | Graphics.LEFT;
            
            // Paint the tab bar
            for (int i = 0; i < _tabCount; ++i) {
                if (i == selectedIndex || i == _pressedIndex) {
                    if (i == 0) {
                        graphics.drawImage(_currentImages[SELECTED_TAB_LEFT_CORNER_EDGE], x, y, anchor);
                    }
                    else {
                        graphics.drawImage(_currentImages[SELECTED_TAB_LEFT_EDGE], x, y, anchor);
                    }
                    
                    x += _edgeImageWidth;
                    graphics.drawImage(_currentImages[SELECTED_TAB_TEXTURE], x, y, anchor);
                    x += _selectedTabWidthWithoutEdges;
                    
                    if (i == _tabCount - 1) {
                        graphics.drawImage(_currentImages[SELECTED_TAB_RIGHT_CORNER_EDGE], x, y, anchor);
                    }
                    else {
                        graphics.drawImage(_currentImages[SELECTED_TAB_RIGHT_EDGE], x, y, anchor);
                    }
                    
                    x += _edgeImageWidth;
                }
                else {
                    if (i == 0) {
                        graphics.drawImage(_currentImages[TAB_LEFT_CORNER_EDGE], x, y, anchor);
                        x += _edgeImageWidth;
                    }
                    
                    if (i == 0 && i != _tabCount - 1) {
                        graphics.drawImage(_currentImages[TAB_TEXTURE_WITH_ONE_EDGE], x, y, anchor);
                        x += _tabWidth - _edgeImageWidth;
                    }
                    else if (i == 0 && i == _tabCount - 1) {
                        graphics.drawImage(_currentImages[TAB_TEXTURE_WITH_TWO_EDGES], x, y, anchor);
                        x += _tabWidth - _edgeImageWidth * 2;
                    }
                    else {
                        graphics.drawImage(_currentImages[TAB_TEXTURE_FULL], x, y, anchor);
                        x += _tabWidth;
                    }
                    
                    if (i == _tabCount - 1) {
                        graphics.drawImage(_currentImages[TAB_RIGHT_CORNER_EDGE], x, y, anchor);
                        x += _edgeImageWidth;
                    }
                }
            }
            
            // Paint the icons
            x = 0;
            Image iconImage = null;
            
            for (int i = 0; i < _tabCount; ++i) {
                if (i == selectedIndex || i == _pressedIndex) {
                    iconImage = _selectedIcons[i];
                }
                else {
                    iconImage = _currentIcons[i];
                }
                
                if (iconImage != null) {
                    graphics.drawImage(iconImage,
                        x + (_tabWidth - iconImage.getWidth()) / 2,
                        y + (HEIGHT - iconImage.getHeight()) / 2, anchor);
                }
                
                x += _tabWidth;
            }
        }

        /**
         * Creates the image assets used by the category bar.
         */
        private void createImages() {
            _opaqueImages = new Image[IMAGE_ASSET_COUNT];
            
            try {
                for (int i = 0; i < IMAGE_URIS.length; ++i) {
                    _opaqueImages[i] = Image.createImage(IMAGE_URIS[i]);
                }
            }
            catch (IOException e) {
                System.out.println("CustomCategoryBarRenderer.createImages(): Failed to load image!");
            }
            
            _tabWidth = WIDTH / _tabCount;
            _edgeImageWidth = _opaqueImages[SELECTED_TAB_LEFT_CORNER_EDGE].getWidth();
            _selectedTabWidthWithoutEdges = _tabWidth - _edgeImageWidth * 2; 
            
            Image temp = _opaqueImages[TAB_TEXTURE_FULL];
            _opaqueImages[TAB_TEXTURE_FULL] = ImageUtils.scale(temp, _tabWidth, HEIGHT);
            _opaqueImages[TAB_TEXTURE_WITH_ONE_EDGE] =
                    ImageUtils.scale(temp, _tabWidth - _edgeImageWidth, HEIGHT);
            _opaqueImages[TAB_TEXTURE_WITH_TWO_EDGES] =
                    ImageUtils.scale(temp, _tabWidth - _edgeImageWidth * 2, HEIGHT);
            _opaqueImages[SELECTED_TAB_TEXTURE] =
                ImageUtils.scale(_opaqueImages[SELECTED_TAB_TEXTURE],
                    _selectedTabWidthWithoutEdges, HEIGHT);
            
            _currentImages = _opaqueImages;
        }

        /**
         * Creates the translucent image assets based on the original ones.
         * Note that this is done asynchronously.
         */
        private void createAndTakeInUseTranslucentImages() {
            final float opacity = _setOpacity;
            final int alpha = (int)(255 * opacity);
            
            if (_translucentImages == null) {
                _translucentImages = new Image[_opaqueImages.length];
                _translucentIcons = new Image[_unselectedIcons.length];
            }
            
            new Thread() {
                public void run() {
                    for (int i = 0; i < _opaqueImages.length; ++i) {
                        _translucentImages[i] = ImageUtils.setAlpha(_opaqueImages[i], alpha);
                    }
                    
                    for (int i = 0; i < _unselectedIcons.length; ++i) {
                        _translucentIcons[i] = ImageUtils.setAlpha(_unselectedIcons[i], alpha);
                    }
                    
                    _currentImages = _translucentImages;
                    _currentIcons = _translucentIcons;
                    _storedOpacity = opacity;
                    repaint();
                }
            }.start();
        }
    }
}
