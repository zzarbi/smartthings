# LIFX Cloud setup

Follow these directions to add support for LIFX Cloud to your SmartThings setup:

1. Setup your LIFX Bulb with the LIFX app, make sure they're accessible in LIFX cloud
1. Add the [device script](lifx/lifx-bulb-2.groovy) to your list of [Device Types](https://graph.api.smartthings.com/ide/devices) (need to be a developer)
1. Add the [device script](lifx/lifx-group-2.groovy) to your list of [Device Types] as well if you want supports for groups
1. Add the [app script](lifx/lifx-connect2.groovy) to [your list of SmartApps](https://graph.api.smartthings.com/ide/apps), set it to your home location
1. Using the SmartThings App, go to "SmartThings Labs" and run the LIFX Connect app
