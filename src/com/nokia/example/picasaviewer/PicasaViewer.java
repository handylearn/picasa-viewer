/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.tantalum.PlatformUtils;
import com.nokia.example.picasaviewer.util.ViewManager;

/**
 * The main class of the application.
 */
public final class PicasaViewer
        extends MIDlet
{
    /*
     * Tune this number up and down for best concurrency of your application.
     * Less than 2 is not allowed and more than 4 probably introduces too much
     * context switching between threads.
     */
    private static final int NUMBER_OF_WORKER_THREADS = 2;
    
    public static final boolean HAS_ONE_KEY_BACK;

    private ViewManager viewManager;

    /**
     * Static constructor.
     */
    static {
        String keyboardType = System.getProperty("com.nokia.keyboard.type");
        
        if (keyboardType  != null && keyboardType.equalsIgnoreCase("OnekeyBack")) {
            HAS_ONE_KEY_BACK = true;
        }
        else {
            HAS_ONE_KEY_BACK = false;
        }
    }

    /**
     * @see javax.microedition.midlet.MIDlet#startApp()
     */
    public void startApp() {
        // Must be initialized before using any parts of Tantalum e.g. L class.
        PlatformUtils.getInstance().setProgram(this,
                NUMBER_OF_WORKER_THREADS,
                PlatformUtils.NORMAL_LOG_MODE);
        
        viewManager = new ViewManager(this);
        viewManager.showInitialView();
    }

    /**
     * @see javax.microedition.midlet.MIDlet#pauseApp()
     */
    protected void pauseApp() {
    }

    /**
     * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
     */
    protected void destroyApp(boolean unconditional)
            throws MIDletStateChangeException
    {
        PlatformUtils.getInstance().shutdown(unconditional, "Shutting down.");
    }
}
