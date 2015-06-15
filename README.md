SmartThings
===========

This repository will contain all the work I've done related to the SmartThings Platform.

WeMo Insight Switch setup
=========================

Follow these directions to add support for WeMo Insight Switches to your SmartThings setup:

1. Setup your Insight switch in the WeMo App
2. Add the [device script](device/wemo/wemo-insight-switch.groovy) to your list of [Device Types](https://graph.api.smartthings.com/ide/devices) (need to be a developer)
3. Add the [app script](app/wemo/wemo-insight-connect.groovy) to [your list of SmartApps](https://graph.api.smartthings.com/ide/apps), set it to your home location (need to be a developer)
4. Using the SmartThings App, go to "SmartThings Labs" and run the WeMo app
