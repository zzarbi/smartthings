/**
 *  Lifx Http
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
    definition (name: "LIFX Group", namespace: "lifx", author: "Nicolas Cerveaux") {
        capability "Polling"
        capability "Switch"
        capability "Switch Level"
        capability "Color Control"
        capability "Refresh"
        
        command "setAdjustedColor"
        command "setColor"
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

private debug(data){
    if(parent.appSettings.debug == "true"){
        log.debug(data)
    }
}

private getAccessToken() {
    return parent.appSettings.accessToken;
}

private sendCommand(path, method="GET", body=null) {
    def accessToken = getAccessToken()
    def pollParams = [
        uri: "https://api.lifx.com",
        path: "/v1beta1/"+path+".json",
        headers: ["Content-Type": "application/x-www-form-urlencoded", "Authorization": "Bearer ${accessToken}"],
        body: body
    ]
    debug(method+" Http Params ("+pollParams+")")
    
    try{
        if(method=="GET"){
            httpGet(pollParams) { resp ->            
                parseResponse(resp)
            }
        }else if(method=="PUT") {
            httpPut(pollParams) { resp ->            
                parseResponse(resp)
            }
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}

private parseResponse(resp) {
    debug("Response: "+resp.data)
    if(resp.status == 200) {
        if (resp.data) {
            if(resp.data instanceof Collection){
                // Default values
                def groupPower = "off"
                def groupBrightness = 0
                def groupHue = 100
                def groupSaturation = 100
                
                resp.data.each {
                    if(it.power == "on") {
                        groupPower = "on"
                        groupBrightness = Math.ceil(it.brightness*100)
                        groupHue = Math.ceil(it.color.hue / 3.6)
                        groupSaturation = Math.ceil(it.color.saturation*100)
                    }
                }
                
                // update power
                if(device.currentValue("switch")!=groupPower){
                    debug("Update switch to "+groupPower)
                    sendEvent(name: 'switch', value: groupPower)
                }
                
                // update level
                if(groupBrightness != device.currentValue("level")){
                    debug('Update level to '+groupBrightness)
                    sendEvent(name: 'level', value: groupBrightness)
                }

                // update hue
                if(groupHue != device.currentValue("hue")){
                    debug('Update hue to '+groupHue)
                    sendEvent(name: 'hue', value: groupHue)
                }
                
                // update saturation
                if(groupSaturation != device.currentValue("saturation")){
                    debug('Update saturation to '+groupSaturation)
                    sendEvent(name: 'saturation', value: groupSaturation)
                }
            }
        }
    }else if(resp.status == 201){
        debug("Something was created/updated")
    }
}

//parse events into attributes
def parse(value) {
    debug("Parsing '${value}' for ${device.deviceNetworkId}")
}

private sendAdjustedColor(data, powerOn) {
    def hue = Math.ceil(data.hue*3.6)
    def saturation = data.saturation/100
    def brightness = data.level/100
    
    sendCommand("lights/group_id:"+device.deviceNetworkId+"/color", "PUT", 'color=hue%3A'+hue+'%20saturation%3A'+saturation+'%20brightness%3A'+brightness+'&duration=1&power_on='+powerOn)
}

def setAdjustedColor(value) {
    def data = [:]
    data.hue = value.hue
    data.saturation = value.saturation
    data.level = device.currentValue("level")
    
    sendAdjustedColor(data, 'true')
    sendEvent(name: 'switch', value: "on")
    sendEvent(name: 'hue', value: value.hue)
    sendEvent(name: 'saturation', value: value.saturation)
}

def setLevel(double value) {
    def data = [:]
    data.hue = device.currentValue("hue")
    data.saturation = device.currentValue("saturation")
    data.level = value

    sendAdjustedColor(data, 'true')
    sendEvent(name: 'level', value: value)
    sendEvent(name: 'switch', value: "on")
}

def setColor(value) {
    log.debug "setColor: ${value}"
    def data = [:]
    data.hue = value.hue
    data.saturation = value.saturation
    data.level = (value.level)?value.level:device.currentValue("level")
    
    sendAdjustedColor(data, 'true')
    sendEvent(name: 'hue', value: value.hue)
    sendEvent(name: 'saturation', value: value.saturation)
    sendEvent(name: 'switch', value: "on")
}

def on() {
    sendCommand("lights/group_id:"+device.deviceNetworkId+"/power", "PUT", "state=on&duration=1")
    sendEvent(name: 'switch', value: "on")
}

def off() {
    sendCommand("lights/group_id:"+device.deviceNetworkId+"/power", "PUT", "state=off&duration=1")
    sendEvent(name: 'switch', value: "off")
}

def refresh() {
    sendCommand("lights/group_id:"+device.deviceNetworkId)
}

def poll() {
    refresh()
}
