<?php /**
 * @author Nicolas Cerveaux
 * 
 * This script will download the source code of every apps listed above from Smartthings Websites
 */

$sessionId = 'REPLACE_WITH_YOUR_SESSION_ID'; // NEED TO BE REPLACED
$pathToSave = '/app/smartthings/';

$deviceUrl = 'https://graph.api.smartthings.com/ide/device/example/';
$categoryUrl = 'https://graph.api.smartthings.com/ide/app/sharedSmartAppsByCategoryId/';
$appUrl = 'https://graph.api.smartthings.com/ide/app/sharedSmartAppByVersionId/';
$exampleUrl = 'https://graph.api.smartthings.com/ide/app/sharedSmartAppByVersionId/';
$rootDirectory = realpath(dirname(__FILE__).'/../');

// List of Categories id/name
$categories = array(
	'8a818a9b39c0de7f0139c0dec27a0014'=>'Convenience',
	'8a818a9b39c0de7f0139c0dec5760025'=>'Family',
	'8a818a9b39c0de7f0139c0dec6ff002f'=>'Fun & Social',
	'8a818a9b39c0de7f0139c0dec7f50035'=>'Green Living',
	'8a818a9b39c0de7f0139c0dec82b0036'=>'Health & Wellness',
	'8a53ab1f3d88225d013d8823190c0008'=>'Mode Magic',
	'7911a57e-2e71-11e2-b536-1a6bcdc263a7'=>'My Apps',
	'8a818a9b39c0de7f0139c0dec911003c'=>'Pets',
	'8a818a9b39c0de7f0139c0dec9d20041'=>'Safety & Security',
	'2d94ac01-92dd-4fac-b212-cda40c989920'=>'SmartThings Labs'
);

$exampleApps = array(
    "8a9c25843e38742c013e4e14f1b226b8" => "Alfred Workflow",
    "3c50af4d-1c0e-4443-95ce-37cd63a6f50e" => "Auto Humidity Vent",
    "8a9d4b1e3b9b1fe3013b9b2071dd001a" => "Big Turn OFF",
    "8a9d4b1e3b9b1fe3013b9b2070f60018" => "Big Turn ON",
    "8a2aa4273dd0b950013dd0ba0621000a" => "Bon Voyage",
    "a698252e-8580-4a53-bc1e-e04652a8a0e6" => "Bose SoundTouch (Connect)",
    "f9faba0c-c3c8-4e17-aa90-e3bdbcf46874" => "Bright When Dark And/Or Bright After Sunset",
    "dece88ce-246b-11e2-af64-22000a9d0d2b" => "Brighten Dark Places",
    "dece792e-246b-11e2-af64-22000a9d0d2b" => "Brighten My Path",
    "1ed27204-8682-403a-8974-248a122721d4" => "Button Controller",
    "afc02ffc-ae47-4577-90ed-073d9cc513ca" => "CO2 Vent",
    "b622b519-cef1-4769-9e6b-510286c8871b" => "Carpool Notifier",
    "b606545b-38f0-4712-b1d2-38040c9edfe9" => "Close The Valve",
    "29e8e7aa-dbcd-4fa2-a313-808b6fcb7715" => "Color Coordinator",
    "cda89c12-3464-4de7-a4d4-4f3578cb292d" => "Curb Control",
    "8a2aa4273dd0b950013dd0ba01ee0006" => "Curling Iron",
    "dece7aa0-246b-11e2-af64-22000a9d0d2b" => "Darken Behind Me",
    "9c315e66-6716-4529-99d4-e68da4bc1c59" => "Door Knocker",
    "e3a4227d-b1c9-42c6-aed2-6423e860fb86" => "Door Lock Code Distress Message",
    "8acc475c3cc9e8d7013cd3ff3d7c0270" => "Double Tap",
    "3d11eec8-c0e7-48e2-a0e5-4066da89c916" => "Dry the Wetspot",
    "da964c2f-64b6-4182-9218-51a13dec772f" => "Ecobee (Connect)",
    "decf48d6-246b-11e2-af64-22000a9d0d2b" => "Elder Care: Daily Routine",
    "8a53ab1f3dd0b94d013de4e394be14e3" => "Elder Care: Slip &amp; Fall",
    "efff360d-6963-4bbd-9c78-360cad3d48a8" => "Energy Alerts",
    "05e56b05-277d-401d-956b-9ac72f4777b7" => "Energy Saver",
    "a7b7895c-c2f1-4b42-9808-3885756be7c7" => "Enhanced Auto Lock Door",
    "ded09498-246b-11e2-af64-22000a9d0d2b" => "Feed My Pet",
    "8a2a823b3c25575e013c2564bcb60023" => "Flood Alert!",
    "8c3c8953-1d9c-4b0a-ba6a-3c912e7f8d5e" => "Forgiving Security",
    "8a2a823b3c6f4a81013c6f4b1a2e0001" => "Garage Door Monitor",
    "8a9d4b1e3bfd839b013bfd843d380018" => "Garage Door Opener",
    "8a53ab1f3d88225d013d882316e40007" => "Gentle Wake Up",
    "8a53ab1f3d88225d013d88231b89000c" => "Good Night",
    "8f97f27a-4b13-442c-93e2-6f79d16616c6" => "Goodnight Ubi",
    "8a2aa4273dd0b950013dd0ba076e000c" => "Greetings Earthling",
    "ded000b4-246b-11e2-af64-22000a9d0d2b" => "Habit Helper",
    "dee1feb7-4c03-4d90-b976-66c7250500a9" => "Hall Light: Welcome Home",
    "8a9d4b1e3ae70cd2013ae70d3a7e0005" => "Has Barkley Been Fed?",
    "70b059c4-f9fe-4ad2-aebe-0c7023e30a1c" => "Hello, Home Phrase Director",
    "364a47e4-936c-4309-9966-d8183790c742" => "Hue (Connect)",
    "4a781946-f5d1-4707-880c-ea6cb5030d5e" => "Hue Mood Lighting",
    "ff38ae58-f4f6-432b-885e-221fc1ac2515" => "Humidity Alert!",
    "edca3bdb-5d25-4d40-a918-3bf3abe7d53b" => "Initial State Event Streamer",
    "8a088aa83eae0765013eae0a2d520005" => "It Moved",
    "dece6e34-246b-11e2-af64-22000a9d0d2b" => "It's Too Cold",
    "58228763-f8ae-40c5-b3ff-336b6c3e15e5" => "It's Too Hot",
    "7706023c-f09d-45a5-9cd2-d9a4a7b68e1c" => "Jawbone Panic Button",
    "a4d7f388-08af-43fc-a927-296069472c9e" => "Jawbone UP (Connect)",
    "34c82cd7-ada4-4dc2-9647-4884dd937d4a" => "Jenkins Notifier",
    "8a2a823b3c3b464a013c3b46fa04001c" => "Keep Me Cozy",
    "8a9f19b33d40d9c5013d40da6ccb0006" => "Keep Me Cozy II",
    "f213e7ae-4b8f-4c36-8f0c-0e8412db143c" => "LIFX (Connect)",
    "69fed8fe-55a6-4bb0-a310-667065ae6784" => "Laundry Monitor",
    "17911e65-ca3e-4462-8065-b572f8472bca" => "Left It Open",
    "7f0ef7e6-22c8-4ede-8c5d-eafc04912d92" => "Let There Be Dark!",
    "dece872a-246b-11e2-af64-22000a9d0d2b" => "Let There Be Light!",
    "5aa9ba95-e44f-479f-a682-8112852d5ee3" => "Life360 (Connect)",
    "8a9d689b3cdf9c35013cdf9cc10a0004" => "Light Follows Me",
    "8a2a823b3c7901a0013c7d07c77e012f" => "Light Up the Night",
    "eb9428f9-aab1-4216-b815-960ce106f477" => "Lighting Director",
    "56cba926-c520-4718-a3cd-c31d219572f9" => "Lights Off with No Motion and Presence",
    "dece85a4-246b-11e2-af64-22000a9d0d2b" => "Lights Off, When Closed",
    "8acc475c3cbe12b7013cc764383e016e" => "Lock It When I Leave",
    "5f0f357b-0ae8-4662-9cd1-bf9264f70a10" => "Lock it at a specific time",
    "d05b1e4f-9bbf-4fd5-b5b9-ddba1e1bbed9" => "Lock it at a specific time",
    "b7384b02-7197-48f9-812d-36d0121b5f3f" => "Logitech Harmony (Connect)",
    "dece7460-246b-11e2-af64-22000a9d0d2b" => "Mail Arrived",
    "8a9f19b33d4b4114013d4bab00d50071" => "Make It So",
    "8a9d4b1e3ae70cd2013ae70d395f0003" => "Medicine Reminder",
    "fc6425a6-a24a-499f-9ad1-d5d97fc2f122" => "Mini Hue Controller",
    "ad571cf5-1152-4695-bed0-963da5228efb" => "Mood Cube",
    "691225dd-c3c2-4116-938a-8e35c5f63b20" => "My Light Toggle",
    "fd734088-b92a-4def-910e-1d791e48fc2c" => "Netatmo (Connect)",
    "9ff89c29-2757-4030-ae07-c00dc3d8c0d4" => "Nobody Home",
    "8a2aa4273dd0b950013dd0b9fe4b0002" => "Notify Me When",
    "8a2a823b3c470412013c4704c06c001b" => "Notify Me When It Opens",
    "8c7ea09e-4c17-4801-a7ef-ff1a30492f68" => "Notify Me With Hue",
    "02360102-16fb-4daf-8ca6-fa5ed4b10c5d" => "ObyThing Music (Connect)",
    "9239e796-e2b9-4a4f-ae59-df0dc8888f54" => "ObyThing Music SmartApp",
    "8a2aa4273e186989013e1d8b90ff0469" => "Once a Day",
    "fedd93aa-50c7-4206-a9a4-95b12b21327a" => "Photo Burst When...",
    "8a2a800c3cb74d95013cb74e386c0007" => "Power Allowance",
    "8a2a800c3cb74d95013cb74e3c4b000b" => "Presence Change Push",
    "8a2a800c3cb74d95013cb74e3b420009" => "Presence Change Text",
    "73392e0c-54b4-48bd-b6d2-62cd1447a323" => "Quirky (Connect)",
    "1e8439ae-d95f-455f-bdc6-44f723c7ed20" => "Ready For Rain",
    "8a2aa4273dd0b950013dd0ba006b0004" => "Ridiculously Automated Garage Door",
    "8a53ab1f3d88225d013d882319ec000a" => "Rise and Shine",
    "ff93d14e-deb5-4c3a-bbc9-d2213bd225e4" => "Routine Director",
    "69f0c2b4-a19b-4867-bf3d-8d3dd3db28fc" => "Safe Watch",
    "8a9c25843e67270e013e6fa9401f10aa" => "Scheduled Mode Change",
    "ded1098c-246b-11e2-af64-22000a9d0d2b" => "Severe Weather Alert",
    "bdd740df-ec2c-45fd-bd44-55b3d3253a0a" => "Sleepy Time",
    "0a8dae0c-4a77-43b9-a02e-71dfe6ef77c6" => "Smart Alarm",
    "a41b599e-4f49-4de5-92b9-3ff69947af2c" => "Smart Home Ventilation",
    "b58476de-bdd7-4809-9505-e034181b140a" => "Smart Humidifier",
    "ec99bf68-a648-406f-a967-a4ae321aa014" => "Smart Light Timer, X minutes unless already on",
    "8a9d689b3d2813e3013d28148c300005" => "Smart Nightlight",
    "8a2aa4273dd0b950013dd0ba04180008" => "Smart Security",
    "3d77aa3e-2e90-4822-b11a-6932f1cc8d91" => "Smart turn it on",
    "dc721cd6-67e2-4ac8-8359-b234a4f02d8c" => "SmartWeather Station Controller",
    "306f1f20-1e24-4abd-ba19-659525b61043" => "Speaker Control",
    "e437e465-c70d-41fe-a0cb-adf630dd35a0" => "Speaker Mood Music",
    "096eb84a-f02a-4f9b-a595-4b0f0131f604" => "Speaker Notify with Sound",
    "d122e8b0-8b95-418e-8757-4cfef1bb3d58" => "Speaker Weather Forecast",
    "8ba19f97-caeb-44c2-8dd7-e617f8fec107" => "Step Notifier",
    "8a3e3fde3e67257e013e74caa33a1912" => "Sunrise/Sunset",
    "24c0bd2e-d4a5-426b-816c-86257397ba73" => "Switch Activates Home Phrase",
    "8eb1dcbb-8219-4992-88e0-cd74515116fb" => "Switch Changes Mode",
    "cc93f638-3355-46bd-8aed-d44891cffc96" => "Talking Alarm Clock",
    "cbcd6bc3-51f0-459c-ae55-7489c449caa5" => "Tcp Bulbs (Connect)",
    "dece8252-246b-11e2-af64-22000a9d0d2b" => "Text Me When It Opens",
    "8a9d4b1e3b781874013b7818f4120014" => "Text Me When There's Motion",
    "dece7604-246b-11e2-af64-22000a9d0d2b" => "Text Me When There's Motion and I'm Not Here",
    "8a1814663e5dbf7d013e6038507a025c" => "The Big Switch",
    "8ad51b2a3d036dda013d0e80cd66066e" => "The Flasher",
    "dece77a8-246b-11e2-af64-22000a9d0d2b" => "The Gun Case Moved",
    "a3e8c079-8f6d-4ce4-963a-6d18fd35870f" => "Thermostat Auto Off",
    "d46ee21c-753b-4271-bf01-169f14b8325e" => "Thermostat Mode Director",
    "7e1ea054-561e-46d9-8726-3e3d4e84ac48" => "Thermostat Window Check",
    "8a2a823b3c648a9e013c648b3ab50002" => "Turn It On For 5 Minutes",
    "dece7118-246b-11e2-af64-22000a9d0d2b" => "Turn It On When I'm Here",
    "8aee96b33d18558e013d1d7d06d904c2" => "Turn It On When It Opens",
    "56535b8f-e5aa-40a7-bace-b2e2140075e7" => "Turn On Only If I Arrive After Sunset",
    "8a3e3fde3e3877ce013e3c8a42a404f9" => "Undead Early Warning",
    "8acc475c3cbe12b7013cc76df6380175" => "Unlock It When I Arrive",
    "439f6d8a-5e47-48d8-a975-c8f720f5df91" => "Vacation Lighting Director",
    "8a2a823b3c7fa476013c874a605504d0" => "Virtual Thermostat",
    "c0e01cca-b652-4c65-b3e2-8f4d56f349c3" => "Weather Underground PWS Connect",
    "ec59d456-e788-4bb3-b71a-d522abe77360" => "Weather Windows",
    "3ad195e4-ba18-44ab-99c8-d2024c3caad1" => "Wemo (Connect)",
    "8aee96b33d363f8b013d3696c1780019" => "When It's Going to Rain",
    "0a0a96ee-ff10-45a2-a9e3-0f339c78f813" => "Whole House Fan",
    "ff7700e6-c9ff-4f5e-89dc-34aa9170c86d" => "Withings Manager",
    "4a6800a8-5af1-4f6e-b53d-7951107b42d2" => "Working From Home"
);


/**
 * Process a CURL request
 * 
 * @param string $url
 * @param string $sessionId
 */
function request($url, $sessionId){
	// create a new cURL resource
	$ch = curl_init();
	
	// set URL and other appropriate options
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_HEADER, false);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_COOKIE, 'JSESSIONID='.$sessionId.';');
	
	// grab URL and pass it to the browser
	$result = curl_exec($ch);
	
	// close cURL resource, and free up system resources
	curl_close($ch);
	
	return $result;
}

// manage samples
echo 'Getting Examples:'.PHP_EOL;
$categoryPath = $rootDirectory.$pathToSave.'Examples';
if(!file_exists($categoryPath)){
    mkdir($categoryPath, 0777, true);
}

foreach($exampleApps as $appId => $appName) {
    $appNameFile = preg_replace('#[^a-z0-9]#i', '-', $appName);
    $appNameFile = preg_replace('#-+#', '-', $appNameFile).'.groovy';
    echo '- Downloading '.$appName.PHP_EOL;

    $filePath = $categoryPath.'/'.$appNameFile;
    $result = request($exampleUrl.$appId, $sessionId);
    if($fileData = json_decode($result)){
        file_put_contents($filePath, $fileData->code);
    }
}

// manage rest of categories
foreach($categories as $id => $categoryName){
	$categoryNameFolder = preg_replace('#[^a-z0-9]#i', '-', $categoryName);
	$categoryNameFolder = preg_replace('#-+#', '-', $categoryNameFolder);
	$categoryPath = $rootDirectory.$pathToSave.$categoryNameFolder;
	
	echo 'Getting Category: '.$categoryName.PHP_EOL;
	if(!file_exists($categoryPath)){
		mkdir($categoryPath, 0777, true);
	}
	
	$categoryData = request($categoryUrl.$id, $sessionId);
	print_r($categoryUrl.$id);
	if($json = json_decode($categoryData)){
		if(is_array($json)){
			foreach($json as $obj){
				$appId = $obj->id;
				$appName = $obj->name;
				$appNameFile = preg_replace('#[^a-z0-9]#i', '-', $appName);
				$appNameFile = preg_replace('#-+#', '-', $appNameFile).'.groovy';
				echo '- Downloading '.$appName.PHP_EOL;
				$filePath = $categoryPath.'/'.$appNameFile;
				$result = request($appUrl.$appId, $sessionId);
				if($fileData = json_decode($result)){
					file_put_contents($filePath, $fileData->code);
				}
			}
			echo PHP_EOL;
		}
	}
	
	echo PHP_EOL;
}
