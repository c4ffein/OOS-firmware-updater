# OOS Firmware Updater

*Extract firmware from an OxygenOS ROM to a custom flashable `firmwareupdater.zip`*

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/packages/fr.witchdoctors.c4ffein.oosfirmwareextractor/)

* Built for and tested on Oneplus 5t (dumpling), but could work on other ROMs using the same file architecture
* Instructions
  * `SELECT ROM TO EXTRACT` - Select your OxygenOS zip
  * Wait for the MD5 check to appear
  * `EXTRACT` - Unzip to a temp directory, modifying the `updater-script` included in the ROM and only including some of the files to only install firmware
  * `CHECK` - Check the directory structure and the modified `updater-script` seem ok to you, as I can't guarantee the modifications that worked on my device's OxygenOS ROM structure will work on all future devices
  * `COOK` - Finally generate the `sdcard/firmwareupdater.zip` that you will be able to install from your recovery

## Contributing
- Don't hesitate to create an issue to notify me if this app is still working on devices older than OnePlus 5t so I can update this README
- The UX is really bad as I never expected people to actually use this app and only did it for myself at first.
  You can create an issue if you're still using this app and felt confuse at first launch, I might improve it.

## License
Some code snippets taken from stackoverflow.com and modified, credited them and released under MIT license as permitted.  
