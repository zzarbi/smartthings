/**
 *  Revogi Smart Power Strip Switch
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
    definition (name: "Revogi Smart Power Strip Switch", namespace: "zzarbi", author: "Nicolas Cerveaux") {
        capability "Power Meter"
        capability "Switch"
        capability "Refresh"

        command "setValue", ["string", "string"]
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

        main "switch"
        details (["switch", "power", "refresh"])
    }
}

private debug(data){
    if(parent.appSettings.debug == "true"){
        log.debug(data)
    }
}

private getSwitchNumber() {
    def dniDecoded = new String(device.deviceNetworkId.decodeBase64())
    def parts = dniDecoded.split(":")
    
    return parts[1].toInteger()
}

def setValue(name, value) {
    if(name == "switch"){
        return sendEvent(name: name, value: value)
    }else if(name=="power") {
        return sendEvent(name: name, value: value, unit: "Watts")
    }
}

private getBridge() {
    def bridge = parent.childDevices.find {it.name == "Revogi Smart Power Strip"}    
    return bridge
}

// parse events into attributes
def parse(String description) {}

def refresh() {
    getBridge().refresh()
}

def on() {
    getBridge().on(getSwitchNumber())
    sendEvent(name: "switch", value: "on")
}

def off() {
    getBridge().off(getSwitchNumber())
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "power", value: 0, unit: "Watts")
}


