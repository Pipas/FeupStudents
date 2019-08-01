# FEUP for Students

A mobile app to access FEUP's SIGARRA website optimized for mobile devices.

## Download
<a href="https://play.google.com/store/apps/details?id=software.pipas.feupstudents">
  <img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
       alt="Android app on Google Play" width="200"/>
</a>

## Features
* Optimized navigation and display for mobile devices
* Auto login
* Add and manage favorite pages

## Security
The app doesn't access any info that is being displayed on the website, the website is fully loaded and then CSS code is injected to the webview in order to change the display style.

The auto login feature works by checking if the user is not logged in and using the users encrypted details stored on device to login using a simple JavaScript function.

**All user data is encrypted and kept on device.** Encryption is handed by the library [Qlassified Android](https://github.com/Q42/Qlassified-Android) that is itself a wrapper for the [Android Keystore System](https://developer.android.com/training/articles/keystore.html). The developer of this app has no access to such data and they are not uploaded to any servers or remote locations.

## Libraries
 * [AHBottomNavigation](https://github.com/aurelhubert/ahbottomnavigation) -  aurelhubert
 * [MaterialDrawer](https://github.com/mikepenz/MaterialDrawer) - mikepenz
 * [LovelyDialog](https://github.com/yarolegovich/LovelyDialog) - yarolegovich
 * [Qlassified Android](https://github.com/Q42/Qlassified-Android) - Q42

## Contributing
All code for the app is available and open source in this repository, feel free to fork and submit pull requests for review.
The project was made in about a week so no rigorous testing or coding guidelines are being followed.

## Author
 * **Paulo Correia** - MIEIC FEUP
 
 App not affiliated with FEUP or Universidade do Porto.
