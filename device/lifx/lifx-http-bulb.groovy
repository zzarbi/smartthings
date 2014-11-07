/**
 *  Lifx Http
 *
 *  Copyright 2014 Nicolas
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
    definition (name: "LIFX Bulb", namespace: "lifx", author: "Nicolas Cerveaux") {
        capability "Polling"
        capability "Switch"
        capability "Switch Level"
        capability "Color Control"
        capability "Refresh"
        
        command "setAdjustedColor"
        command "setValue", ["string", "string"]
    }

    simulator {
    }

    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light14", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light14", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.Lighting.light14", backgroundColor:"#79b821"
            state "turningOff", label:'${name}', icon:"st.Lighting.light14", backgroundColor:"#ffffff"
        }
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }
        controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
            state "color", action:"setAdjustedColor"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main(["switch"])
        details(["switch","levelSliderControl","rgbSelector","refresh"])
    }
}

def setValue(name, value) {
    return sendEvent(name: name, value: value)
}

def getBridge() {
    def bridge = parent.childDevices.find {it.name == "LIFX Bridge"}    
    return bridge
}

//parse events into attributes
def parse(value) {
    log.debug "Parsing '${value}' for ${device.deviceNetworkId}"
}

def poll() {
    getBridge().poll(device.deviceNetworkId)
}

def setAdjustedColor(value) {
    sendEvent(name: 'hue', value: value.hue)
    sendEvent(name: 'saturation', value: value.saturation)
    
    def data = [:]
    data.hue = value.hue
    data.saturation = value.saturation
    data.level = device.currentValue("level")
    
    getBridge().setAdjustedColor(device.deviceNetworkId, data)
}

def setLevel(double value) {
    sendEvent(name: 'level', value: value)
    
    def data = [:]
    data.hue = device.currentValue("hue")
    data.saturation = device.currentValue("saturation")
    data.level = value

    getBridge().setAdjustedColor(device.deviceNetworkId, data)
}

def on() {
    sendEvent(name: 'switch', value: 'on')
    getBridge().on(device.deviceNetworkId)
}

def off() {
    sendEvent(name: 'switch', value: 'off')
    getBridge().off(device.deviceNetworkId)
}

def refresh() {
    poll()
}
