/**
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.picasaviewer.util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import org.tantalum.util.L;
import com.nokia.mid.ui.IconCommand;

/**
 * A custom icon command for refreshing the images.
 */
public class UpdateIconCommand extends IconCommand {
    private static Image image = null;

    static {
        try {
            image = Image.createImage("/connect.png");
        }
        catch (Exception e) {
            //#debug
            L.e("Can not initialize", "Update icon image", e);
        }
    }

    public UpdateIconCommand() {
        super("Refresh", "Refresh images", image, image, Command.OK, 0);
    }
}
