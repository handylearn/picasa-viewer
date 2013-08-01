PicasaViewer
============

PicasaViewer example application demonstrates the use of the cross-platform
development library, Tantalum 5. The application features images from the Picasa
web gallery and allows the user to search for specific images from the service.
Furthermore, the example application demonstrates adaptive design, performance,
JSON parsing, advanced caching and cross-platform techniques in Java
applications.

The version 1.0 was implemented so that both Nokia Asha and Android versions
used the same common engine code. The user interface (UI) layer was implemented
separately: Nokia Asha used LCDUI and Android used the Android UI library.

This example application is hosted in Nokia Developer Projects:
- https://projects.developer.nokia.com/picasa_viewer

For more information on the implementation, visit the wiki pages:
- https://projects.developer.nokia.com/picasa_viewer/wiki

What's new in version 1.1
-------------------------
- First port of the application to Nokia Asha software platform 1.0
- Used Tantlum version updated to Tantalum5


1. Usage
-------------------------------------------------------------------------------

The application displays a grid of images downloaded from Picasa. The user can 
tap an image to display it in full screen. The full screen view also shows
the image title and the author of the image. The full screen view can be
dismissed by tapping the screen or by selecting back.

By default the application displays Picasa featured images. In Category Bar
(Nokia Asha software platfomr and Series 40) or Menu (Android) there's an option
to search images. When user types the search terms the displayed images are
refreshed on fly. An efficient cache is utilised to restore the previously
downloaded images.


2. Prerequisites
-------------------------------------------------------------------------------

- Java ME basics
- Android basics (version 1.0 only)


3. Important files and classes
-------------------------------------------------------------------------------

3.1 Package com.nokia.example.picasaviewer
------------------------------------------
- PicasaViewer.java      The MIDlet main class

3.2 Package com.nokia.example.picasaviewer.common
-------------------------------------------------
- PicasaImageObject.java    Class for storing information about a Picasa image
- PicasaStorage.java        Class for accessing cached data like thumbnails,
                            images and feeds

3.3 Package com.nokia.example.picasaviewer.ui
---------------------------------------------
- DetailCanvas.java     Class for displaying full screen image with details
- FeaturedCanvas.java   Class for displaying a grid of Picasa featured images
- SearchCanvas.java     Class for displaying Picasa search dialog and search 
                        results

3.4 Package com.nokia.example.picasaviewer.util
-----------------------------------------------
- ViewManager.java      Manages the view stack and navigation


4. Compatibility
-------------------------------------------------------------------------------

4.1 Version 1.1
---------------
- Nokia Asha software platform 1.0
- Tested to work on Nokia Asha 501
- Developed with Nokia Asha SDK 1.0

4.1.1 Known issues
------------------
- Tantalum5 does not seem to handle numerous concurrent web requests well
- The behavior of the spinner is sometimes erratic
- The text in the custom search bar is not visible on Series 40
- The navigation model brakes sometimes; exits the application with back key
  when in the details view

4.2 Version 1.0
---------------
- Series 40 touch platforms with CLDC 1.1 and MIDP 2.0
- Android API level 14 (Ice Cream Sandwich)
- Tested to work on the Nokia Asha 311, Nokia Asha 303, and Nokia X3-02
- Developed with NetBeans 7.0.1 and Nokia SDK 2.0 for Java

4.2.1 Known issues
------------------
- The Tantalum4 Series 40 image scaling has some minor glitches seen in bottom
  of the scaled images


5. Building, installing, and running the application
-------------------------------------------------------------------------------

5.1 Preparations
----------------
Check that you have Nokia Asha SDK 1.0 installed.

5.2 Packaging the application using Nokia Asha SDK 1.0
------------------------------------------------------
You cannot install the application on the device with the IDE, but you can 
package the application: After you have imported the project, locate the
Application Descriptor in the Package Explorer window and open it. Open the 
Overview tab (by default it is the first tab on the left) and click Create
package. Select the destination directory and click Finish.

5.3 Installing application binary to phone
------------------------------------------

Connect the phone to the computer with USB cable or Bluetooth. Locate the
application binary (.jar file). Copy the file to your phone, locate it and tap
to install. With Series 40 phone you can install the file using Nokia Suite:
Drag the file from the file explorer on top of the connected phone image in the
Nokia Suite window.

After the application is installed, locate the application icon from the
application menu and launch the application by selecting the icon.


6. Licence
-------------------------------------------------------------------------------

See the licence text file delivered with this project. The licence file is also
available online at
https://projects.developer.nokia.com/picasa_viewer/browser/picasa_viewer/Licence.txt


8. Related documentation
-------------------------------------------------------------------------------

Nokia Asha SDK:
- http://www.developer.nokia.com/Develop/Java/Tools/


The Tantalum API is open source and freely available in
- https://github.com/TantalumMobile/Tantalum


9. Version history
-------------------------------------------------------------------------------

1.1 Ported to Nokia Asha software platform 1.0 and Tantlum5 taken in use
1.0 Initial release
