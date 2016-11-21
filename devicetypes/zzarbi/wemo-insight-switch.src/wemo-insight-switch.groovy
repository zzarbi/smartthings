/**
 *  WeMo Insight Switch
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
metadata {
    // Automatically generated. Make future change here.
    definition (name: "WeMo Insight Switch", namespace: "zzarbi", author: "Nicolas Cerveaux") {
        capability "Power Meter"
        capability "Energy Meter"
        capability "Actuator"
        capability "Switch"
        capability "Polling"
        capability "Refresh"

        command "subscribe"
        command "resubscribe"
        command "unsubscribe"
    }

    // simulator metadata
    simulator {}

    // UI tile definitions
    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
            state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("energy", "device.energy", decoration: "flat") {
            state "default", label:'${currentValue} Wm'
        }
        valueTile("power", "device.power", decoration: "flat") {
            state "default", label:'${currentValue} W'
        }

        main "switch"
        details (["switch", "power", "energy", "refresh"])
    }
}

private debug(data){
    if(parent.appSettings.debug == "true"){
        log.debug(data)
    }
}

// parse events into attributes
def parse(String description) {
    if (description) {
        return false
    }
    debug(description)
    def map = stringToMap(description)

    def headerString = ""
    if (map.headers) {
        headerString = new String(map.headers.decodeBase64())
    }
    def result = []

    // update subscriptionId
    if (headerString.contains("SID: uuid:")) {
        def sid = (headerString =~ /SID: uuid:.*/) ? ( headerString =~ /SID: uuid:.*/)[0] : "0"
        sid -= "SID: uuid:".trim()
        debug('Update subscriptionID: '+ sid)
        updateDataValue("subscriptionId", sid)
    }

    // parse the rest of the message
    if (map.body) {
        def bodyString = new String(map.body.decodeBase64())
        def body = new XmlSlurper().parseText(bodyString)

        if(bodyString.contains('GetBinaryStateResponse') || bodyString.contains('GetInsightParamsResponse') || bodyString.contains('SetBinaryStateResponse') || bodyString.contains('<BinaryState>')) {
            def responseValue = body.text();
            def value = "off"
            def energy = 0
            def power = 0
            debug("Updating Device:")
            if(responseValue.contains("|")){
                /*
                    State (1 on | 0 off | 8 standby)
                    Seconds Since 1970 of Last State Change 1416067796
                    Last On Seconds 3835
                    Seconds On Today 14633
                    Unknown � Unit is Seconds 236095
                    Total Seconds 1209600
                    Avg On � Units are Watts 98
                    Energy Used Now in mW * minutes 124130
                    Energy Used Total in mW * minutes 24366561
                    Unknown
                 */
                def parts = responseValue.split("\\|")
                value = (parts[0].toInteger() >= 1) ? "on" : "off"
                energy = Math.ceil(parts[8].toInteger() / 1000)
                power = Math.ceil(parts[7].toInteger() / 1000)
                debug("Status: $value")
                debug("Energy: $energy")
                debug("Power: $power")
            }else{
                value = responseValue.toInteger() == 1 ? "on" : "off"
                debug("Status: $value")
            }

            // send event to refresh switch
            result << createEvent(name: "switch", value: value)
            result << createEvent(name: "energy", value: energy, unit: "Wm")
            result << createEvent(name: "power", value: power, unit: "W")
        }
    }

    result
}

private getCallBackAddress() {
    device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            debug("Can't figure out ip and port for device: ${device.id}")
        }
    }

    //convert IP/port
    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    debug("Using ip: ${ip} and port: ${port} for device: ${device.id}")
    return ip + ":" + port
}

private postRequest(path, SOAPaction, body) {
    // Send  a post request
    new physicalgraph.device.HubAction([
        'method': 'POST',
        'path': path,
        'body': body,
        'headers': [
            'HOST': getHostAddress(),
            'Content-type': 'text/xml; charset=utf-8',
            'SOAPAction': "\"${SOAPaction}\""
        ]
    ], device.deviceNetworkId)
}

def poll() {
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <s:Body>
        <u:GetInsightParams xmlns:u="urn:Belkin:service:insight:1"></u:GetInsightParams>
    </s:Body>
</s:Envelope>
"""
    postRequest('/upnp/control/insight1', 'urn:Belkin:service:insight:1#GetInsightParams', body)
}

def on() {
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <SOAP-ENV:Body>
        <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
            <BinaryState>1</BinaryState>
        </m:SetBinaryState>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
"""
    postRequest('/upnp/control/basicevent1', 'urn:Belkin:service:basicevent:1#SetBinaryState', body)
}

def off() {
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <SOAP-ENV:Body>
        <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
            <BinaryState>0</BinaryState>
        </m:SetBinaryState>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
"""
    postRequest('/upnp/control/basicevent1', 'urn:Belkin:service:basicevent:1#SetBinaryState', body)
}

def refresh() {
    debug("Executing WeMo Switch 'subscribe', then 'timeSyncResponse', then 'poll'")
    poll()
}

def subscribe(hostAddress) {
    debug("Executing 'subscribe()'")
    def address = getCallBackAddress()
    new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${hostAddress}
CALLBACK: <http://${address}/>
NT: upnp:event
TIMEOUT: Second-5400
User-Agent: CyberGarage-HTTP/1.0


""", physicalgraph.device.Protocol.LAN)
}

def subscribe() {
    subscribe(getHostAddress())
}

def subscribe(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        debug("Updating ip from $existingIp to $ip")
        updateDataValue("ip", ip)
    }
    if (port && port != existingPort) {
        debug("Updating port from $existingPort to $port")
        updateDataValue("port", port)
    }

    subscribe("${ip}:${port}")
}

def resubscribe() {
    debug("Executing 'resubscribe()'")
    def sid = getDeviceDataByName("subscriptionId")

    new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}
TIMEOUT: Second-5400


""", physicalgraph.device.Protocol.LAN)

}

def unsubscribe() {
    def sid = getDeviceDataByName("subscriptionId")
    new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}


""", physicalgraph.device.Protocol.LAN)
}