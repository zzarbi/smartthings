<?php /**
 * @author Nicolas Cerveaux
 * 
 * This script will download the source code of every devices listed above from Smartthings Websites
 */

$sessionId = 'REPLACE_WITH_YOUR_SESSION_ID'; // NEED TO BE REPLACED
$pathToSave = '/device/smartthings/';

$deviceUrl = 'https://graph.api.smartthings.com/ide/device/example/';
$rootDirectory = realpath(dirname(__FILE__).'/../');

$devices = array(	
    "8a8a038b3f1faad3013f1fad0481000c" => "Aeon Home Energy Meter",
    "8a9c25843e37f441013e37f51a780009" => "Aeon Illuminator Module",
    "2f981316-8b6f-4c19-b031-9ae53ff4c95a" => "Aeon Key Fob",
    "54cb0498-d099-4f57-bf28-b3538cd52217" => "Aeon LED Bulb",
    "7a23cd3e-ec6e-42ba-aded-4045da19843e" => "Aeon Minimote",
    "8a9d4b1e3bd91ce9013bd91d728f0010" => "Aeon Multisensor",
    "f752a00c-e153-4141-949a-d4114844f599" => "Aeon Multisensor 6",
    "4a9285d5-9763-44fd-8492-b7861e483bd8" => "Aeon Multisensor Gen5",
    "8a9c25843e37f441013e37f511d90007" => "Aeon Outlet",
    "c3a56dda-a2c9-40eb-96ad-a1397809b02e" => "Aeon Siren",
    "4dd92203-1417-487e-987b-fb7d8128315f" => "Aeon SmartStrip",
    "26637bf2-3292-11e2-9c79-22000a1dc790" => "Arduino ThingShield",
    "2663790e-3292-11e2-9c79-22000a1dc790" => "Arrival Sensor",
    "83fee9dd-7e29-42e6-b09b-897815626840" => "Bose SoundTouch",
    "db343b32-0f1d-4cb5-90e9-c7c60a7dadd3" => "CT100 Thermostat",
    "5b1fa934-132b-4b90-b7b3-1094bae1a8ef" => "CentraLite Dimmer",
    "4018627f-fccc-4514-9c31-a853e6036326" => "CentraLite Thermostat",
    "ff20e87b-992b-42b0-9cd6-72b0030dedc3" => "Cooper RF9500",
    "1fdc5cd8-07ef-4db5-bfa4-e57ced4ef8fd" => "Cree Bulb",
    "bdefadfd-d59f-4d54-82c2-5fbb3ac6965f" => "Danalock",
    "8a9d4b1e3b9b1fe3013b9b206a7f000d" => "Dimmer Switch",
    "8a9d4b1e3b6c74b9013b6c752771000b" => "Door Shield",
    "eea4ae16-aaab-4a53-a87c-8d82f144f09a" => "Dropcam",
    "8aa8a6b23f769c09013f769ece9b0010" => "Dropcam",
    "5532c73e-0bf3-40aa-a51b-786b3b81c82a" => "EcoNet Vent",
    "8aa8a6b23f769c09013f769ed1c20012" => "Ecobee",
    "359eb4ba-0751-4394-aa01-fe81c6e0a12a" => "Ecobee Thermostat",
    "8aa8a6b23f769c09013f769e8f2f0003" => "Everspring Flood Sensor",
    "825614f3-8a36-4578-8124-a15459ac14db" => "Fibaro Door/Window Sensor",
    "975c2616-84ae-4798-904f-710d0ce1f0a7" => "Fibaro Flood Sensor",
    "fa5a1942-608f-4220-902c-247ec22f97da" => "Fibaro Motion Sensor",
    "f70cfb8b-3382-4420-99c5-c8bd2511c85a" => "Fibaro Smoke Sensor",
    "9cf2c422-150d-4b91-b214-14c69fe74a81" => "Fidure Thermostat",
    "8a1118243f879a60013f8fe4efdb2812" => "Fortrezz Water Valve",
    "dc16563c-8b89-4756-8415-169d72a7ffee" => "Foscam",
    "be174007-2261-4585-951a-497ffd945517" => "GE Link Bulb",
    "3fc968b2-29cd-4f7f-bf09-4afd14bcaeae" => "GE ZigBee Dimmer",
    "6b30c06b-ab14-40df-8662-683359c67b98" => "GE ZigBee Switch",
    "6b30c06b-ab14-40df-8662-683359c67b98" => "GE ZigBee Switch",
    "5328631a-1e46-4bb6-abaf-4deb67b5db8f" => "Home Energy Meter",
    "fef2e8d4-a1c4-4e4a-ba5f-221b8a0713c2" => "HomeSeer Multisensor",
    "6bfd4c3f-b31a-4af4-b457-71ad395699ad" => "Hue Bridge",
    "48b39485-7ebc-4c80-bf6e-9a653a2d1c95" => "Hue Bulb",
    "2eda355c-c98f-4cb7-95c1-637d8904c001" => "Hue Lux Bulb",
    "f36e327b-9fe4-4190-a4ec-c53dea1fba85" => "Jawbone User",
    "83c6f7ba-46f4-490c-9c47-7b00f1f0978c" => "Keen Home Smart Vent",
    "8823dd69-a087-4a66-b207-0fac5bff8c99" => "LIFX Color Bulb",
    "894fb483-9a0c-4f5c-b437-b164a9022971" => "LIFX White Bulb",
    "017a57fa-5f47-4faf-96b3-183eed48ec9e" => "Life360 User",
    "2663758a-3292-11e2-9c79-22000a1dc790" => "Light Sensor",
    "a33780a2-68c1-465b-9c81-ca8081701b40" => "Logitech Harmony Hub C2C",
    "ad6950f0-c9fe-4f5a-b24b-56aeb5938627" => "Microchip Test Board",
    "cb38b684-52fc-4065-ba80-68bc26b8d12d" => "MimoLite Garage Door Controller",
    "8a9d4b1e3bfce38a013bfce42d360015" => "Mobile Presence",
    "8a1814663e5dbf7d013e602ecece0248" => "Momentary Button Tile",
    "26637648-3292-11e2-9c79-22000a1dc790" => "Motion Detector",
    "7af699fe-1ab2-4ce2-93fb-a7153d4d71b4" => "NYCE Motion Sensor",
    "d7d4c294-0310-4860-8b5c-1c7bf9225f09" => "NYCE Open/Closed Sensor",
    "0e7aa867-9aec-4a24-a372-69014bfa8c24" => "Nest Thermostat",
    "c133d1c2-07cb-4eb0-a4ac-e3c458cac59a" => "Netatmo Additional Module",
    "ba155665-433d-4e20-9d12-efad7744c3d6" => "Netatmo Basestation",
    "262c0013-b1ab-47d6-9255-0aff499efdf1" => "Netatmo Basestation",
    "a1e6b21e-2ee6-4c58-b83a-31047eb687aa" => "Netatmo Outdoor Module",
    "7c0d7b98-bc45-441f-ace5-045aef8a2609" => "Netatmo Rain",
    "684c53d1-e70c-4421-964f-b7275a75fe92" => "OSRAM LIGHTIFY Gardenspot mini RGB",
    "76c75bb4-1a43-425a-b1ce-1766a5bd4404" => "OSRAM LIGHTIFY LED Flexible Strip RGBW",
    "64c6c2d2-784b-4782-9028-19b393884bf3" => "OSRAM LIGHTIFY LED Tunable White 60W",
    "6d38663a-1703-46b6-9939-8c1fb9467cf9" => "ObyThing Music",
    "8a70b39f3e5dbc6e013e602bba0a0272" => "On/Off Button Tile",
    "26637d50-3292-11e2-9c79-22000a1dc790" => "On/Off Shield",
    "266374ae-3292-11e2-9c79-22000a1dc790" => "Open/Closed Sensor",
    "266377a6-3292-11e2-9c79-22000a1dc790" => "Particulate Detector",
    "26637c9c-3292-11e2-9c79-22000a1dc790" => "Pet Feeder Shield",
    "75fe2090-7e25-43a1-a3f8-5c7cbcf7dbac" => "Plant Link",
    "188ef304-8efa-45c1-9089-aea07c7c85f0" => "Quirky Wink Eggtray",
    "28ac03be-94fb-40e8-8e70-cfc529794a3e" => "Quirky Wink Nimbus",
    "f730b653-b0b6-483e-8c84-977e240f5eb3" => "Quirky Wink Porkfolio",
    "424d3f7b-42c7-4eea-8675-f87d6449bf28" => "Quirky Wink Powerstrip",
    "83cd8ae6-b742-4ce1-8a9a-2a02a084bfef" => "Quirky Wink Spotter",
    "f65b4e32-79b6-4678-9ad1-1315db1a805e" => "RGBW Light",
    "77ec5b76-e4a1-4127-bb97-1f23c80afa3d" => "Secure Dimmer",
    "7cb4aa6c-7d54-4969-b08a-bb3d22e8122d" => "Simulated Alarm",
    "7085326a-05d3-4175-9c05-93c6904b5fef" => "Simulated Button",
    "9c78f10f-d71c-4bbd-a164-cfc6fc3729dc" => "Simulated Color Control",
    "3237c3c4-7f74-4fd0-89db-9bbcf945feaa" => "Simulated Contact Sensor",
    "5a476918-a33b-45cd-a274-6cf94611ba75" => "Simulated Garage Door Opener",
    "8c438f30-9f29-45e2-ac8b-ed1bb7e7e3f3" => "Simulated Lock",
    "2d629fba-7b73-432a-8a84-0c810a744be8" => "Simulated Minimote",
    "853d865e-1d6c-4dcf-87a3-879b7757b9d7" => "Simulated Motion Sensor",
    "0d8244b9-e242-4e38-8f30-834b0753a239" => "Simulated Presence Sensor",
    "ac1759bd-cc1c-41ef-a770-ce0e6e86760c" => "Simulated Smoke Alarm",
    "c5b2dbad-73cb-4e12-bf41-f08742e8e560" => "Simulated Switch",
    "420a5067-a4b9-4858-bdc6-a088903e78b2" => "Simulated Temperature Sensor",
    "9264f29a-4470-4332-b689-bc5cfe7c9bfc" => "Simulated Thermostat",
    "fa6abb9e-b44e-4fbd-9820-76a2b56305a4" => "Simulated Water Sensor",
    "1468389b-8fdd-484b-970e-9a85757617b7" => "Simulated Water Valve",
    "5d3d201d-c4b4-4354-81e9-39d6d728e2c0" => "Smart Block",
    "d856d0a8-2012-4a36-afcc-86dcf48831bd" => "Smart Body Analyzer",
    "8a9d4b1e3b9b1fe3013b9b206c87000f" => "SmartAlert Siren",
    "90ab35ab-8487-4ea7-b9bf-97d0fdbd5fc5" => "SmartPower Dimming Outlet",
    "575cca07-1d9f-4b3d-9fda-c663d8d53051" => "SmartPower Outlet",
    "266372d8-3292-11e2-9c79-22000a1dc790" => "SmartPower Outlet V1",
    "8a3e510a3d460f4f013d460fe3050002" => "SmartSense Camera",
    "8a53ab1f3d88225d013d882303fb0001" => "SmartSense Garage Door Multi",
    "f232a760-0b09-4449-a96c-23a03e93f86a" => "SmartSense Garage Door Sensor Button",
    "26637f76-3292-11e2-9c79-22000a1dc790" => "SmartSense Moisture",
    "1b76003a-c010-4921-9e32-debea8be87c2" => "SmartSense Moisture Sensor",
    "26637ec2-3292-11e2-9c79-22000a1dc790" => "SmartSense Motion",
    "26637ec2-3292-11e2-9c79-22000a1dc790" => "SmartSense Motion",
    "821425e6-b305-468a-9a27-db0e4852ce3a" => "SmartSense Motion Sensor",
    "896ae842-e6d0-4b9b-a252-05377186c029" => "SmartSense Motion/Temp Sensor",
    "26637e0e-3292-11e2-9c79-22000a1dc790" => "SmartSense Multi",
    "258ee648-d846-4702-bdfb-475a7ed047b4" => "SmartSense Multi + Graph",
    "52517166-43f9-4cbf-8bc2-7da4e8df5614" => "SmartSense Multi Sensor",
    "dd6fe142-0c6c-4ee0-b15c-19b18a4727f7" => "SmartSense Open/Closed Accelerometer Sensor",
    "6caaddcc-dfbc-4378-b6d0-668631278542" => "SmartSense Open/Closed Sensor",
    "98a1be77-339a-4327-ba98-b6383c7d8364" => "SmartSense Temp/Humidity",
    "c7421ab5-e60c-4d8c-90da-bd66acfa91af" => "SmartSense Temp/Humidity Sensor",
    "8a3e510a3d460f4f013d460fdca50000" => "SmartSense Virtual Open/Closed",
    "8a70b39f3e5dbc6e013e660916180f3d" => "SmartWeather Station Tile",
    "8a9d4b1e3b8af959013b8af9d183000e" => "Spark",
    "a6efaf4e-00ab-4e61-a223-0473e24050c0" => "Sylvania Ultra iQ",
    "a7ee501a-6400-49b7-8270-407952255f8f" => "TCP Bulb",
    "266376f2-3292-11e2-9c79-22000a1dc790" => "Temperature Sensor",
    "8a53ab1f3d88225d013d882308ce0004" => "Thing",
    "1f663645-5903-4880-b1c4-a32e928b92cd" => "Tyco Door/Window Sensor",
    "2663785a-3292-11e2-9c79-22000a1dc790" => "Unknown",
    "b100c5ad-6d43-4adf-89f9-4109995e059d" => "Wattvision",
    "895a06d0-bbe6-4866-9d40-71293086e129" => "WeMo Bulb",
    "ace4ba69-5fed-4dc1-b7d4-4f7b60d0e77f" => "Wemo Light Switch",
    "c11d956b-779c-4ae7-a630-5b100d8939c9" => "Wemo Motion",
    "5f3161fb-e8a8-45e7-99c8-8a5cb5b90f69" => "Wemo Switch",
    "19e06f76-a3fc-4667-a095-146a52da311e" => "Wireless Scale",
    "b941fb87-d7c4-47ba-b31f-38748ed7aff1" => "Yoics Camera",
    "8ac43eda3f434b8d013f434c78c60006" => "Z-Wave Controller",
    "8ac43eda3f434b8d013f434c6a8d0000" => "Z-Wave Device",
    "45eeb48f-cdfb-4cff-b536-eaf1396c3b9c" => "Z-Wave Device Multichannel",
    "813a1f28-7ec4-4f6a-b5a2-d1ffd8609ca5" => "Z-Wave Door/Window Sensor",
    "548b3451-701a-4244-b66a-40d08564dc47" => "Z-Wave Garage Door Opener",
    "8a2a823b3c988884013c98891a8a0003" => "Z-Wave Lock",
    "d9877312-f804-4080-b1aa-c78a949365c7" => "Z-Wave Metering Dimmer",
    "8a8a038b3f1faad3013f1faccd0a0006" => "Z-Wave Metering Switch",
    "34e7c55a-5d0e-4dc6-8da4-96cdc83b84cb" => "Z-Wave Motion Sensor",
    "620e0f0e-fd25-4143-9935-a6a6a0145bc1" => "Z-Wave Relay",
    "8ac43eda3f434b8d013f434c7cdf0008" => "Z-Wave Remote",
    "8ac43eda3f434b8d013f434c71220004" => "Z-Wave Sensor",
    "2b3c4e47-8f59-42d6-a7d8-696504a1c03f" => "Z-Wave Siren",
    "26d69a34-ada2-42dc-9e45-445c78ae2619" => "Z-Wave Smoke Alarm",
    "26638034-3292-11e2-9c79-22000a1dc790" => "Z-Wave Switch",
    "6c434282-f211-41c7-b73b-91630dd66df7" => "Z-Wave Switch Secure",
    "8a2a823b3c3b464a013c3b46e9d70011" => "Z-Wave Thermostat",
    "8a3e510a3d460f4f013d460fe4c90004" => "Z-Wave Virtual Momentary Contact Switch",
    "1e88cabe-5ba4-4647-9a23-0ad5b887f94b" => "Z-Wave Water Sensor",
    "cd7479a9-b067-4c92-b646-df0e690714c8" => "Zen Thermostat",
    "52969956-9ba8-46ba-873e-1bb46cfef033" => "ZigBee Dimmer",
    "68003de7-ef45-4727-b5a2-ec502c1312ac" => "ZigBee Dimmer Power",
    "f7bb8bcb-32ee-4ef0-a86d-517f083d7dbd" => "ZigBee Lock",
    "a939a297-64e7-4b3b-ba6c-1d7a7e367ae4" => "ZigBee Switch",
    "be5e79b7-e72b-417a-ba23-22fc0abcef16" => "ZigBee Switch Power",
    "87c54d71-30ab-4a16-ae60-4b8cc62317fa" => "ZigBee White Color Temperature Bulb",
    "0977a809-fe12-4cee-962c-2b5ee7f53aaf" => "Zigbee Hue Bulb",
    "6534a2df-2981-4783-8457-2bdfded2c7b5" => "Zigbee Valve",
    "a88f2733-bca1-455e-bd25-20789850f426" => "netatmo-outdoor"
);

if(!file_exists($rootDirectory.$pathToSave)){
    mkdir($rootDirectory.$pathToSave, 0777, true);
}

foreach($devices as $id => $deviceName){
	$deviceFileName = preg_replace('#[^a-z0-9]#i', '-', $deviceName).'.groovy';
	$deviceFileName = preg_replace('#-+#', '-', $deviceFileName);
	
	echo 'Getting: '.$deviceName.PHP_EOL;
	echo 'FileName: '.$deviceFileName.PHP_EOL;
	
	// create a new cURL resource
	$ch = curl_init();
	
	// set URL and other appropriate options
	curl_setopt($ch, CURLOPT_URL, $deviceUrl.$id);
	curl_setopt($ch, CURLOPT_HEADER, false);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_COOKIE, 'JSESSIONID='.$sessionId.';');
	
	// grab URL and pass it to the browser
	$result = curl_exec($ch);
	$info = curl_getinfo($ch);
	
	if(curl_errno($ch) == 0 && $info['http_code'] < 400) {
    	$fullPath = $rootDirectory.$pathToSave.$deviceFileName;
    	file_put_contents($fullPath, $result);
	}else{
	    echo '-- Skipped last one --'.PHP_EOL;
	}
	
	// close cURL resource, and free up system resources
	curl_close($ch);
	
	echo PHP_EOL;
}
