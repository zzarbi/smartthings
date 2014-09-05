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
	'8a8a038b3f1faad3013f1fad0481000c' => 'Aeon Home Energy Meter',
    '8a9c25843e37f441013e37f51a780009' => 'Aeon Illuminator Module',
    '2f981316-8b6f-4c19-b031-9ae53ff4c95a' => 'Aeon Key Fob',
    '7a23cd3e-ec6e-42ba-aded-4045da19843e' => 'Aeon Minimote',
    '8a9d4b1e3bd91ce9013bd91d728f0010' => 'Aeon Multisensor',
    '8a9c25843e37f441013e37f511d90007' => 'Aeon Outlet',
    '26637bf2-3292-11e2-9c79-22000a1dc790' => 'Arduino ThingShield',
    '5b1fa934-132b-4b90-b7b3-1094bae1a8ef' => 'CentraLite Dimmer',
    '575cca07-1d9f-4b3d-9fda-c663d8d53051' => 'CentraLite Switch',
    '4018627f-fccc-4514-9c31-a853e6036326' => 'CentraLite Thermostat',
    'bdefadfd-d59f-4d54-82c2-5fbb3ac6965f' => 'Danalock',
    '8a9d4b1e3b9b1fe3013b9b206a7f000d' => 'Dimmer Switch',
    '8a9d4b1e3b6c74b9013b6c752771000b' => 'Door Shield',
    'eea4ae16-aaab-4a53-a87c-8d82f144f09a' => 'Dropcam',
    '359eb4ba-0751-4394-aa01-fe81c6e0a12a' => 'Ecobee Thermostat',
    '8aa8a6b23f769c09013f769e8f2f0003' => 'Everspring Flood Sensor',
    '8a1118243f879a60013f8fe4efdb2812' => 'Fortrezz Water Valve',
    'dc16563c-8b89-4756-8415-169d72a7ffee' => 'Foscam',
    'be174007-2261-4585-951a-497ffd945517' => 'GE Link Bulb',
    'fef2e8d4-a1c4-4e4a-ba5f-221b8a0713c2' => 'HomeSeer Multisensor',
    '6bfd4c3f-b31a-4af4-b457-71ad395699ad' => 'Hue Bridge',
    '48b39485-7ebc-4c80-bf6e-9a653a2d1c95' => 'Hue Bulb',
    '2adc2264-51be-4e2e-bfd9-d3797408f862' => 'Life360  User',
    '2663758a-3292-11e2-9c79-22000a1dc790' => 'Light Sensor',
    '8a9d4b1e3bfce38a013bfce42d360015' => 'Mobile Presence',
    '8a1814663e5dbf7d013e602ecece0248' => 'Momentary Button Tile',
    '26637648-3292-11e2-9c79-22000a1dc790' => 'Motion Detector',
    '8a70b39f3e5dbc6e013e602bba0a0272' => 'On/Off Button Tile',
    '26637d50-3292-11e2-9c79-22000a1dc790' => 'On/Off Shield',
    '266374ae-3292-11e2-9c79-22000a1dc790' => 'Open/Closed Sensor',
    '266377a6-3292-11e2-9c79-22000a1dc790' => 'Particulate Detector',
    '26637c9c-3292-11e2-9c79-22000a1dc790' => 'Pet Feeder Shield',
    '75fe2090-7e25-43a1-a3f8-5c7cbcf7dbac' => 'Plant Link',
    '77ec5b76-e4a1-4127-bb97-1f23c80afa3d' => 'Secure Dimmer',
    'd856d0a8-2012-4a36-afcc-86dcf48831bd' => 'Smart Body Analyzer',
    '8a9d4b1e3b9b1fe3013b9b206c87000f' => 'SmartAlert Siren',
    '266372d8-3292-11e2-9c79-22000a1dc790' => 'SmartPower Outlet',
    '8a3e510a3d460f4f013d460fe3050002' => 'SmartSense Camera',
    '8a53ab1f3d88225d013d882303fb0001' => 'SmartSense Garage Door Multi',
    'f232a760-0b09-4449-a96c-23a03e93f86a' => 'SmartSense Garage Door Sensor Button',
    '26637f76-3292-11e2-9c79-22000a1dc790' => 'SmartSense Moisture',
    '1b76003a-c010-4921-9e32-debea8be87c2' => 'SmartSense Moisture Sensor',
    '26637ec2-3292-11e2-9c79-22000a1dc790' => 'SmartSense Motion',
    '896ae842-e6d0-4b9b-a252-05377186c029' => 'SmartSense Motion/Temp Sensor',
    '26637e0e-3292-11e2-9c79-22000a1dc790' => 'SmartSense Multi',
    '258ee648-d846-4702-bdfb-475a7ed047b4' => 'SmartSense Multi + Graph',
    '6caaddcc-dfbc-4378-b6d0-668631278542' => 'SmartSense Open/Closed Sensor',
    '2663790e-3292-11e2-9c79-22000a1dc790' => 'SmartSense Presence',
    'c7421ab5-e60c-4d8c-90da-bd66acfa91af' => 'SmartSense Temp/Humidity Sensor',
    '8a3e510a3d460f4f013d460fdca50000' => 'SmartSense Virtual Open/Closed',
    '8a70b39f3e5dbc6e013e660916180f3d' => 'SmartWeather Station Tile',
    'cb5f0ce6-1539-4fc0-8ffe-d6eda9a94287' => 'Sonos Player',
    '8a9d4b1e3b8af959013b8af9d183000e' => 'Spark',
    'a6efaf4e-00ab-4e61-a223-0473e24050c0' => 'Sylvania Ultra iQ',
    '266376f2-3292-11e2-9c79-22000a1dc790' => 'Temperature Sensor',
    '8a53ab1f3d88225d013d882308ce0004' => 'Thing',
    '2663785a-3292-11e2-9c79-22000a1dc790' => 'Unknown',
    'ace4ba69-5fed-4dc1-b7d4-4f7b60d0e77f' => 'Wemo Light Switch',
    'c11d956b-779c-4ae7-a630-5b100d8939c9' => 'Wemo Motion',
    '5f3161fb-e8a8-45e7-99c8-8a5cb5b90f69' => 'Wemo Switch',
    '19e06f76-a3fc-4667-a095-146a52da311e' => 'Wireless Scale',
    'b941fb87-d7c4-47ba-b31f-38748ed7aff1' => 'Yoics Camera',
    '8ac43eda3f434b8d013f434c78c60006' => 'Z-Wave Controller',
    '8ac43eda3f434b8d013f434c6a8d0000' => 'Z-Wave Device',
    '813a1f28-7ec4-4f6a-b5a2-d1ffd8609ca5' => 'Z-Wave Door/Window Sensor',
    '8a2a823b3c988884013c98891a8a0003' => 'Z-Wave Lock',
    '8a8a038b3f1faad3013f1faccd0a0006' => 'Z-Wave Metering Switch',
    '34e7c55a-5d0e-4dc6-8da4-96cdc83b84cb' => 'Z-Wave Motion Sensor',
    '620e0f0e-fd25-4143-9935-a6a6a0145bc1' => 'Z-Wave Relay',
    '8ac43eda3f434b8d013f434c7cdf0008' => 'Z-Wave Remote',
    '8ac43eda3f434b8d013f434c71220004' => 'Z-Wave Sensor',
    '26d69a34-ada2-42dc-9e45-445c78ae2619' => 'Z-Wave Smoke Alarm',
    '26638034-3292-11e2-9c79-22000a1dc790' => 'Z-Wave Switch',
    '8a2a823b3c3b464a013c3b46e9d70011' => 'Z-Wave Thermostat',
    '8a3e510a3d460f4f013d460fe4c90004' => 'Z-Wave Virtual Momentary Contact Switch',
    '1e88cabe-5ba4-4647-9a23-0ad5b887f94b' => 'Z-Wave Water Sensor',
    '52969956-9ba8-46ba-873e-1bb46cfef033' => 'ZigBee Dimmer',
    '0977a809-fe12-4cee-962c-2b5ee7f53aaf' => 'Zigbee Hue Bulb',
    '6534a2df-2981-4783-8457-2bdfded2c7b5' => 'Zigbee Valve',
    '017a57fa-5f47-4faf-96b3-183eed48ec9e' => 'life360-user'
);

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
	
	$fullPath = $rootDirectory.$pathToSave.$deviceFileName.PHP_EOL;
	
	file_put_contents($fullPath, $result);
	
	// close cURL resource, and free up system resources
	curl_close($ch);
	
	echo PHP_EOL;
}
