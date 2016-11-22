/**
 *  WD TV Live Player
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
    definition (name: "WD TV Live", namespace: "zzarbi", author: "Nicolas Cerveaux") {
        capability "Switch"
        capability "Switch Level"
        capability "Media Controller"
        capability "Polling"
        capability "Refresh"
        capability "Music Player"

        command "subscribe"
        command "resubscribe"
        command "unsubscribe"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    // UI tile definitions
    tiles {
        // Main
        standardTile("main", "device.status", width: 1, height: 1, canChangeIcon: true) {
            state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
            state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#79b821"
            state "grouped", label:'Grouped', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
        }

        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Entertainment.entertainment11", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.Entertainment.entertainment11", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.Entertainment.entertainment11", backgroundColor:"#79b821"
            state "turningOff", label:'${name}', icon:"st.Entertainment.entertainment11", backgroundColor:"#ffffff"
        }

        standardTile("mute", "device.mute", inactiveLabel: false, decoration: "flat") {
            state "unmuted", label:"", action:"music Player.mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffffff", nextState:"muted"
            state "muted", label:"", action:"music Player.unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff", nextState:"unmuted"
        }

        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
            state "level", action:"switch level.setLevel", backgroundColor:"#ffffff"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "main"
        details (["switch", "mute", "levelSliderControl", "refresh"])
    }
}

// parse events into attributes
def parse(String description) {
    def map = stringToMap(description)
    def header = new String(map.headers.decodeBase64())
    def result = []
    log.debug("Header: "+header)

    // parse the rest of the message
    if(header.contains("text/xml")){
        if (map.body) {
            def bodyString = new String(map.body.decodeBase64())
            def body = new XmlSlurper().parseText(bodyString)

            if(bodyString.contains('u:GetMuteResponse')){
                def responseValue = body.text();
                def value = responseValue.toInteger() == 1 ? "muted" : "unmuted"
                result << createEvent(name: "mute", value: value)
            }else if(bodyString.contains('u:GetVolumeResponse')){
                def responseValue = body.text();
                def value = responseValue.toInteger()
                result << createEvent(name: "level", value: value)
            }
        }
    }

    result
}

// handle commands
def on() {
    log.debug "Executing 'on'"
    // TODO: handle 'on' command
}

def off() {
    log.debug "Executing 'off'"
    // TODO: handle 'off' command
}

def startActivity() {
    log.debug "Executing 'startActivity'"
    // TODO: handle 'startActivity' command
}

def getAllActivities() {
    log.debug "Executing 'getAllActivities'"
    // TODO: handle 'getAllActivities' command
}

def getCurrentActivity() {
    log.debug "Executing 'getCurrentActivity'"
    // TODO: handle 'getCurrentActivity' command
}

def poll() {
    // Get Mute update
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <s:Body>
        <u:GetMute xmlns:u="urn:schemas-upnp-org:service:RenderingControl:1">
            <InstanceID>0</InstanceID>
            <Channel>Master</Channel>
            <CurrentMute/>
        </u:GetMute>
    </s:Body>
</s:Envelope>
"""
    postRequest('/MediaRenderer_RenderingControl/control', 'urn:schemas-upnp-org:service:RenderingControl:1#GetMute', body)

    // Get Volume update
    body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <s:Body>
        <u:GetVolume xmlns:u="urn:schemas-upnp-org:service:RenderingControl:1">
            <InstanceID>0</InstanceID>
            <Channel>Master</Channel>
            <CurrentVolume/>
        </u:GetVolume>
    </s:Body>
</s:Envelope>
"""
    postRequest('/MediaRenderer_RenderingControl/control', 'urn:schemas-upnp-org:service:RenderingControl:1#GetVolume', body)
}

def refresh() {
    poll()
}

def play() {
    log.debug "Executing 'play'"
    // TODO: handle 'play' command
}

def pause() {
    log.debug "Executing 'pause'"
    // TODO: handle 'pause' command
}

def stop() {
    log.debug "Executing 'stop'"
    // TODO: handle 'stop' command
}

def nextTrack() {
    log.debug "Executing 'nextTrack'"
    // TODO: handle 'nextTrack' command
}

def playTrack() {
    log.debug "Executing 'playTrack'"
    // TODO: handle 'playTrack' command
}

def playText() {
    log.debug "Executing 'playText'"
    // TODO: handle 'playText' command
}

def setLevel(double value) {
    def level = value.intValue()
    // Get Mute update
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <s:Body>
        <u:SetVolume xmlns:u="urn:schemas-upnp-org:service:RenderingControl:1">
            <InstanceID>0</InstanceID>
            <Channel>Master</Channel>
            <DesiredVolume>${level}</DesiredVolume>
        </u:SetVolume>
    </s:Body>
</s:Envelope>
"""
    postRequest('/MediaRenderer_RenderingControl/control', 'urn:schemas-upnp-org:service:RenderingControl:1#SetVolume', body)
}

def previousTrack() {
    log.debug "Executing 'previousTrack'"
    // TODO: handle 'previousTrack' command
}

private muteAction(value) {
    // Get Mute update
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <s:Body>
        <u:SetMute xmlns:u="urn:schemas-upnp-org:service:RenderingControl:1">
            <InstanceID>0</InstanceID>
            <Channel>Master</Channel>
            <DesiredMute>${value}</DesiredMute>
        </u:SetMute>
    </s:Body>
</s:Envelope>
"""
    postRequest('/MediaRenderer_RenderingControl/control', 'urn:schemas-upnp-org:service:RenderingControl:1#SetMute', body)
}

def mute() {
    muteAction(1)
}

def unmute() {
    muteAction(0)
}

def setTrack() {
    log.debug "Executing 'setTrack'"
    // TODO: handle 'setTrack' command
}

def resumeTrack() {
    log.debug "Executing 'resumeTrack'"
    // TODO: handle 'resumeTrack' command
}

def restoreTrack() {
    log.debug "Executing 'restoreTrack'"
    // TODO: handle 'restoreTrack' command
}

def subscribe() {
    log.debug "Executing 'subscribe'"
}

def subscribe(ip, port) {
    log.debug "Executing 'subscribe'"
}

def resubscribe() {
    log.debug "Executing 'resubscribe'"
}

private postRequest(path, SOAPaction, body) {
    log.debug("Process: "+SOAPaction)
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

private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            //log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    //convert IP/port
    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    //log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}