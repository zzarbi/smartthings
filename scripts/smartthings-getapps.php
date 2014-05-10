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
