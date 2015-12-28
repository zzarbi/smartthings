/**
 *  Revogi Smart Power Strip
 *  Source: https://github.com/zzarbi/smartthings/blob/master/device/wemo/wemo-insight-switch.groovy
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
    definition (name: "Revogi Smart Power Strip", namespace: "zzarbi", author: "Nicolas Cerveaux") {
        capability "Power Meter"
        capability "Switch"
        capability "Polling"
        capability "Refresh"
        
        command "poll", ["string"]
        command "on", ["string"]
        command "off", ["string"]
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
        valueTile("power", "device.power", decoration: "flat") {
            state "default", label:'${currentValue} W'
        }

        main "power"
        details (["switch", "power", "refresh"])
    }
}

import groovy.json.JsonSlurper

private debug(data){
    if(parent.appSettings.debug == "true"){
        log.debug(data)
    }
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

private sendCommand(String path, String method = "GET") {
    def ip = getHostAddress()
    debug("send $path with $method on $ip")
    def cmd = new physicalgraph.device.HubAction([
       'method': method,
       'path': path,
       'headers': [
           'HOST': ip
       ]
   ], device.deviceNetworkId)
   return cmd
}

// parse events into attributes
def parse(String description) {
    debug("Got somehting: $description")
    def map = stringToMap(description)
    def result = []
    
    if (map.body) {
        def json = new JsonSlurper().parseText(new String(map.body.decodeBase64()))
        
        // status update
        if (json.response == 511 && json.data){
            def switches = [:]
            def i = 0
            def power = 0
            
            for (i = 0;i<json.data."switch".size();i++) {
                updateSwitch(i, [
                    'status': json.data."switch"[i],
                    'power': Float.parseFloat(json.data."watt"[i])
                ])
                
                power += Float.parseFloat(json.data."watt"[i])
            }

            result << createEvent(name: "switch", value: (power>0?"on":"off"))
            result << createEvent(name: "power", value: power, unit: "Watts")
            
        }
    }
    
    result
}

private updateSwitch(number, data) {
    def dni = device.deviceNetworkId + ":"+number;   
    dni = new String(dni.encodeAsBase64())
    debug("updateSwitch #$number with $data dni: $dni")
    
    // not sure why find doesn't wrk but this does...
    def aSwitch
    for(def d : parent.childDevices) {
        if(d.deviceNetworkId == dni){
            aSwitch = d
            break
        }
    }
    
    if(aSwitch){
        // update switch
        if(data.status == 1 && aSwitch.currentValue("switch")!='on'){
            log.debug('Update switch to on')
            aSwitch.setValue('switch', "on")
        }else if(data.status == 0 && aSwitch.currentValue("switch")!='off'){
            log.debug('Update switch to off')
            aSwitch.setValue('switch', "off")
        }
        
        // update level
        if(data.power != aSwitch.currentValue("power")){
            log.debug('Update power to '+data.power)
            aSwitch.setValue('power', data.power)
        }
    }
}

def refresh() {
    poll()
}

def poll() {
    debug("Polling Data for ${device.name}")
    sendCommand("/?cmd=511")
}

def on() {
    sendCommand('/?cmd=200&json={"port":0,"state":1}')
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendCommand('/?cmd=200&json={"port":0,"state":0}')
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "power", value: 0, unit: "Watts")
}

def on(switchNumber) {
    sendCommand('/?cmd=200&json={"port":'+(switchNumber+1)+',"state":1}')
}

def off(switchNumber) {
    sendCommand('/?cmd=200&json={"port":'+(switchNumber+1)+',"state":0}')
}


