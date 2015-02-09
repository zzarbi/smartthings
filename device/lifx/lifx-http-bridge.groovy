/**
 *  LIFX Bridge
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
    definition (name: "LIFX Bridge", namespace: "lifx", author: "Nicolas Cerveaux") {
        attribute "serialNumber", "string"
        attribute "networkAddress", "string"
        
        command "poll", ["string"]
        command "setAdjustedColor", ["string", "json_object"]
        command "on", ["string"]
        command "off", ["string"]
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles {
        standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "default", label: "LIFX Bridge", action: "", icon: "st.unknown.zwave.static-controller", backgroundColor: "#FFFFFF"
        }
        valueTile("networkAddress", "device.hostaddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
            state "default", label:'${getHostAddress}'
        }

        main (["icon"])
        details(["networkAddress"])
    }
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def parts = device.deviceNetworkId.split(":")
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    //log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

// parse events into attributes
def parse(description) {
    def map = description
    if (description instanceof String)  {
        map = stringToMap(description)
    }
    def headers = ''
    if(map.headers){
        headers = new String(map.headers.decodeBase64()) 
    }
    def body = new String(map.body.decodeBase64()) 
    try{
        if(headers.contains("application/json")){
            def resp = new groovy.json.JsonSlurper().parseText(body)
            if(resp instanceof ArrayList){
                sendEvent(name: "bulbsStatus", value: device.hub.id, isStateChange: true, data: resp)
                resp.each { bulb ->
                    updateBulb(bulb.id, bulb)
                }
            }else{
                updateBulb(resp.id, resp)
            }
        }
    } catch(Exception e) {
        log.error("Exception: "+e)
        log.error("Description: "+description)
    }
}

private updateBulb(bulbId, data) {
    // not sure why find doesn't wrk but this does...
    def bulb
    for(def d : parent.childDevices) {
        if(d.deviceNetworkId == bulbId){
            bulb = d
            break
        }
    }
    
    if(bulb){
        //log.debug("Update "+bulb.deviceNetworkId)
        def brightness = Math.ceil(data.color.brightness*100)
        def hue = Math.ceil(data.color.hue / 3.6)
        def saturation = Math.ceil(data.color.saturation*100)

        // update switch
        if(data.on == true && bulb.currentValue("switch")!='on'){
            log.debug('Update switch to on')
            bulb.setValue('switch', "on")
        }else if(data.on == false && bulb.currentValue("switch")!='off'){
            log.debug('Update switch to off')
            bulb.setValue('switch', "off")
        }
        
        // update level
        if(brightness != bulb.currentValue("level")){
            //log.debug('Update level to '+brightness)
            bulb.setValue('level', brightness)
        }
        
        // update hue
        if(hue != bulb.currentValue("hue")){
            //log.debug('Update hue to '+hue)
            bulb.setValue('hue', hue)
        }
        
        // update saturation
        if(saturation != bulb.currentValue("saturation")){
            //log.debug('Update saturation to '+saturation)
            bulb.setValue('saturation', saturation)
        }
    }
}


private sendCommand(path, method="GET") {
    //log.debug("Path: "+path)
    new physicalgraph.device.HubAction([
       'method': method,
       'path': path,
       'headers': [
           'HOST': getHostAddress()
       ]
   ], device.deviceNetworkId)
}

/* Hook for child devices */
def poll(childDeviceId) {
    sendCommand("/lights/"+childDeviceId)
}

def setAdjustedColor(childDeviceId, data) {
    def hue = Math.ceil(data.hue*3.6)
    def saturation = data.saturation/100
    def brightness = data.level/100
    def duration = 1
    
    sendCommand("/lights/"+childDeviceId+"/color?hue=$hue&saturation=$saturation&brightness=$brightness&duration=$duration&_method=put")
}

def on(childDeviceId) {
    sendCommand("/lights/"+childDeviceId+"/on?_method=put")
}

def off(childDeviceId) {
    sendCommand("/lights/"+childDeviceId+"/off?_method=put")
}