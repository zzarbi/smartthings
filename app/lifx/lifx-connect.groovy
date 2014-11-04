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
    name: "LIFX (Connect)",
    namespace: "lifx",
    author: "Nicolas Cerveaux",
    description: "Allows you to connect your LIFX lights bulbs with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app. Adjust colors by going to the Thing detail screen for your LIFX lights (tap the gear on LIFX tiles).\n\nPlease update your LIFX Bridge first, outside of the SmartThings app, using the LIFX app.",
    category: "SmartThings Labs",
    iconUrl: "https://d21buns5ku92am.cloudfront.net/40204/logo/small-1385905812.png",
    iconX2Url: "https://d21buns5ku92am.cloudfront.net/40204/logo/small-1385905812.png"
)

preferences {
    page(name:"mainPage", title:"LIFX Device Setup", content:"mainPage", refreshTimeout:5)
    page(name:"bridgeDiscovery", title:"LIFX Bridge Discovery", content:"bridgeDiscovery", refreshTimeout:5)
    page(name:"bulbDiscovery", title:"LIFX Bulb Discovery", content:"bulbDiscovery", refreshTimeout:5)
}

private discoverBridges() {
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:MediaRenderer:1", physicalgraph.device.Protocol.LAN))
}

private discoverBulbs() {
    def bridge = getChildDevices()?.find {it.name == "LIFX Bridge"}
    
    if(bridge) {
        try{
            sendHubCommand(new physicalgraph.device.HubAction([
                method: "GET",
                path: "/lights",
                headers: [
                    HOST: getHostAddressFromDevice(bridge)
                ]
            ], bridge.deviceNetworkId))
        } catch(Exception e){
            log.debug "___exception: " + e
        }
    }
}

def mainPage() {
    if(canInstallLabs()) {
        def bridge = getChildDevices()?.find {it.name == "LIFX Bridge"}
        
        if (bridge) {
            if(!state.bridgeSubscribed){
                log.debug('subscribe to bridge')
                subscribe(bridge, "bulbsStatus", bulbsStatusHandler)
                subscribe(bridge, "bulbStatus", bulbStatusHandler)
                state.bridgeSubscribed = true
            }else{
                log.debug('Already subscribe to bridge')
            }
            return bulbDiscovery()
        } else {
            return bridgeDiscovery()
        }
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

def bridgeDiscovery(params=[:]) {
    int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
    state.refreshCount = refreshCount + 1
    def refreshInterval = 5

    log.debug "REFRESH COUNT :: ${refreshCount}"

    if(!state.subscribe) {
        subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribe = true
    }

    //ssdp request every 25 seconds
    if((refreshCount % 5) == 0) {
        discoverBridges()
    }

    def bridgesDiscovered = bridgesDiscovered()
    return dynamicPage(name:"bridgeDiscovery", title:"Brige Discovery Started!", nextPage:"bulbDiscovery", refreshInterval: refreshInterval, install:true, uninstall: selectedBridges != null) {
        section("Select a Bridge...") {
            input "selectedBridges", "enum", required:false, title:"Select Bridges \n(${bridgesDiscovered.size() ?: 0} found)", multiple:true, options:bridgesDiscovered
        }
    }
}

def bulbDiscovery(params=[:]) {
    def refreshInterval = 5
    
    discoverBulbs()

    def bulbsDiscovered = bulbsDiscovered()
    return dynamicPage(name:"bulbDiscovery", title:"Bulb Discovery Started!", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: selectedBulbs != null) {
        section("Select a Bulb...") {
            input "selectedBulbs", "enum", required:false, title:"Select Bulbs \n(${bulbsDiscovered.size() ?: 0} found)", multiple:true, options:bulbsDiscovered
        }
    }
}

def bridgesDiscovered() {
    def bridges = getBridges().findAll { it?.value?.verified == true }
    def map = [:]
    bridges.each {
        map["${it.value.ssdpUSN}"] = it.value.name
    }
    map
}

def bulbsDiscovered() {
    def bulbs = getBulbs()
    def map = [:]
    bulbs.each {
        map["${it.value.id}"] = it.value.label
    }
    map
}

def getBridges()
{
    if (!state.bridges) { state.bridges = [:] }
    state.bridges
}

def getBulbs()
{
    if (!state.bulbs) { state.bulbs = [:] }
    state.bulbs
}

def installed() {
    initialize()

    //runIn(5, "subscribeToDevices") //initial subscriptions delayed by 5 seconds
    //runIn(10, "refreshDevices") //refresh devices, delayed by 10 seconds
    //runIn(300, "doDeviceSync" , [overwrite: false]) //setup ip:port syncing every 5 minutes

    // SUBSCRIBE responses come back with TIMEOUT-1801 (30 minutes), so we refresh things a bit before they expire (29 minutes)
    //runIn(1740, "refresh", [overwrite: false])
}

def updated() {
    initialize()
    
    //runIn(5, "subscribeToDevices") //subscribe again to new/old devices wait 5 seconds
    //runIn(10, "refreshDevices") //refresh devices again, delayed by 10 seconds
}

def refresh() {
    log.debug "refresh() called"
}

def initialize() {
    // remove location subscription afterwards
    unsubscribe()
    state.subscribe = false

    if (selectedBridges) {
        addBridges()
    }
    
    if (selectedBulbs) {
        addBulbs()
    }
}

def addBridges() {
    def bridges = getBridges()

    selectedBridges.each { ssdpUSN ->
        // find corresponding bridge
        def selectedBridge = bridges."${ssdpUSN.toString()}"
        def dni
        def d
        if (selectedBridge) {
            dni = selectedBridge.ip+":"+selectedBridge.port
            // find childDevices
            d = getChildDevices()?.find {it.deviceNetworkId == dni}
        }
        
        // if device not foun
        if (!d) {
            // create device
            d = addChildDevice("lifx", "LIFX Bridge", dni, selectedBridge.hub, [
                "label": selectedBridge.name,
                "data": [
                    "mac": selectedBridge.mac,
                    "ip": selectedBridge.ip,
                    "port": selectedBridge.port
                ]
            ])
        }
    }
}

def addBulbs() {
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
            d = addChildDevice("lifx", "LIFX Bulb", id, selectedBulb.hub, [
                "label": selectedBulb.label,
                "data": [
                     "site_id": selectedBulb.site_id
                 ]
            ])
        }
    }
}

//Handles events to add new bulbs
def bulbsStatusHandler(evt) {
    log.info "Bulbs Status"
    
    def bulbs = getBulbs()
    evt.jsonData.each { bulb ->
       bulb.hub = evt?.hubId
       if (!(bulbs."${bulb.id.toString()}")) { //if it doesn't already exist
           bulbs << ["${bulb.id.toString()}":bulb]
           log.debug("Add new bulb ${bulb.id}")
       } else { // just update the values
           log.debug "Update bulb ${bulb.id}"
       }
    }
}

def bulbStatusHandler(evt) {
    log.info "Bulb Status"
}

def locationHandler(evt) {
    if(evt.name == "ping") {
        return ""
    }
    
    if(evt.description.contains('urn:schemas-upnp-org:device:MediaRenderer:')){
        log.debug("Found a device")
        
        def device = [:]
        def parts = evt.description.split(',')
        parts.each { part ->
            part = part.trim()
            if (part.startsWith('mac:')) {
                def valueString = part.split(":")[1].trim()
                if (valueString) {
                    device.mac = valueString
                }
            } else if (part.startsWith('networkAddress:')) {
                def valueString = part.split(":")[1].trim()
                if (valueString) {
                    device.ip = valueString
                }
            } else if (part.startsWith('deviceAddress:')) {
                def valueString = part.split(":")[1].trim()
                if (valueString) {
                    device.port = valueString
                }
            }else if (part.startsWith('ssdpUSN:')) {
                part -= "ssdpUSN:"
                def valueString = part.trim()
                if (valueString) {
                    device.ssdpUSN = valueString
                }
            }
        }
        device.hub = evt?.hubId
        device.name = "LIFX Bridge"
        device.verified = true
        
        // override the port to 56780
        device.port = convertPortToHex(56780);
        
        def briges = getBridges()
        if (!(bridges."${device.ssdpUSN.toString()}")) { //if it doesn't already exist
            bridges << ["${device.ssdpUSN.toString()}":device]
            log.debug("Add new bridge ${device.ssdpUSN}")
        } else { // just update the values
            log.debug "Update bridge ${device.ssdpUSN}"

            def d = bridges."${device.ssdpUSN.toString()}"
            if(d.ip != device.ip || d.port != device.port) {
                d.ip = device.ip
                d.port = device.port
                log.debug "Bridge's port or ip changed..."
                
                /*def children = getChildDevices()
                log.debug "Found children ${children}"
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        log.debug "updating ip and port, and resubscribing, for device ${it} with mac ${parsedEvent.mac}"
                        it.subscribe(parsedEvent.ip, parsedEvent.port)
                    }
                }*/
            }
        }
    }
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddressFromDevice(device) {
    def parts = device.deviceNetworkId.split(":")
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04X', port)
    return hexport
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


/* Hook for child devices */
def poll(childDevice) {
    log.debug "Executing 'poll'"
}

def setAdjustedColor(childDevice, value) {
    log.debug "Executing 'setAdjustedColor'"
}

def refresh(childDevice) {
    log.debug "Executing 'refresh'"
}

def setLevel(childDevice, double value) {
    log.debug "Executing 'setLevel'"
}

def on(childDevice) {
    log.debug "Executing 'on'"
}

def off(childDevice) {
    log.debug "Executing 'off'"
}
