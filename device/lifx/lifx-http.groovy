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
preferences {
    input("server", "text", title: "Server", description: "Your LIFX-HTTP Server IP")
    input("port", "text", title: "Port", description: "Your LIFX-HTTP Server Port")
}
 
 
metadata {
	definition (name: "Lifx Http", namespace: "lifx", author: "Nicolas Cerveaux") {
    	capability "Polling"
		capability "Switch"
		capability "Switch Level"
		capability "Color Control"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) 
		{
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) 
		{
			state "level", action:"switch level.setLevel"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") 
		{
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch","refresh","levelSliderControl"])
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
}

// handle commands
def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
    api('on')
    
    sendEvent(name: 'switch', value: "on")
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
    api('off')
    
    sendEvent(name: 'switch', value: "off")
}

def setLevel() {
	log.debug "Executing 'setLevel'"
	// TODO: handle 'setLevel' command
}

def setHue() {
	log.debug "Executing 'setHue'"
	// TODO: handle 'setHue' command
}

def setSaturation() {
	log.debug "Executing 'setSaturation'"
	// TODO: handle 'setSaturation' command
}

def setColor() {
	log.debug "Executing 'setColor'"
	// TODO: handle 'setColor' command
}

private Long converIntToLong(ipAddress) {
	log.debug(ipAddress)
	long result = 0;;
	def parts = ipAddress.split("\\.")
    for (int i = 3; i >= 0; i--) {
        result |= (Long.parseLong(parts[3 - i]) << (i * 8));
    }

    return result & 0xFFFFFFFF;
}

private String convertIPToHex(ipAddress) {
	return Long.toHexString(converIntToLong(ipAddress));
}

private String getDeviceId() {
	def ip = convertIPToHex(settings.server)
	def port = Long.toHexString(Long.parseLong(settings.port))
	return ip + ":0x" + port
}

def api(String bulbRequest, success = {}) {
	log.debug("Location: $location")
    def hubWrapper = null
    for(def it : location.hubs) {
    	String name = it.name
        if(name != "Virtual Hub"){
        	hubWrapper = it
            break
        }
    }
    def hub = hubWrapper.getHub()
    log.debug(hub.id)
    
	if(!state.subscribe) {
        //subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribe = true
    }
    
    if("Virtual" == device.label){
        settings.server = "192.168.10.125"
        settings.port = "56780"
    }
    
    def ip = "${settings.server}:${settings.port}"
    def deviceNetworkId = getDeviceId()
    def encodedName = device.name.replaceAll(" ", "%20")
    
    log.debug "Server: ${settings.server}:${settings.port}"
    log.debug "DeviceId: ${deviceNetworkId}"
    log.debug "Name: ${encodedName}"
    
    if ( bulbRequest == "on" ) {
    	def uri = "/lights/label:${name}/on?_method=put"
    } else if ( bulbRequest == "off" ) {
    	def uri = "/lights/label:${name}/off?_method=put"
    }
    def turnOn = new physicalgraph.device.HubAction("""GET /lights HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}")
}
