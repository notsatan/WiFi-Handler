# WiFi-Handler
App to help people switch between using Wifi and Wifi Hotspot.

### How do I use this?
- Download the APK version of the app from [here](/../../releases)
- Run the app. The app will guide you from there.

## Why make this app?
Its made to help people switch between using Wifi and Wifi-Hotspot without having to go through the system settings to locate the two. In prior versions of Android, the app will switch the hotspot on/off by itself. For the newer versions, the app will directly send the user to the relevant settings page.

## How do I modify the app myself?
  Uhhh.
- Pull the entire project on your local PC.
- In Android Studio (or anything else), goto `File -> Open` and then navigate to the location where you pulled this project.
- No additonal dependicies or libraries are required to run the project.

## The app doesn't work for Hotspot in my device (panicks).
The intent required to navigate to the Wifi Hotspot section in settings can vary from one manufacturer to another. So, if the hotspot settings page in your phone doesn't open up directly, or if you end up somewhere other than Hotspot Settings, blame your manufacturer ;) 

As such, the application is using the generic android intent for navigating to the Hotspot settings, but even that can cause problems in specific devices.

## I want memes. Gib memes.
Alright here you go.

![Java Meme](https://i.imgur.com/4QRTsR2.jpg)

## Licensing
This project is licensed under [Mozilla Public License 2.0](/LICENSE)
