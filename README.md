# Bloodhound
![](https://i.imgur.com/JpB9yI0.png)

Take control of your device without any of the privacy issues.

Bloodhound is free and open source device tracker for Android. With Bloodhound you can track your device's location, take secret pictures from both your front and back camera, 
listen in on your device's microphone, and set an alarm off even if your device is on silent. There's no worry about privacy issues either, Bloodhound is completely in your control.

You can set up Bloodhound to be connected to a Nextcloud account, or start the tracking from an SMS trigger. You can also set up an Emergency Tracking phone number, where you can enable Bloodhound from within the application and it will send the information to the number via SMS. So not only will your phone feel safer with Bloodhound installed, you'll feel safer too.

Notes:
I'm still working on the Nextcloud server side app / haven't decided if I want to build a Bloodhound server in NodeJS. Will wait until the school semester is finished though...
For right now you can place the "Bloodhound" directory in your root Nextcloud file directory. To enable the service, set the check binary file to 1 in "Bloodhound/Config" and enable or disable which tracking flags you want in the config.json file. The uploaded pictures, locations, and microphone recordings will be places in "Bloodhound/Pictures", "Bloodhound/Location", and "Bloodhound/Recordings" respectively.

Also the username and password is stored in plain text in the application preferences. While this is somewhat shameful it is only temporary...
