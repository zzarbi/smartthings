# SmartThings

This repository will contain all the work I've done related to the SmartThings Platform. There are currently support for the following.

## Setup

1. Setup GitHub integration with this repository on the [My Device Handlers](https://graph.api.smartthings.com/ide/devices) page by selecting Settings, add new repository and then entering `zzarbi`, `smartthings`, and `master` in the three fields (more details at [GitHub integration](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html)).
1. Add device handlers on the [My Device Handlers](https://graph.api.smartthings.com/ide/devices) page and SmartApps on the [My SmartApps](https://graph.api.smartthings.com/ide/apps) page. To add a device handler or SmartApp select Update from Repo then select smartthings (master). Check the boxes you want to add then select the publish checkbox and Execute Update.
1. Follow directions below for your device and to know which boxes to check for each device.

### WeMo Insight Switch

Follow these directions to add support for WeMo Insight Switches to your SmartThings setup:

1. Setup your Insight Switch in the WeMo app.
1. Add the `wemo-insight-switch.groovy` device handler and `wemo-insight-connect.groovy` SmartApp.
1. Using the SmartThings App, go to "SmartThings Labs" and run the WeMo app.

### LIFX Cloud

Follow these directions to add support for LIFX Cloud to your SmartThings setup:

1. Setup your LIFX Bulb with the LIFX app, make sure they're accessible in LIFX cloud.
1. Add the `lifx-bulb-2.groovy` and `lifx-group-2.groovy` device handlers and `lifx-connect2.groovy` SmartApp.
1. Using the SmartThings App, go to "SmartThings Labs" and run the LIFX Connect app.

### Revogi Smart Power Strip

Follow these directions to add support for Revogi Smart Power Strip to your SmartThings setup:

1. Setup your Revogi Device in the Revogi app.
1. Add the `revogi-sps.groovy` and `revogi-sps-switch.groovy` device handlers and `revogi-connect.groovy` SmartApp.
1. Using the SmartThings App, go to "My Apps" and run the Revogi Connect app
