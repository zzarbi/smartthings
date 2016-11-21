/**
 *  WD Player Connect
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
    name: "WD Player Connect",
    namespace: "zzarbi",
    author: "Nicolas Cerveaux",
    description: "Allows you to integrate your WD Devices with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "http://www.emerce.nl/content/uploads/2014/06/wd_logo.png",
    iconX2Url: "http://www.emerce.nl/content/uploads/2014/06/wd_logo.png"
)

preferences {
    page(name:"firstPage", title:"WD Devices Setup", content:"firstPage")
}

private discoverAllWDDevices() {
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:MediaRenderer:1", physicalgraph.device.Protocol.LAN))
}

private getFriendlyName(String deviceNetworkId) {
    sendHubCommand(new physicalgraph.device.HubAction([
        method: "GET",
        path: "/",
        headers: [
            HOST: ipAddressFromDni(deviceNetworkId)
        ]], deviceNetworkId)
    )
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private ipAddressFromDni(deviceNetworkId) {
    def parts = deviceNetworkId.split(":")
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    //log.debug "Using ip: ${ip} and port: ${port} for device: ${deviceNetworkId}"
    return ip + ":" + port
}

private verifyDevices() {
    //log.debug("Verify Devices")
    def wdDevices = getWdDevices().findAll { it?.value?.verified != true }
    wdDevices.each {
        getFriendlyName((it.value.ip + ":" + it.value.port))
    }
}

def firstPage() {
    if(canInstallLabs()) {
        int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
        state.refreshCount = refreshCount + 1
        def refreshInterval = 5

        //log.debug "REFRESH COUNT :: ${refreshCount}"

        if(!state.subscribe) {
            // subscribe to answers from HUB
            subscribe(location, null, locationHandler, [filterEvents:false])
            state.subscribe = true
        }

        //ssdp request every 25 seconds
        if((refreshCount % 5) == 0) {
            discoverAllWDDevices()
        }

        //setup.xml request every 5 seconds except on discoveries
        if(((refreshCount % 1) == 0) && ((refreshCount % 5) != 0)) {
            verifyDevices()
        }

        def wdDeviceDiscovered = wdDeviceDiscovered()

        return dynamicPage(name:"firstPage", title:"Discovery Started!", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: selectedPlayers != null) {
            section("Select a Player...") {
                input "selectedDevices", "enum", required:false, title:"WD Devices \n(${wdDeviceDiscovered.size() ?: 0} found)", multiple:true, options:wdDeviceDiscovered
            }
        }
    } else {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"firstPage", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
            section("Upgrade") {
                paragraph "$upgradeNeeded"
            }
        }
    }
}

def wdDeviceDiscovered() {
    //log.debug("Dicovered Player")
    def wdDevices = getWdDevices().findAll { it?.value?.verified == true }
    def map = [:]
    wdDevices.each {
        def value = it.value.name ?: "WD Device ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
        def key = it.value.mac
        map["${key}"] = value
    }
    map
}

def getWdDevices() {
    if (!state.wdDevices) { state.wdDevices = [:] }
    state.wdDevices
}

def installed() {
    //log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    //log.debug "Updated with settings: ${settings}"
    initialize()
}

def resubscribe() {
    refresh()
}

def refresh() {
    refreshDevices()
}

def refreshDevices() {
    def devices = getAllChildDevices()
    devices.each { d ->
        d.refresh()
    }
}

def subscribeToDevices() {
    //log.debug "subscribeToDevices() called"
    def devices = getAllChildDevices()
    devices.each { d ->
        //log.debug('Call subscribe on '+d.id)
        d.subscribe()
    }
}

def addDevices() {
    def wdDevices = getWdDevices()

    selectedDevices.each { dni ->
        def selectedDevice = wdDevices.find { it.value.mac == dni } ?: wdDevices.find { "${it.value.ip}:${it.value.port}" == dni }

        def d
        if (selectedDevice) {
            d = getChildDevices()?.find {
                it.dni == selectedDevice.value.mac || it.device.getDataValue("mac") == selectedDevice.value.mac
            }
        }

        if (!d) {
            log.debug("Add device")
            try{
                d = addChildDevice("wd", selectedDevice.value.model, selectedDevice.value.mac, selectedDevice.value.hub, [
                    "label": selectedDevice.value.name ?: "Unknow WD Device",
                    "data": [
                        "mac": selectedDevice.value.mac,
                        "ip": selectedDevice.value.ip,
                        "port": selectedDevice.value.port
                    ]
                ])
            }catch(Exception e){
                log.error("Cannot add Device: "+e)
            }
        }
    }
}

def initialize() {
    // remove location subscription afterwards
    unsubscribe()
    state.subscribe = false

    if (selectedDevices) {
        addDevices()
    }

    // run once subscribeToDevices
    subscribeToDevices()

    //setup cron jobs
    schedule("10 * * * * ?", "subscribeToDevices")
}

def locationHandler(evt) {
    if(evt.name == "ping") {
        return ""
    }

    def description = evt.description
    def hub = evt?.hubId
    def parsedEvent = parseDiscoveryMessage(description)
    parsedEvent << ["hub":hub]

    if (parsedEvent?.ssdpTerm?.contains("device:MediaRenderer:1")) {
        def wdDevices = getWdDevices()

        if (!(wdDevices."${parsedEvent.ssdpUSN.toString()}")) { //if it doesn't already exist
            wdDevices << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
        } else { // just update the values
            def d = wdDevices."${parsedEvent.ssdpUSN.toString()}"
            boolean deviceChangedValues = false

            if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
                d.ip = parsedEvent.ip
                d.port = parsedEvent.port
                deviceChangedValues = true
            }

            if (deviceChangedValues) {
                def children = getChildDevices()
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        it.subscribe(parsedEvent.ip, parsedEvent.port)
                    }
                }
            }

        }
    } else if (parsedEvent.headers && parsedEvent.body) {
        def headers = new String(parsedEvent.headers.decodeBase64())
        // if request is XML
        if(headers.contains("text/xml")){
            def bodyString = new String(parsedEvent.body.decodeBase64())
            def body = new XmlSlurper().parseText(bodyString)

            // if the xml containt data about the device
            if (body?.device?.deviceType?.text().contains("device:MediaRenderer:1") && body?.device?.manufacturer?.text().contains("Western Digital Corporation")) {
                def wdDevices = getWdDevices();
                def wdDevice = wdDevices.find {it?.key?.contains(body?.device?.UDN?.text())}
                if (wdDevice) {
                    wdDevice.value << [name:body?.device?.friendlyName?.text(), model:body?.device?.modelName?.text(), verified: true]
                } else {
                    log.error "/setup.xml returned a wemo device that didn't exist"
                }
            }
        }
    }
}

private def parseDiscoveryMessage(String description) {
    def device = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            device.devicetype = valueString
        } else if (part.startsWith('mac:')) {
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
        } else if (part.startsWith('ssdpPath:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ssdpPath = valueString
            }
        } else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpUSN = valueString
            }
        } else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpTerm = valueString
            }
        } else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                device.headers = valueString
            }
        } else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                device.body = valueString
            }
        }
    }

    device
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