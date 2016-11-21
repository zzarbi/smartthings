/**
 *  LIFX (Connect)
 *  Source: https://github.com/zzarbi/smartthings
 *
 *  Copyright 2014 Nicolas Cerveaux
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.json.JsonBuilder

definition(
    name: "LIFX 2.0",
    namespace: "zzarbi",
    author: "Nicolas Cerveaux",
    description: "Allows you to connect your LIFX lights bulbs with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app. Adjust colors by going to the Thing detail screen for your LIFX lights (tap the gear on LIFX tiles).\n\nPlease update your LIFX Bridge first, outside of the SmartThings app, using the LIFX app.",
    category: "SmartThings Labs",
    iconUrl: "https://d21buns5ku92am.cloudfront.net/40204/logo/small-1385905812.png",
    iconX2Url: "https://d21buns5ku92am.cloudfront.net/40204/logo/small-1385905812.png"
) {
    appSetting "accessToken"
    appSetting "debug"
}

preferences {
    page(name:"mainPage", title:"LIFX Device Setup", content:"mainPage", refreshTimeout:5)
    page(name:"lifxDiscovery", title:"LIFX Bulb Discovery", content:"lifxDiscovery", refreshTimeout:5)
}

private debug(data) {
    if(appSettings.debug == "true"){
        log.debug(data)
    }
}

private discoverLifx() {
    debug(location)
    debug("Start Discovery of LIFX Bulb using access_token="+appSettings.accessToken)
    def pollParams = [
        uri: "https://api.lifx.com",
        path: "/v1beta1/lights.json?access_token="+appSettings.accessToken,
        headers: ["Content-Type": "text/json", "Authorization": "Bearer ${appSettings.accessToken}"],
        query: [format: 'json', body: jsonRequestBody]
    ]

    try{
        httpGet(pollParams) { resp ->
            if(resp.status == 200) {
                if (resp.data) {
                    def bulbs = getBulbs()
                    def groups = getGroups()

                    resp.data.each() { bulb ->
                        // add Bulbs
                        if (!(bulbs."${bulb.id.toString()}")) { // if it doesn't already exist
                            bulbs << ["${bulb.id.toString()}":bulb]
                            debug("Found new bulb ${bulb.id}")
                        } else { // just update the values
                            bulbs["${bulb.id.toString()}"] = bulb;
                            debug("Updating bulb ${bulb.id}")
                        }

                        // add Groups
                        if (!(groups."${bulb.group.id.toString()}")) { // if it doesn't already exist
                            groups << ["${bulb.group.id.toString()}":bulb.group]
                            debug("Found new group ${bulb.group.id}")
                        } else { // just update the values
                            groups["${bulb.group.id.toString()}"] = bulb.group;
                            debug("Updating group ${bulb.group.id}")
                        }
                    }
                }
            } else {
                debug("Error while Discovering LIFX Bulb")
            }
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}

def mainPage() {
    if(canInstallLabs()) {
        if(appSettings.accessToken == ''){
            def upgradeNeeded = "To use LIFX 2.0 you will need an access_token Labs"
            return dynamicPage(name:"mainPage", title:"Access Token needed!", nextPage:"", install:false, uninstall: true) {
                section("Access Token") {
                    paragraph "$upgradeNeeded"
                }
            }
        }

        return lifxDiscovery()
    } else {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"mainPage", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
            section("Upgrade") {
                paragraph "$upgradeNeeded"
            }
        }
    }
}

def lifxDiscovery(params=[:]) {
    def refreshInterval = 5

    discoverLifx()

    def bulbsDiscovered = bulbsDiscovered()
    def groupsDiscovered = groupsDiscovered()
    return dynamicPage(name:"lifxDiscovery", title:"LIFX Discovery Started!", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: (selectedBulbs != null || selectedGroups!= null)) {
        section("Select a bulb to add...") {
            input "selectedBulbs", "enum", required:false, title:"LIFX Bulbs \n(${bulbsDiscovered.size() ?: 0} found)", multiple:true, options:bulbsDiscovered
            input "selectedGroups", "enum", required:false, title:"LIFX Groups \n(${groupsDiscovered.size() ?: 0} found)", multiple:true, options:groupsDiscovered
        }
    }
}

def bulbsDiscovered() {
    def bulbs = getBulbs()
    def map = [:]
    bulbs.each {
        map["${it.value.id}"] = it.value.label
    }
    map
}

def groupsDiscovered() {
    def groups = getGroups()
    def map = [:]
    groups.each {
        map["${it.value.id}"] = it.value.name
    }
    map
}

private getBulbs() {
    if (!state.bulbs) { state.bulbs = [:] }
    state.bulbs
}

private getGroups() {
    if (!state.groups) { state.groups = [:] }
    state.groups
}

def installed() {
    initialize()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

def updated() {
    initialize()
}

def refresh() {
    debug("refresh() called")
}

def initialize() {
    debug("Initialize LIFX 2.0")

    // remove schedule
    unschedule()
    state.statusScheduled = false

    if (selectedBulbs) {
        addBulbs()
    }

    if(selectedGroups) {
        addGroups()
    }

    if((selectedBulbs || selectedGroups) && state.statusScheduled == false) {
        // schedule from every 5 minute
        schedule("0 0/5 * * * ?", "pollChildrenHandler")
        schedule("0 0/15 * * * ?", "checkBulbConnectionStatus")
        state.statusScheduled = true
        pollChildrenHandler()
    }
}

def pollChildrenHandler() {
    debug("Poll Children")
    getChildDevices().each {
        it.poll()
    }
}

def checkBulbConnectionStatus() {
    debug("Check checkBulbConnectionStatus")
    def foundError = false
    def bulbWithErrors = []
    def dateLastError = null
    def today = new Date()

    if(state.lastError != 0){
        dateLastError = new Date((long)state.lastError)
    }

    // call dicoverBulb
    discoverLifx();
    getBulbs().each { id, it ->
        if(!it.connected){
            foundError = true
            bulbWithErrors << it.label
        }
    }

    if(foundError){
        if(dateLastError == null || dateLastError.format('MM/dd') != today.format('MM/dd')){
            state.lastError = now()
            debug("Send Notification")
            try{
                sendNotification('Following LIFX Bulb ('+bulbWithErrors.join(',')+') are disconnected, please reboot them')
            }catch(Exception e){}
        }else{
            debug("Notification already sent today")
        }
    }else{
        state.lastError = 0
    }
}

private addBulbs() {
    def bulbs = getBulbs()

    selectedBulbs.each { id ->
        // find corresponding bulb
        def selectedBulb = bulbs."${id.toString()}"
        def d
        if (selectedBulb) {
            // find childDevices
            d = getChildDevices()?.find {it.deviceNetworkId == id}
        }

        // if device not found
        if (!d) {
            // create device
            debug("Add LIFX Bulb ${selectedBulb.label}")
            d = addChildDevice("lifx", "LIFX Bulb", id, null, [
                "name": selectedBulb.label,
                "label": selectedBulb.label
            ])
        }
    }
}

private addGroups() {
    def groups = getGroups()

    selectedGroups.each { id ->
        // find corresponding bulb
        def selectedGroup = groups."${id.toString()}"
        def d
        if (selectedGroup) {
            // find childDevices
            d = getChildDevices()?.find {it.deviceNetworkId == id}
        }

        // if device not found
        if (!d) {
            // create device
            debug("Add LIFX Group ${selectedGroup.name}")
            d = addChildDevice("lifx", "LIFX Group", id, null, [
                "name": selectedGroup.name + " Group",
                "label": selectedGroup.name + " Group"
            ])
        }
    }
}

private removeChildDevices(children) {
    debug("Deleting ${children.size()} LIFX Bulb")
    children.each {
        debug("Delete LIFX Bulb.id=${it.deviceNetworkId}")
        deleteChildDevice(it.deviceNetworkId)
    }
}

private Boolean canInstallLabs() {
    return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware) {
    return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions() {
    return location.hubs*.firmwareVersionString.findAll { it }
}