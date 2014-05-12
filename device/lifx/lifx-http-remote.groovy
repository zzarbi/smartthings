/**
 *  Lifx Http - Via Remote Access
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
preferences {
    input("host", "text", title: "Host", description: "Your lifx-http Host Name")
    input("port", "text", title: "Port", description: "Your lifx-http Server Port")
}
 
metadata {
	definition (name: "lifx-http Remote", namespace: "lifx", author: "Nicolas Cerveaux") {
    	capability "Polling"
		capability "Switch"
		capability "Switch Level"
		capability "Color Control"
		capability "Refresh"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
			state "color", action:"setAdjustedColor"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "saturation", action:"color control.setSaturation"
		}
		main "switch"
		details(["switch","levelSliderControl","rgbSelector","refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

def parse(Map event) {
	log.debug "Parsing '${event}'"
}

def poll() {
    log.debug "Executing 'poll'"
    String selector = getLifxSelector()

    sendCommand("/$selector", "get") { response ->
    	//set switch state
        if(device.currentState("switch").value == "on"){// current state on
        	if(!response.data.on){
            	sendEvent(name: 'switch', value: "off")
            }
        }else{ //current state off
        	if(response.data.on){
            	sendEvent(name: 'switch', value: "on")
            }
        }
        
        //set level
        double level = response.data.color.brightness
        level = level*100
        sendEvent(name: 'level', value: level)
        
        //set hue
        double hue = response.data.color.hue
        sendEvent(name: 'hue', value: hue)
        
        //set saturation
        double saturation = response.data.color.saturation
        sendEvent(name: 'saturation', value: saturation)
        
        log.debug("Light is "+device.currentState("switch").value)
        log.debug("Hue: "+device.currentState("hue").value)
        log.debug("Brightness: "+device.currentState("level").value)
        log.debug("Saturation: "+device.currentState("saturation").value)
        
    }
}


def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
}

private getLifxSelector() {
	String label = device.name.replaceAll(" ", "%20")
	return "label:$label"
}

private sendCommand(command, method = "get", success = {}){
	def url = "http://"+settings.host+":"+settings.port+"/lights"
	
	if(method == "get"){
    
   		log.debug("Get: " + url + command)
    
		httpGet(url + command, success)
	}
	
}

private changeColor(String name, value, duration = 0){
	log.debug("Change $name to $value for $duration")
	double hue = Double.parseDouble(device.currentState("hue").value)
	double brightness = Double.parseDouble(device.currentState("level").value)
	double saturation = Double.parseDouble(device.currentState("saturation").value)
    
    if(name == 'hue'){
    	hue = value
    }else if(name == 'level'){
    	brightness = value/100
    }else if(name == 'saturation'){
    	saturation = value
    }
    String commandURl = "/"+getLifxSelector()+"/color?hue=$hue&saturation=$saturation&brightness=$brightness&duration=$duration&_method=put"
    sendCommand(commandURl, "get") { response ->
    	log.debug(response)
        log.debug("Success change $name to $value")
        sendEvent(name: name, value: value)
    } 
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}

// handle commands
def on() {
	log.debug "Executing 'on'"
    String selector = getLifxSelector()
    sendCommand("/"+selector+"/on?_method=put", "get") {
    	sendEvent(name: 'switch', value: "on")
    }
}

def off() {
	log.debug "Executing 'off'"
    String selector = getLifxSelector()
    sendCommand("/"+selector+"/off?_method=put", "get") {
    	sendEvent(name: 'switch', value: "off")
    }
}

def setLevel(double value) {
	changeColor("level", value)
}

def setHue(value) {
	changeColor("hue", value)
}

def setSaturation(value) {
	changeColor("saturation", value)
}


def setColor(value) {
	log.debug "setColor: ${value}"
}
