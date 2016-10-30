/**
 *  Smarter Security Camera
 *
 *  Copyright 2016 Nicolas Cerveaux
 *  * Only compatible with Diskstation Camera (https://github.com/st-swanny/smartthings/blob/master/DiskStation/DiskstationCamera.groovy)
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
definition(
    name: "Smarter Security Camera",
    namespace: "zzarbi",
    author: "Nicolas Cerveaux",
    description: "Let you choose a preset for different mode\r\n* Based on Smart Security Camera of BLebson (https://github.com/blebson/Smart-Security-Camera/blob/master/smartapps/blebson/smart-security-camera.src/smart-security-camera.groovy)",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when@2x.png")


preferences {
	page(name: "homePage")
	page(name: "preferencesPage")
}

def homePage() {
    dynamicPage(name: "homePage", install: false, uninstall: !theCamera, nextPage: 'preferencesPage') {
        section("About") {
            paragraph "Smarter Security Camera will change the position of you camera depending of your Mode"
            paragraph "This is compatible only with Diskstation Camera"
        }
        section("Please select a camera to manage") {
            input "theCamera", "capability.imageCapture", multiple: false, required: true
        }
    }    
}

def preferencesPage() {
    dynamicPage(name: "preferencesPage", install: true, uninstall: !theCamera) {
        section("Please select a preset for each modes") {
        	location.modes.each {
            	def presetSettings = "pressetFor_" + it
                def label = "Name of the Preset for " + it
                
        		input(name: presetSettings, type: "text", title: label, required: false)
        	}
        }
    }
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// Subscribe to mode change
    subscribe(location, "mode", modeChangeHandler)
    
    location.modes.each {
        def presetSettings = settings."pressetFor_${it}"
        presetSettings = presetSettings.trim()
		if (presetSettings) {
        	log.debug("Save preset for $it to: $presetSettings")
        }
    }
}

def modeChangeHandler(evt) {
    log.debug "mode changed to ${evt.value}"
    
    def preset = settings."pressetFor_${evt.value}"
    preset = preset.trim()
    
    if (preset) {
    	log.debug("Move Camera to: ${preset}")
        theCamera.presetGoName(preset)
    }
}
