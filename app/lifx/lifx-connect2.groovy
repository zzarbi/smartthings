/**
 *  Lifx (Connect)
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
    namespace: "lifx",
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
    page(name:"bridgeDiscovery", title:"LIFX Bridge Discovery", content:"bridgeDiscovery", refreshTimeout:5)
    page(name:"bulbDiscovery", title:"LIFX Bulb Discovery", content:"bulbDiscovery", refreshTimeout:5)
}

private debug(data) {
    if(appSettings.debug == "true"){
        log.debug(data)
    }
}

private discoverBulbs() {
    debug(location)
    debug("Start Discovery of LIFX Bulb using access_token="+appSettings.accessToken)
    def pollParams = [
        uri: "https://api.lifx.com:443",
        path: "/v1beta1/lights.json?access_token="+appSettings.accessToken,
        headers: ["Content-Type": "text/json", "Authorization": "Bearer ${appSettings.accessToken}"],
        query: [format: 'json', body: jsonRequestBody]
    ]
    
    try{
        httpGet(pollParams) { resp ->            
            if(resp.status == 200) {
                if (resp.data) {
                    def bulbs = getBulbs()
                    
                    resp.data.each() { bulb ->
                        if (!(bulbs."${bulb.id.toString()}")) { //if it doesn't already exist
                            bulbs << ["${bulb.id.toString()}":bulb]
                            debug("Found new bulb ${bulb.id}")
                        } else { // just update the values
                            debug("Bulb already been found ${bulb.id}")
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
        
        return bulbDiscovery()
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

def bulbDiscovery(params=[:]) {
    def refreshInterval = 5
    
    discoverBulbs()

    def bulbsDiscovered = bulbsDiscovered()
    return dynamicPage(name:"bulbDiscovery", title:"LIFX Discovery Started!", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: selectedBulbs != null) {
        section("Select a bulb to add...") {
            input "selectedBulbs", "enum", required:false, title:"LIFX Bulb \n(${bulbsDiscovered.size() ?: 0} found)", multiple:true, options:bulbsDiscovered
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

private getBulbs() {
    if (!state.bulbs) { state.bulbs = [:] }
    state.bulbs
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
        
        if(state.statusScheduled == false) {
            // schedule from every 5 minute
            schedule("0 0/5 * * * ?", "pollChildrenHandler")
            state.statusScheduled = true
            pollChildrenHandler()
        }
    }
}

def pollChildrenHandler() {
    debug("Poll Children")
    getChildDevices().each {
        it.poll()
    }
}

private addBulbs() {
    def bulbs = getBulbs()

    selectedBulbs.each { id ->
        // find corresponding bridge
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

private removeChildDevices(children)
{
    debug("Deleting ${children.size()} LIFX Bulb")
    children.each {
        debug("Delete LIFX Bulb.id=${it.deviceNetworkId}")
        deleteChildDevice(it.deviceNetworkId)
    }
}

private Boolean canInstallLabs()
{
    return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
    return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
    return location.hubs*.firmwareVersionString.findAll { it }
}
