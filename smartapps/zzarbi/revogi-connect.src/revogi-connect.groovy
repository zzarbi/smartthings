/**
 *  Revogi Connect
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
definition(
    name: "Revogi Connect",
    namespace: "zzarbi",
    author: "Nicolas Cerveaux",
    description: "Allows you to integrate your Revogi Devices to SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://pbs.twimg.com/profile_images/534589970405543936/w63OE06d_normal.jpeg",
    iconX2Url: "https://pbs.twimg.com/profile_images/534589970405543936/w63OE06d_bigger.jpeg"
) {
    appSetting "debug"
}

import groovy.json.JsonSlurper

preferences {
    page(name: "home", install: false, uninstall: true, nextPage: null) {
        section() {
            href "setupSPSPage", title:"Add a Smart Power Strip", description: "(Compatible with SOW321)"
        }
    }

    page(name: "settingsPage")
    page(name: "setupSPSPage")
    page(name: "discoverSPSpage")
    page(name: "nextPage")
}

def settingsPage() {
    dynamicPage(name: "settingsPage", title: "Settings", install: false, uninstall: true, nextPage: "home") {
        section() {
            input "debug", title: "Debug Mode", "bool", required: true, defaultValue: false
        }
    }
}

def setupSPSPage() {
    // reset counter to 0
    state.refreshCount = 0

    if(canInstallLabs()) {
        dynamicPage(name: "setupSPSPage", title: "Smart Power Strip Setup", install: false, uninstall: true, nextPage: "discoverSPSpage") {
            section() {
                input "spsIP", title: "Enter the IP of the device", "text", required: true, defaultValue: "192.168."
            }
        }
    } else {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"setupSPSPage", title:"Upgrade needed!", nextPage:home, install:false, uninstall: true) {
            section("Upgrade") {
                paragraph "$upgradeNeeded"
            }
        }
    }
}

def discoverSPSpage() {
    int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
    state.refreshCount = refreshCount + 1
    def refreshInterval = 5

    if(!state.subscribe) {
        debug("Subscribe to location")
        // subscribe to answers from HUB
        subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribe = true
    }

    debug("REFRESH COUNT :: ${refreshCount}")
    debug("newIp:"+ spsIP)

    if(((refreshCount % 1) == 0) && ((refreshCount % 5) != 0)) {
        verifyDevices(spsIP)
    }

    def revogiSPS = getRevogiSPS()

    // if discovered device no need to refresh
    if (revogiSPS.size() > 0){
        return dynamicPage(name:"discoverSPSpage", title:"Device Found", nextPage:"home", install:true, uninstall: true) {
            section() {
                paragraph "Device found on IP $spsIP !\nTo Finish this setup click on \"Done\"."
            }
        }
    }

    return dynamicPage(name:"discoverSPSpage", title:"Verification in progress", nextPage:null, refreshInterval: refreshInterval, install:false, uninstall: true) {
        section() {
            paragraph "The device is currently being verified with IP $spsIP"
            href "setupSPSPage", title:"Change IP", description: null
        }
    }
}

def nextPage() {
    home()
}

private debug(data) {
    if(appSettings.debug == "true" || appSettings.debug){
        log.debug(data)
    }
}

private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private verifyDevices(ip) {
    def deviceNetworkId= convertIPtoHex(ip)+":"+convertPortToHex("80")
    debug("Verify Device via ${ip} and ${deviceNetworkId}")

    sendHubCommand(new physicalgraph.device.HubAction("""GET /?cmd=511 HTTP/1.1
HOST: ${deviceNetworkId}

""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

def getRevogiSPS() {
    if (!state.smartPowerStrips) {
        state.smartPowerStrips = [:]
    }
    return state.smartPowerStrips
}

def locationHandler(evt) {
    if(evt.name == "ping") {
        return ""
    }

    def request = parseDiscoveryMessage(evt.description)
    def headers = ""
    def body = ""
    def hub = evt?.hubId
    request << ["hub":hub] // add the hub in the request

    if (request.headers) {
        headers = request.headers
    }

    if (request.body) {
        body = request.body
    }

    debug("Headers: ${headers}")
    debug("body: ${body}")

    // if it's an answer of cmd 511
    if (body.contains("\"response\":511")) {
        def revogiSPS = getRevogiSPS()
        if (!(revogiSPS."${request.mac.toString()}")) { //if it doesn't already exist
            debug("New device discovered")
            revogiSPS << ["${request.mac.toString()}":request]
        } else { // just update the values
            debug("Device already discovered - Update")
            revogiSPS."${request.mac.toString()}" = request
        }
    }
}

def installed() {
    debug("Installed with settings: ${settings}")
    initialize()
}

def uninstalled() {
    debug("Uninstalling, removing child devices...")
    removeChildDevices(getChildDevices())
}

private removeChildDevices(devices) {
    devices.each {
        try {
            deleteChildDevice(it.deviceNetworkId) // 'it' is default
        } catch (physicalgraph.exception.ConflictException e) {
            throw e
        } catch (Exception e) {
            debug("___exception: " + e)
        }
    }
}

def updated() {
    debug("Updated with settings: ${settings}")
    initialize()
}

def addRevogiSPS() {
    def revogiSPS = getRevogiSPS()

    if(!state.installedSwitch){
        state.installedSwitch = [:]
    }

    revogiSPS.each { dni, value ->
        // decode data
        def jsonData = new JsonSlurper().parseText(value.body)

        if (jsonData.data."switch".size() > 0){
            debug("Setting Up " + jsonData.data."switch".size() + " switches")

            // adding all switches
            for (def i = 0;i<jsonData.data."switch".size();i++) {
                debug("Switch #"+i)
                def deviceID = dni + ":"+i;
                deviceID = new String(deviceID.encodeAsBase64())

                if(!state.installedSwitch."$deviceID") {
                    def d = getChildDevices()?.find {
                        it.dni == deviceID
                    }

                    if (!d) { // only add if not added already
                        def data  = [
                             "label": "Smart Power Strip Switch #"+(i+1),
                             "data": [
                                 "mac": value.mac,
                                 "ip": value.ip,
                                 "port": value.port,
                                 "switch": i,
                                 "master": false
                             ]
                         ]

                        debug("Adding Device Data: " + data)

                        try {
                            state.installedSwitch."$deviceID" = true;
                            d = addChildDevice("zzarbi", "Revogi Smart Power Strip Switch", deviceID, value.hub, data)
                        }catch(Exception e){
                            debug("___exception: " + e)
                        }
                    }
                }
            }
        }

        def bridge = getChildDevices()?.find {
            it.dni == dni
        }

        if(!state.installedSwitch."$dni"){
            // adding the bridges itself
            if (!bridge) { // only add if not added already
                def data  = [
                     "label": "Smart Power Strip",
                     "data": [
                         "mac": value.mac,
                         "ip": value.ip,
                         "port": value.port,
                         "master": true
                     ]
                ]

                debug("Adding Device Data: " + data)

                try {
                    state.installedSwitch."$dni" = true;
                    bridge = addChildDevice("zzarbi", "Revogi Smart Power Strip", dni, value.hub, data)
                }catch(Exception e){
                    debug("___exception: " + e)
                }
            }
        }
    }
}

def initialize() {
    debug("Initialiaze")
    // remove location subscription afterwards
    unsubscribe()
    state.subscribe = false
    state.refreshScheduler = false

    def revogiSPS = getRevogiSPS()

    // add device if it's found
    if(revogiSPS.size() > 0) {
        addRevogiSPS()

        if (!state.refreshScheduler) {
            // schedule from every minute
            schedule("0 * * * * ?", spsRefreshScheduler)
            state.refreshScheduler = true
        }
    }
}

def spsRefreshScheduler(evt) {
    debug("Running scheduler - Revogi Smart Power Strip")
    def spsBridge = getChildDevices()?.find {it.name == "Revogi Smart Power Strip"}

    if (spsBridge) {
        spsBridge.refresh()
    }
}

private def parseDiscoveryMessage(String description) {
    debug("parseDiscoveryMessage: ${description}")
    def request = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                request.mac = valueString
            }
        } else if (part.startsWith('ip:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                request.ip = valueString
            }
        } else if (part.startsWith('port:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                request.port = valueString
            }
        } else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                request.headers = new String(valueString.decodeBase64())
            }
        } else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                request.body = new String(valueString.decodeBase64())
            }
        }
    }

    request
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