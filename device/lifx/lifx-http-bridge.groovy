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

private getHostAddress() {
    def parts = device.deviceNetworkId.split(":")
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

// parse events into attributes
def parse(description) {
    def map = description
    if (description instanceof String)  {
        map = stringToMap(description)
    }
    def header = new String(map.header.decodeBase64()) 
    def body = new String(map.body.decodeBase64()) 
    try{
        def resp = new groovy.json.JsonSlurper().parseText(body)
        if(resp instanceof ArrayList){
            sendEvent(name: "bulbsStatus", value: device.hub.id, isStateChange: true, data: resp)
        }else{
            sendEvent(name: "bulbStatus", value: device.hub.id, isStateChange: true, data: resp)
        }
    } catch(Exception e) {
        log.error("Exception: "+e)
        log.error("Header: "+header)
        log.error("Body: "+body)
    }
}