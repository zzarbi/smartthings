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

//parse events into attributes
def parse(value) {
    log.debug "Parsing '${value}'"
}

def poll() {
    parent.poll(this)
}

def setAdjustedColor(value) {
    parent.setAdjustedColor(this, value)
}

def refresh() {
    parent.refresh(this)
}

def setLevel(double value) {
    parent.setLevel(this, value)
}

def on() {
    parent.on(this)
}

def off() {
    parent.off(this)
}
