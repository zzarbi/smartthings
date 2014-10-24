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
    "8a9c25843e38742c013e4e14f1b226b8" => 'Alfred Workflow',
    "8a9d4b1e3b9b1fe3013b9b2071dd001a" => 'Big Turn OFF',
    "8a9d4b1e3b9b1fe3013b9b2070f60018" => 'Big Turn ON',
    "8a2aa4273dd0b950013dd0ba0621000a" => 'Bon Voyage',
    "dece88ce-246b-11e2-af64-22000a9d0d2b" => 'Brighten Dark Places',
    "dece792e-246b-11e2-af64-22000a9d0d2b" => 'Brighten My Path',
    "1ed27204-8682-403a-8974-248a122721d4" => 'Button Controller',
    "b622b519-cef1-4769-9e6b-510286c8871b" => 'Carpool Notifier',
    "8a2aa4273dd0b950013dd0ba01ee0006" => 'Curling Iron',
    "dece7aa0-246b-11e2-af64-22000a9d0d2b" => 'Darken Behind Me',
    "8acc475c3cc9e8d7013cd3ff3d7c0270" => 'Double Tap',
    "c3fb300e-9cb4-4463-b5b0-d464ef1cc16e" => 'Dropcam (Connect)',
    "da964c2f-64b6-4182-9218-51a13dec772f" => 'Ecobee (Connect)',
    "decf48d6-246b-11e2-af64-22000a9d0d2b" => 'Elder Care: Daily Routine',
    "8a53ab1f3dd0b94d013de4e394be14e3" => 'Elder Care: Slip &amp; Fall',
    "ded09498-246b-11e2-af64-22000a9d0d2b" => 'Feed My Pet',
    "8a2a823b3c25575e013c2564bcb60023" => 'Flood Alert!',
    "8a2a823b3c6f4a81013c6f4b1a2e0001" => 'Garage Door Monitor',
    "8a9d4b1e3bfd839b013bfd843d380018" => 'Garage Door Opener',
    "8a53ab1f3d88225d013d882316e40007" => 'Gentle Wake Up',
    "8a53ab1f3d88225d013d88231b89000c" => 'Good Night',
    "8a2aa4273dd0b950013dd0ba076e000c" => 'Greetings Earthling',
    "ded000b4-246b-11e2-af64-22000a9d0d2b" => 'Habit Helper',
    "8a9d4b1e3ae70cd2013ae70d3a7e0005" => 'Has Barkley Been Fed?',
    "364a47e4-936c-4309-9966-d8183790c742" => 'Hue (Connect)',
    "4a781946-f5d1-4707-880c-ea6cb5030d5e" => 'Hue Mood Lighting',
    "8a088aa83eae0765013eae0a2d520005" => 'It Moved',
    "dece6e34-246b-11e2-af64-22000a9d0d2b" => 'It\'s Too Cold',
    "58228763-f8ae-40c5-b3ff-336b6c3e15e5" => 'It\'s Too Hot',
    "8a2a823b3c3b464a013c3b46fa04001c" => 'Keep Me Cozy',
    "8a9f19b33d40d9c5013d40da6ccb0006" => 'Keep Me Cozy II',
    "69fed8fe-55a6-4bb0-a310-667065ae6784" => 'Laundry Monitor',
    "17911e65-ca3e-4462-8065-b572f8472bca" => 'Left It Open',
    "dece872a-246b-11e2-af64-22000a9d0d2b" => 'Let There Be Light!',
    "5aa9ba95-e44f-479f-a682-8112852d5ee3" => 'Life360 (Connect)',
    "8a9d689b3cdf9c35013cdf9cc10a0004" => 'Light Follows Me',
    "8a2a823b3c7901a0013c7d07c77e012f" => 'Light Up the Night',
    "dece85a4-246b-11e2-af64-22000a9d0d2b" => 'Lights Off, When Closed',
    "8acc475c3cbe12b7013cc764383e016e" => 'Lock It When I Leave',
    "6800a17f-bd8d-4818-8ad0-43714d21ade5" => 'Logitech Harmony Connect',
    "d41b8a19-3d26-4b4b-bc7e-21637ff184ad" => 'Logitech Harmony Hub Control',
    "dece7460-246b-11e2-af64-22000a9d0d2b" => 'Mail Arrived',
    "8a9f19b33d4b4114013d4bab00d50071" => 'Make It So',
    "8a9d4b1e3ae70cd2013ae70d395f0003" => 'Medicine Reminder',
    "8a2aa4273dd0b950013dd0b9fe4b0002" => 'Notify Me When',
    "8a2a823b3c470412013c4704c06c001b" => 'Notify Me When It Opens',
    "8c7ea09e-4c17-4801-a7ef-ff1a30492f68" => 'Notify Me With Hue',
    "8a2aa4273e186989013e1d8b90ff0469" => 'Once a Day',
    "fedd93aa-50c7-4206-a9a4-95b12b21327a" => 'Photo Burst When...',
    "8a2a800c3cb74d95013cb74e386c0007" => 'Power Allowance',
    "8a2a800c3cb74d95013cb74e3c4b000b" => 'Presence Change Push',
    "8a2a800c3cb74d95013cb74e3b420009" => 'Presence Change Text',
    "73392e0c-54b4-48bd-b6d2-62cd1447a323" => 'Quirky (Connect)',
    "8a2aa4273dd0b950013dd0ba006b0004" => 'Ridiculously Automated Garage Door',
    "8a53ab1f3d88225d013d882319ec000a" => 'Rise and Shine',
    "8a9c25843e67270e013e6fa9401f10aa" => 'Scheduled Mode Change',
    "ded1098c-246b-11e2-af64-22000a9d0d2b" => 'Severe Weather Alert',
    "bdd740df-ec2c-45fd-bd44-55b3d3253a0a" => 'Sleepy Time',
    "8a9d689b3d2813e3013d28148c300005" => 'Smart Nightlight',
    "8a2aa4273dd0b950013dd0ba04180008" => 'Smart Security',
    "dc721cd6-67e2-4ac8-8359-b234a4f02d8c" => 'SmartWeather Station Controller',
    "ef0f0a6e-0b52-45c8-85dc-ff5312605f54" => 'Sonos (Connect)',
    "306f1f20-1e24-4abd-ba19-659525b61043" => 'Sonos Control',
    "e437e465-c70d-41fe-a0cb-adf630dd35a0" => 'Sonos Mood Music',
    "096eb84a-f02a-4f9b-a595-4b0f0131f604" => 'Sonos Notify with Sound',
    "d122e8b0-8b95-418e-8757-4cfef1bb3d58" => 'Sonos Weather Forecast',
    "8ba19f97-caeb-44c2-8dd7-e617f8fec107" => 'Step Notifier',
    "8a3e3fde3e67257e013e74caa33a1912" => 'Sunrise/Sunset',
    "cbcd6bc3-51f0-459c-ae55-7489c449caa5" => 'Tcp Bulbs (Connect)',
    "dece8252-246b-11e2-af64-22000a9d0d2b" => 'Text Me When It Opens',
    "8a9d4b1e3b781874013b7818f4120014" => 'Text Me When There\'s Motion',
    "dece7604-246b-11e2-af64-22000a9d0d2b" => 'Text Me When There\'s Motion and I\'m Not Here',
    "8a1814663e5dbf7d013e6038507a025c" => 'The Big Switch',
    "8ad51b2a3d036dda013d0e80cd66066e" => 'The Flasher',
    "dece77a8-246b-11e2-af64-22000a9d0d2b" => 'The Gun Case Moved',
    "8a2a823b3c648a9e013c648b3ab50002" => 'Turn It On For 5 Minutes',
    "dece7118-246b-11e2-af64-22000a9d0d2b" => 'Turn It On When I\'m Here',
    "8aee96b33d18558e013d1d7d06d904c2" => 'Turn It On When It Opens',
    "8a3e3fde3e3877ce013e3c8a42a404f9" => 'Undead Early Warning',
    "8acc475c3cbe12b7013cc76df6380175" => 'Unlock It When I Arrive',
    "8a2a823b3c7fa476013c874a605504d0" => 'Virtual Thermostat',
    "3ad195e4-ba18-44ab-99c8-d2024c3caad1" => 'Wemo (Connect)',
    "8aee96b33d363f8b013d3696c1780019" => 'When It\'s Going to Rain'
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
    mkdir($categoryPath);
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
		mkdir($categoryPath);
	}
	
	$categoryData = request($categoryUrl.$id, $sessionId);
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
