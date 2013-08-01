/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.util;

import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;

/**
 * Utilities for handling images.
 */
public class ImageUtils {
    /**
     * Sets the alpha of the each pixel in the image based on the given value.
     * @param image The original image.
     * @param alpha The alpha value.
     * @return A newly created image with applied alpha.
     * @throws NullPointerException If the given image is null.
     * @throws IllegalArgumentException If the alpha value is not in [0, 255].
     */
    public static Image setAlpha(final Image image, final int alpha)
       throws NullPointerException, IllegalArgumentException
    {
        if (image == null) {
            throw new NullPointerException();
        }
        
        if (alpha < 0 || alpha > 255) {
            throw new IllegalArgumentException();
        }
        
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int[] originalRgb = new int[width * height];
        image.getRGB(originalRgb, 0, width, 0, 0, width, height);
        
        final int opaqueRgb[] = new int[width * height];
        
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (originalRgb[(width * y) + x] >>> 24 == 255) {
                    opaqueRgb[(width * y) + x] =
                        originalRgb[(width * y) + x] + (alpha << 24);
                }
                else {
                    opaqueRgb[(width * y) + x] = originalRgb[(width * y) + x];
                }
            }
        }
        
        return Image.createRGBImage(opaqueRgb, width, height, true);
    }

    /**
     * Subtracts the given RGB values from the given image while maintaining
     * the alpha level of each pixel.
     * @param image The original image.
     * @param r Red value to subtract.
     * @param g Green value to subtract.
     * @param b Blue value to subtract.
     * @return The modified image.
     * @throws NullPointerException If the given image is null.
     * @throws IllegalArgumentException If any of RGB values is invalid.
     */
    public static Image substractRgb(final Image image,
                                     final int r,
                                     final int g,
                                     final int b)
       throws NullPointerException, IllegalArgumentException
    {
        if (image == null) {
            throw new NullPointerException();
        }
        
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException();
        }
        
        final int[] rgbToSubstract = new int[3];
        rgbToSubstract[0] = r;
        rgbToSubstract[1] = g;
        rgbToSubstract[2] = b;
        
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int[] originalRgb = new int[width * height];
        image.getRGB(originalRgb, 0, width, 0, 0, width, height);
        
        final int newRgb[] = new int[width * height];
        int pixel = 0;
        int[] argb = new int[4];
        
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = originalRgb[(width * y) + x];
                argb[0] = (pixel & 0xff000000) >>> 24;
                argb[1] = (pixel & 0x00ff0000) >>> 16;
                argb[2] = (pixel & 0x0000ff00) >>> 8;
                argb[3] = pixel & 0x000000ff;
                
                for (int i = 0; i < 3; ++i) {
                    if (argb[i + 1] - rgbToSubstract[i] >= 0) {
                        argb[i + 1] -= rgbToSubstract[i];
                    }
                    else {
                        argb[i + 1] = 0;
                    }
                }
                    
                newRgb[(width * y) + x] =
                    (argb[0] << 24) | (argb[1] << 16) | (argb[2] << 8) | argb[3];
            }
        }
        
        return Image.createRGBImage(newRgb, width, height, true);
    }

    /**
     * Scales the given image.
     * @param image The original image.
     * @param newWidth The new image width.
     * @param newHeight The new image height.
     * @return The scaled image.
     */
    public static Image scale(final Image image,
                              final int newWidth,
                              final int newHeight)
    {
        if (newWidth <= 0 || newHeight <= 0 || image == null) {
            throw new IllegalArgumentException(
                "Invalid width or height or the given image is null!");
        }
        
        // Get the size and the RGB array of the original image
        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();
        final int[] originalRgb = new int[sourceWidth * sourceHeight];
        image.getRGB(originalRgb, 0, sourceWidth, 0, 0, sourceWidth, sourceHeight);
        
        final int scaledRgb[] = new int[newWidth * newHeight];
        
        for (int y = 0; y < newHeight; y++) {
            final int dy = y * sourceHeight / newHeight;
            
            for (int x = 0; x < newWidth; x++) {
                final int dx = x * sourceWidth / newWidth;
                scaledRgb[(newWidth * y) + x] = originalRgb[(sourceWidth * dy) + dx];
            }
        }
        
        return Image.createRGBImage(scaledRgb, newWidth, newHeight, true);
    }

    /**
     * This method takes an image and overlays all the visible pixels with the
     * specified color. This is useful for single color icons that should be
     * colored according to the theme of the device.
     *
     * @param image The original image.
     * @return The resulting image.
     */
    public static Image drawMaskedImage(Image image, Display display) {
        final int color = display.getColor(Display.COLOR_HIGHLIGHTED_BORDER);
        
        // Store the pixel array of the image
        final int[] sourceData = new int[image.getHeight() * image.getWidth()];
        
        image.getRGB(
                sourceData, 
                0, 
                image.getWidth(), 
                0, 
                0, 
                image.getWidth(),
                image.getHeight());
        
        // Overlay non-transparent pixels with the specified color
        for (int i = 0; i < sourceData.length; i++) {
            sourceData[i] = 
                (sourceData[i] & 0xFF000000) | (color & 0x00FFFFFF);
        }
        
        // Create the new image
        final Image overlayed =
            DirectUtils.createImage(
                image.getWidth(), 
                image.getHeight(), 
                0x000000);
        
        overlayed.getGraphics().drawRGB(
                sourceData, 
                0, 
                image.getWidth(), 
                0, 
                0,
                image.getWidth(), 
                image.getHeight(), 
                true);
        
        return overlayed;
    }
}
