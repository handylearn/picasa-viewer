/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.common;

import java.util.Vector;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.storage.CacheView;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.ImageCacheView;
import org.tantalum.util.L;

/**
 * Class for accessing cached data like thumbnails, images and feeds. The class
 * gets the resource from the cache first, if it is not found there it retrieves
 * it from the web.
 */
public class PicasaStorage {
    public static final int NR_OF_FEATURED = 20;
    public static int imageSide;
    private static int screenWidth;
    private static String thumbSize;
    private static int imageSize; //Must be valid picasa size
    private static String urlOptions;
    private static String featURL;
    private static String searchURL;
    private static volatile PicasaImageObject selectedImage = null;
    public static StaticWebCache feedCache;
    public static StaticWebCache imageCache;

    /**
     * Initializes the storage.
     * @param width The width of the screen. This is used to determine how large
     * images should be.
     */
    public static synchronized void init(final int width) {
        if (feedCache == null) {
            final ImageCacheView imageCacheView =
                PlatformUtils.getInstance().getImageCacheView();
            
            screenWidth = width;
            
            if (screenWidth < 256) {
                imageSide = 128; // Must be supported picasa thumb size
                imageSize = 288; // Image max size to get suitable sized images from picasa
            }
            else {
                imageSide = 256; // Must be supported thumb size in picasa
                imageSize = 720; // Picasa size for "fullsize" images
            }
            
            imageCacheView.setMaxSize(width, width);
            
            try {
                imageCache = StaticWebCache.getWebCache(
                        '4', 
                        PlatformUtils.PHONE_DATABASE_CACHE, 
                        imageCacheView, 
                        new StaticWebCache.HttpTaskFactory(), null);
            } catch (FlashDatabaseException e) {
                L.e("Could not initialize imageCache.", "", e);
            }
            
            try {
                feedCache = StaticWebCache.getWebCache(
                        '5', 
                        (CacheView) new ImageObjectTypeHandler());
            } catch (FlashDatabaseException e) {
                L.e("Could not initialize feedCache.", "", e);
            }
            
            thumbSize = 
                    imageSide + 
                    "c"; // c is for cropped, ensures image proportions
            
            urlOptions = 
                    "?alt=json&kind=photo&max-results=" + 
                    NR_OF_FEATURED + 
                    "&thumbsize=" + 
                    thumbSize + 
                    "&fields=entry(title,author(name),updated,media:group)" + 
                    "&imgmax=" + 
                    imageSize;
            
            featURL = 
                    "http://picasaweb.google.com/data/feed/base/featured" + 
                    urlOptions;
            
            searchURL = 
                    "http://picasaweb.google.com/data/feed/base/all" + 
                    urlOptions + 
                    "&q=";
        }
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static PicasaImageObject getSelectedImage() {
        return selectedImage;
    }

    public static void setSelectedImage(final PicasaImageObject selectedImage) {
        PicasaStorage.selectedImage = selectedImage;
    }

    /**
     * Tell Tantalum to fetch the ImageObjects
     *
     * @param callback - RunnableResult to be ran in the UI thread
     * @param fromWeb - True to force fetch from web
     */
    public static Task getImageObjects(final String search,
                                       final int getPriority,
                                       final int getType,
                                       final Task callback)
    {
        final String url = search != null ? searchURL + search : featURL;
        return feedCache.getAsync(url, getPriority, getType, callback);
    }

    /**
     * Class for converting the JSON response in to a Vector of
     * PicasaImageObject-objects. The vector is saved by Tantalum.
     */
    private static class ImageObjectTypeHandler implements CacheView {
        public Object convertToUseForm(Object key, byte[] bytes) {
            JSONObject responseJson;
            
            try {
                responseJson = new JSONObject(new String(bytes));
            }
            catch (JSONException ex) {
                //#debug
                L.e("bytes are not a JSON object", featURL, ex);
                return null;
            }
            
            JSONArray entries = new JSONArray();
            final Vector vector = new Vector();
            
            try {
                final JSONObject feed = 
                        ((JSONObject) responseJson).getJSONObject("feed");
                entries = feed.getJSONArray("entry");
            }
            catch (JSONException e) {
                vector.addElement(
                        new PicasaImageObject("No Results", "", "", ""));

                //#debug
                L.e("JSON no result", featURL, e);
            }

            for (int i = 0; i < entries.length(); i++) {
                try {
                    final JSONObject jsonObject = 
                            entries.getJSONObject(i);
                    final String title = 
                            jsonObject.getJSONObject("title").getString("$t");
                    final String author = 
                            jsonObject.getJSONArray("author").getJSONObject(0).getJSONObject("name").getString("$t");
                    final String thumbUrl = 
                            jsonObject.getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
                    final String imageUrl = 
                            jsonObject.getJSONObject("media$group").getJSONArray("media$content").getJSONObject(0).getString("url");

                    //#mdebug
                    L.i("JSON parsed title: ", title);
                    L.i("JSON parsed author: ", author);
                    L.i("JSON parsed thumb url: ", thumbUrl);
                    L.i("JSON parsed image url: ", imageUrl);
                    //#enddebug

                    vector.addElement(
                            new PicasaImageObject(title, author, thumbUrl, imageUrl));
                }
                catch (JSONException e) {
                    //#debug
                    L.e("JSON item parse error", featURL, e);
                }
            }

            if (entries.length() == 0) {
                vector.addElement(new PicasaImageObject("No Results", "", "", ""));
            }
            
            return vector;
        }
    }
}
