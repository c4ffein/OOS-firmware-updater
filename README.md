# OOS Firmware Updater

*Extract firmware from an OxygenOS ROM to a custom flashable `firmwareupdater.zip`*

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/packages/fr.witchdoctors.c4ffein.oosfirmwareextractor/)

* Built for and tested on Oneplus 5t (dumpling), but could work on other ROMs using the same file architecture
* Maybe the custom ROM you want to use already includes the latest radio/firmware.
  Maybe not, or maybe you want to use a specific firmware version, in which case this tool can be useful.
* Instructions
  * `SELECT ROM TO EXTRACT` - Select your OxygenOS zip
  * Wait for the MD5 check to appear
  * `EXTRACT` - Unzip to a temp directory, modifying the `updater-script` included in the ROM and only including some of the files to only install firmware
  * `CHECK` - Check the directory structure and the modified `updater-script` seem ok to you, as I can't guarantee the modifications that worked on my device's OxygenOS ROM structure will work on all future devices
  * `COOK` - Finally generate the `sdcard/firmwareupdater.zip` that you will be able to install from your recovery
* The extracted `sdcard/firmwareupdater.zip` enables you to update the firmware without having to re-flash your custom ROM.
  You should still keep a working previous version of `firmwareupdater.zip` or of OxygenOS to flash back just in case
  a change in the OxygenOS ROM directory structure broke compatibility with this tool
  and flashing the last generated firmware left your device in an unusable state.

## Contributing
- Don't hesitate to create an issue to notify me if this app is still working on devices older than OnePlus 5t so I can update this README
- The UX is really bad as I never expected people to actually use this app and only did it for myself at first.
  You can create an issue if you're still using this app and felt confuse at first launch, I might improve it.

## Security
- Obviously, you are going to flash probably closed-source firmware files, but at least it's from an official ROM.
- The `updater-script` generated by the app is executed using the original `update-binary` binary file extracted from the ROM.
  - Not that it matters since you are flashing firmware files from it anyway.
  - You can check [this repository](https://github.com/james34602/update-binary) in case you are curious of how this works.

## License
Some code snippets taken from stackoverflow.com and modified, credited them and released under MIT license as permitted.  
