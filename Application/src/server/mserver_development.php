<?php

require('config.php');
require('point_location.php');
require('kraje/extents.php');


function pointInRegion($regionId, $point) {
    $region_coords_string = file_get_contents("kraje/".$regionId.".coords");
    $region_coords = explode(",", $region_coords_string);
    //print_r($region_coords);
    $pointLocation2 = new pointLocation();
    $result = $pointLocation2->pointInPolygon($point, $region_coords);    
    return $result;
}

function getRegion($kraje, $lon, $lat) {
    foreach ($kraje as $value){
        if ($lon>=$value[1] && $lon<=$value[2] && $lat>=$value[3] && $lat<=$value[4]) { 
            if (pointInRegion($value[0], $lon." ".$lat) == "inside") {
                return $value[0];
            }
        }
    }
    return "OUT";
}

function uploadFile($id) {

    if (!isset($_FILES["fileToUpload"])) return "";

    $uid = uniqid();

    $target_dir = "/var/local/patrac/".$id."/";
    $target_file = $target_dir . $uid . "_" . basename($_FILES["fileToUpload"]["name"]);
    $uploadOk = 1;

    // Check if file already exists
    if (file_exists($target_file)) {
        $uploadOk = 0;
        return '';
    }
    
    // Check file size
    if ($_FILES["fileToUpload"]["size"] > 10000000) {
        $uploadOk = 0;
        return '';
    }

    // Check if $uploadOk is set to 0 by an error
    if ($uploadOk == 0) {
        // if everything is ok, try to upload file
        return '';
    } else {
        if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file)) {
            return $uid . "_" . basename( $_FILES["fileToUpload"]["name"]);
        } else {
            return '';
        }
    }
}

function checkLatLon($lat, $lon) {
    if ($lat != "" && $lon != "") {
        if (is_numeric($lat) && is_numeric($lon)) {
            return true;
        } else {
            return false;
        }
    } else {
        return false;
    }
}

function checkSystemId() {
    if (!ctype_alnum($_REQUEST["id"])) die("E;checkSystemId:1");
    $SQL = "SELECT id, status FROM system_users WHERE id = '".$_REQUEST["id"]."'";
    $res = mysql_query($SQL) or die("E;checkSystemId:2"); 
    $row = mysql_fetch_array($res);
    if ($row["id"] != $_REQUEST["id"]) die("E;checkSystemId:3");
}

function getSearches() {
    checkSystemId();

    $SQL = "SELECT id, status FROM system_users WHERE id = '".$_REQUEST["id"]."'";
    $res = mysql_query($SQL) or die("E;getSearches:0"); 
    $row = mysql_fetch_array($res);

    /*
        status
        sleeping - basic state,
        waiting - android knows that there is search,
        callonduty - operatop asks for help,
        onduty - searcher accepted the call
    */

    if (isset($_REQUEST["arrive"])) {
        // accept the search
        $SQL = "UPDATE system_users SET status = 'readyforduty', arrive = '".$_REQUEST["arrive"]."' WHERE id = '".$_REQUEST["id"]."'";
        // not accept the search
        if ($_REQUEST["arrive"] == "NKD") {
          $SQL = "UPDATE system_users SET status = 'sleeping', arrive = '".$_REQUEST["arrive"]."' WHERE id = '".$_REQUEST["id"]."'";
          echo "R;".$SQL;
        } else {
          echo "A;".$SQL;
        }
        mysql_query($SQL) or die("E;getSearches:1;".$SQL);
    }

    if (isset($_REQUEST["lon"]) && isset($_REQUEST["lat"]) && checkLatLon($_REQUEST["lon"], $_REQUEST["lat"])) {
        // save the position of the searcher
        $SQL = "UPDATE system_users SET lon = ".$_REQUEST["lon"].", lat = ".$_REQUEST["lat"].", status = 'waiting' WHERE id = '".$_REQUEST["id"]."'";
        mysql_query($SQL) or die("E;getSearches:2;".$SQL);
        echo "W;".$SQL;
    }

    if ($row["status"] == "sleeping") {
        $SQL = "SELECT count(searchid) ct FROM searches WHERE status = 'confirmed'";
        $res = mysql_query($SQL) or die("E;getSearches:3"); 
        $row = mysql_fetch_array($res);
        if ($row["ct"] > 0) {
          echo "S;".$row["ct"];
        } else {
          echo "N;".$row["ct"];
        }
    }

    if ($row["status"] == "readyforduty") {
        echo "T;readyforduty";
    }

    if ($row["status"] == "released") {
        echo "R;None";
    }
    
    if ($row["status"] == "waiting") {
        echo "T;waiting";
    }

    if ($row["status"] == "onduty") {
        echo "T;onduty";
    }

    // calltojoin
    $callStatus = "J";
    // callonduty
    if ($row["status"] == "callonduty") {
        $callStatus = "D";
    }

    if ($row["status"] == "callonduty" || $row["status"] == "calltojoin") {
        $SQL = "SELECT searchid FROM system_users WHERE id = '".$_REQUEST["id"]."'";
        $res = mysql_query($SQL) or die("E;getSearches:4"); 
        $row = mysql_fetch_array($res);
        $SQL = "SELECT searchid, description FROM searches WHERE status = 'confirmed' AND searchid = '".$row["searchid"]."'";
        $res = mysql_query($SQL) or die("E;getSearches:5"); 
        while ($row = mysql_fetch_array($res)) {
            echo $callStatus.";".$row["searchid"].";".$row["description"]."\n";
        } 
    }
}

function getSystemUsers($kraje) {
    checkSystemId();
    $SQL = "SELECT * FROM system_users";
    $res = mysql_query($SQL) or die("E;getSystemUsers:1"); 
    while ($row = mysql_fetch_array($res)) {
        $region = getRegion($kraje, $row["lon"], $row["lat"]);
        echo $row["sysid"].";".$row["user_name"].";".$row["status"].";".$row["searchid"].";".$region.";".$row["arrive"]."\n";
    }
}

function areItemsNumbers($array) {
    return ctype_digit(implode('',$array));
}

function changeStatus() {
    checkSystemId();      
    $items = explode(";", $_REQUEST["ids"]);
    if (areItemsNumbers($items)) {
      // TODO do it based on ids
      if (isset($_REQUEST["status_from"])) {
          $SQL = "UPDATE system_users SET status = '".$_REQUEST["status_to"]."' WHERE status = '".$_REQUEST["status_from"]."'";
      } else {
          $SQL = "UPDATE system_users SET status = '".$_REQUEST["status_to"]."'";
      }
      $SQL .= ", searchid = '".$_REQUEST["searchid"]."'";
      if ($_REQUEST["status_to"] == "released") {
        $SQL .= ", arrive = ''";
      }
      $SQL .= " WHERE sysid IN (".implode(',',$items).")";
      echo "I;".$SQL;  
      mysql_query($SQL) or die("E;changeStatus:1"); 
    } else {
      $SQL = "UPDATE system_users SET status = '".$_REQUEST["status_to"]."'";
      $SQL .= ", searchid = '".$_REQUEST["searchid"]."'";
      if ($_REQUEST["status_to"] == "released") {
        $SQL .= ", arrive = ''";
      }
      $SQL .= " WHERE id = '".$_REQUEST["id"]."'";
      echo "I;".$SQL;  
      mysql_query($SQL) or die("E;changeStatus:2"); 
    }
}

function getAllUsers() {
    //checkSystemId(); 
    $SQL = "SELECT id, user_name, lat, lon, dt_updated FROM users";
	  $res = mysql_query($SQL) or die("E;getAllUsers:1");
    while ($row = mysql_fetch_array($res)) { 
      $timestamp = time()+date("Z");
	    $utc_timestamp = gmdate("Y/m/d H:i:s",$timestamp);
	    $diff =  strtotime($utc_timestamp) - strtotime($row["dt_updated"]);
	    if ($diff > 300) {
	      echo $row["id"].";".$row["dt_updated"].";D;".$row["user_name"].";".$row["lon"]." ".$row["lat"].";".$diff."\n"; 
	    } else {
	      echo $row["id"].";".$row["dt_updated"].";A;".$row["user_name"].";".$row["lon"]." ".$row["lat"].";".$diff."\n";
	    }
	  }   
}

function createNewSearch() {
  checkSystemId(); 
	if (!ctype_alnum($_REQUEST["searchid"])) {
   die("E;createNewSearch:1");
  }
  $SQL = "INSERT INTO searches (searchid, description, status, region) 
        VALUES ('".$_REQUEST["searchid"]."', '".$_REQUEST["description"]."', 'confirmed', '".$_REQUEST["region"]."')";
	mysql_query($SQL) or die("E;createNewSearch:2");
  mkdir("/var/local/patrac/coordinator".$_REQUEST["searchid"]."/", 0777);
  $SQL = "INSERT INTO users (id, user_name) 
        VALUES ('coordinator".$_REQUEST["searchid"]."', 'Štáb')";
	mysql_query($SQL) or die("E;createNewSearch:3");
}

function closeSearch() {
  checkSystemId(); 
	if (!ctype_alnum($_REQUEST["searchid"])) {
    die("E;closeSearch:1");
  }
  $SQL = "UPDATE searches SET status = 'closed' WHERE searchid = '".$_REQUEST["searchid"]."'";
	mysql_query($SQL) or die("E;closeSearch:2");
  $SQL = "UPDATE system_users SET status = 'released', arrive = '', searchid = '' WHERE searchid = '".$_REQUEST["searchid"]."'";
	mysql_query($SQL) or die(mysql_error("E;closeSearch:3"));
}

function getId() {
  $id = "";
  if (isset($_REQUEST["id"]) && preg_match ('/[a-zA-Z0-9]/', $_REQUEST["id"]) && $_REQUEST["id"] != "null") {
    $id = $_REQUEST["id"];
  }
  $username = $_REQUEST["user_name"];
  if (isset($_REQUEST["systemid"]) && preg_match ('/[a-zA-Z0-9]/', $_REQUEST["systemid"])) {
    $SQL = "UPDATE system_users SET searchid = '".$_REQUEST["searchid"]."', status = 'onduty' WHERE id = '".$_REQUEST["systemid"]."'";
    mysql_query($SQL) or die("E;getId:1");
    /*
    $SQL = "SELECT user_name FROM system_users WHERE id = '".$_REQUEST["systemid"]."'";
    $res = mysql_query($SQL) or die("E;getId:2");
    $row = mysql_fetch_array($res);
    if ($row["user_name"] != "") {
      $username = $row["user_name"];
    }    
    $SQL = "SELECT id FROM users WHERE system_user = '".$_REQUEST["systemid"]."' AND searchid = '".$_REQUEST["searchid"]."'";
    $res = mysql_query($SQL) or die("E;getId:3");
    $row = mysql_fetch_array($res);
    $id = $row["id"];
    */             
  }

  if ($id == "") {
    $id = uniqid();
    mkdir("/var/local/patrac/".$id."/", 0777);
    $SQL = "INSERT INTO users (id, user_name, searchid) VALUES ('".$id."', '".$username."', '".$_REQUEST["searchid"]."')";    
    if (isset($_REQUEST["systemid"]) && preg_match ('/[a-zA-Z0-9]/', $_REQUEST["systemid"])) {
      $SQL = "INSERT INTO users (id, user_name, searchid, system_user) VALUES ('".$id."', '".$username."', '".$_REQUEST["searchid"]."', '".$_REQUEST["systemid"]."')";
    }
    mysql_query($SQL) or die("E;getId:4");
  }  
  
  echo "ID:".$id;
  
  if (checkLatLon($_REQUEST["lat"], $_REQUEST["lon"])) {
     $SQL = "INSERT INTO locations (id, lat, lon, searchid, dt_created) VALUES ('".$id."', ".$_REQUEST["lat"].", ".$_REQUEST["lon"].", '".$_REQUEST["searchid"]."', utc_timestamp())";
     mysql_query($SQL) or die("E;getId:5");
     $SQL = "UPDATE users SET lat = ".$_REQUEST["lat"].", lon = ".$_REQUEST["lon"].", searchid = '".$_REQUEST["searchid"]."', dt_updated = utc_timestamp() WHERE id = '".$id."'";
     mysql_query($SQL) or die("E;getId:6");
  }    
}

function sendLocation() {
  if (checkLatLon($_REQUEST["lat"], $_REQUEST["lon"])) {
    $SQL = "INSERT INTO locations (id, lat, lon, searchid, dt_created) VALUES ('".$_REQUEST["id"]."', ".$_REQUEST["lat"].", ".$_REQUEST["lon"].", '".$_REQUEST["searchid"]."', utc_timestamp())";
	  mysql_query($SQL) or die("E;sendLocation:1"); 
	  echo "P;S:1";
    $SQL = "UPDATE users SET lat = ".$_REQUEST["lat"].", lon = ".$_REQUEST["lon"].", searchid = '".$_REQUEST["searchid"]."', dt_updated = utc_timestamp() WHERE id = '".$_REQUEST["id"]."'";
    mysql_query($SQL) or die("E;sendLocation:2");
    $SQL = "UPDATE system_users su, users u SET su.status = 'onduty' WHERE u.system_user = su.id AND u.id = '".$_REQUEST["id"]."'";
    mysql_query($SQL) or die("E;sendLocations:3");
	} else {
	  echo "E;Incorrect input:".$_REQUEST["lat"]." ".$_REQUEST["lon"]."\n"; 
	}
}

function sendLocations() {
  $coordsString = $_REQUEST["coords"];
	$coords = explode(",", $coordsString);
	$count = 0;
	$errorCount = 0;
	foreach($coords as $coord) {
	  $coordString = trim($coord);
	  $coord = explode(";", $coordString);
	  if (checkLatLon($coord[0], $coord[1])) {
	    $SQL = "INSERT INTO locations (id, lon, lat, searchid, dt_created) VALUES ('".$_REQUEST["id"]."', ".$coord[0].", ".$coord[1].", '".$_REQUEST["searchid"]."', utc_timestamp())";
	    mysql_query($SQL) or die("E;sendLocations:1");
      $SQL = "UPDATE users SET lat = ".$coord[1].", lon = ".$coord[0].", searchid = '".$_REQUEST["searchid"]."',  dt_updated = utc_timestamp() WHERE id = '".$_REQUEST["id"]."'";
      mysql_query($SQL) or die("E;sendLocations:2");
      $SQL = "UPDATE system_users su, users u SET su.status = 'onduty' WHERE u.system_user = su.id AND u.id = '".$_REQUEST["id"]."'";
      mysql_query($SQL) or die("E;sendLocations:3");
	    $count++; 
	  } else {
	    $errorCount++;
	  }
	}
	echo "P;S:".$count."\n";
	echo "P;NS:".$errorCount."\n";
}

function getLocations() {
  $SQL = "SELECT id, user_name, lat, lon, dt_updated FROM users WHERE searchid = '".$_REQUEST["searchid"]."'";
	$res = mysql_query($SQL) or die("E;getLocations:1");
	while ($row = mysql_fetch_array($res)) { 
	  $timestamp = time()+date("Z");
	  $utc_timestamp = gmdate("Y/m/d H:i:s",$timestamp);
	  $diff =  strtotime($utc_timestamp) - strtotime($row["dt_updated"]);
	  if ($diff > 300) {
	    echo $row["id"].";".$row["dt_updated"].";D;".$row["user_name"].";".$row["lon"]." ".$row["lat"].";".$diff."\n"; 
	  } else {
	    echo $row["id"].";".$row["dt_updated"].";A;".$row["user_name"].";".$row["lon"]." ".$row["lat"].";".$diff."\n";
	  }
	}
}

function getTracks() {
  $SQL = "SELECT DISTINCT id FROM locations WHERE searchid = '".$_REQUEST["searchid"]."'";
	$res = mysql_query($SQL) or die("E;getTracks:1");
	while ($row = mysql_fetch_array($res)) { 
	  $SQL2 = "SELECT id, lat, lon, dt_created FROM locations WHERE id = '".$row["id"]."' AND searchid = '".$_REQUEST["searchid"]."' ORDER BY dt_created DESC LIMIT 1";
	  $res2 = mysql_query($SQL2) or die("E;getTracks:2");
	  $row2 = mysql_fetch_array($res2);
	  $SQL3 = "SELECT user_name FROM users WHERE id = '".$row["id"]."'";
	  $res3 = mysql_query($SQL3) or die("E;getTracks:3"); 
	  $row3 = mysql_fetch_array($res3);
	  $diff = strtotime(date('Y-m-d H:i:s')) - strtotime($row2["dt_created"]);
	  $SQL4 = "SELECT lat, lon FROM locations WHERE id = '".$row["id"]."' AND searchid = '".$_REQUEST["searchid"]."' ORDER BY dt_created, sysid";
	  $res4 = mysql_query($SQL4) or die("E;getTracks:4");
	  $points="";
	  $position = 0;           
	  while ($row4 = mysql_fetch_array($res4)) {
	    if ($position > 0) {
	      $points=$points.";".$row4["lon"]." ".$row4["lat"]; 
	    } else {
	      $points=$row4["lon"]." ".$row4["lat"];
	    }
	    $position++;
	  }  
	  if ($diff > 300) {
	    echo $row["id"].";".$row2["dt_created"].";D;".$row3["user_name"].";".$points."\n"; 
	  } else {
	    echo $row["id"].";".$row2["dt_created"].";A;".$row3["user_name"].";".$points."\n";
	  }
	}
}

function printGpxHeader($filename) {
  header($_SERVER["SERVER_PROTOCOL"] . " 200 OK");
  header("Cache-Control: public"); // needed for internet explorer
  header("Content-Type: application/gpx+xml");
  header("Content-Transfer-Encoding: Binary");
  header("Content-Disposition: attachment; filename=".$filename);
  $content = "<?xml version=\"1.0\"?>\n";
  $content .= "<gpx version=\"1.1\" creator=\"Patrac Server\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ogr=\"http://osgeo.org/gdal\" xmlns=\"http://www.topografix.com/GPX/1/1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n";
  return $content;
}

function getGpxLast() {
	$SQL = "SELECT id, user_name, lat, lon, dt_updated FROM users WHERE searchid = '".$_REQUEST["searchid"]."'";
	if (isset($_REQUEST["id"])) $SQL .= " AND id = '".$_REQUEST["id"]."'";
	$res = mysql_query($SQL) or die("E;getGpxLast:1");
  
$content = printGpxHeader("server_last.gpx");
	while ($row = mysql_fetch_array($res)) { 
	  $timeutc = str_replace(" ","T", $row["dt_updated"])."Z";
	  $content .= "<wpt lat=\"".$row["lat"]."\" lon=\"".$row["lon"]."\"><name>".$row["user_name"]."</name><desc>SessionId: ".$row["id"]."</desc><time>".$timeutc."</time></wpt>\n";
	}
	$content .= "</gpx>";
	header("Content-Length:".strlen($content));
	echo $content;        
	die();
}

function getGpx() {

	$SQL = "SELECT DISTINCT id FROM locations WHERE searchid = '".$_REQUEST["searchid"]."'";
	if (isset($_REQUEST["id"])) $SQL .= " AND id = '".$_REQUEST["id"]."'";
	$res = mysql_query($SQL) or die("E;getGpx:1");

  $content = printGpxHeader("server.gpx");
  while ($row = mysql_fetch_array($res)) { 
    $SQL2 = "SELECT lat, lon, dt_created FROM locations WHERE id = '".$row["id"]."' AND searchid = '".$_REQUEST["searchid"]."' ORDER BY dt_created, sysid";
    $res2 = mysql_query($SQL2) or die("E;getGpx:2");
    $SQL3 = "SELECT user_name FROM users WHERE id = '".$row["id"]."'";
    $res3 = mysql_query($SQL3) or die("E;getGpx:3"); 
    $row3 = mysql_fetch_array($res3);
    $content .= "<trk><name>".$row3["user_name"]."</name><desc>SessionId: ".$row["id"]."</desc>\n";
    $content .= "<trkseg>\n";
    while ($row2 = mysql_fetch_array($res2)) {
      $timeutc = str_replace(" ","T", $row2["dt_created"])."Z";
      $content .= "<trkpt lat=\"".$row2["lat"]."\" lon=\"".$row2["lon"]."\"><time>".$timeutc."</time></trkpt>\n";
    }
    $content .= "</trkseg></trk>";
  }
  $content .= "</gpx>";
  header("Content-Length:".strlen($content));
  echo $content;        
  die();
}

function getMessages() {
  //LIMIT 1 aby odešla v jednom požadavku vždy jen jedna zpráva
	$SQL = "SELECT * FROM messages WHERE id = '".$_REQUEST["id"]."' and readed <> 1 LIMIT 1";
	$res = mysql_query($SQL) or die("E;getMessages:1"); 
  $messageSent = false;	
  while ($row = mysql_fetch_array($res)) { 
    $SQL = "SELECT user_name FROM users WHERE id = '".$row["from_id"]."'";
    $res2 = mysql_query($SQL) or die("E;getMessages:2"); 
    $row2 = mysql_fetch_array($res2);
    $from = "NN";
    if ($row2["user_name"] != "") $from = $row2["user_name"];
	  echo "M;".$row["id"].";".$row["message"].";".$row["file"].";".$row["dt_created"].";".$row["shared"].";".$from.";".$row["sysid"];
    $messageSent = true;
	}
  if (!$messageSent) {
    echo "M;None";
  }
}

function markMessageAsReaded() {
	  $SQL = "UPDATE messages SET readed = 1 WHERE sysid = ".$_REQUEST["sysid"]." AND id = '".$_REQUEST["id"]."'";
	  mysql_query($SQL) or die("E;markMessageAsReaded:1;".$SQL); 
    echo "M;None";
}

function stripMessage($message) {
    return preg_replace('/[^A-ZĚŠČŘŽÝÁÍÉÚŮĎŤa-zěščřžýáíéúůďť0-9\-\.\ ]/', '_', $message);
}

function insertMessage($id, $message, $filename, $searchid, $shared, $from_id) {
    if (ctype_alnum($id) && ctype_alnum($from_id)) {
        $message = urldecode($message);
        if (strlen($message) > 255) $message = substr($message, 255);
        $SQL = "INSERT INTO messages (id, message, file, searchid, shared, from_id) VALUES ('".$id."', '".stripMessage($message)."', '".$filename."', '".$searchid."', ".$shared.", '".$from_id."')";
        mysql_query($SQL) or die("E;insertMessage:1");
        echo "I;".$SQL;
    } else {
        echo "E;ERROR (incorrect input): ID: ".$id." FROM_ID: ".$from_id;
    }
}

function insertSingleMessage() {
  $from_id = "NN" + uniqid();
  if (isset($_REQUEST["from_id"])) $from_id = $_REQUEST["from_id"];
  $filename = uploadFile($_REQUEST["id"]);
  //if ($filename == '') echo "I;NO FILE PROVIDED ";
  insertMessage($_REQUEST["id"], $_REQUEST["message"], $filename, $_REQUEST["searchid"], 0, $from_id);
}

function insertMessages() {
  $from_id = "NN" + uniqid();
	if (isset($_REQUEST["from_id"])) $from_id = $_REQUEST["from_id"];
	if (strpos($_REQUEST["ids"], ';') !== false) {
	  //echo "UPLOADING ";
	  $filename = uploadFile("shared");
	  //if ($filename == '') echo "NO FILE PROVIDED ";
	  $ids = explode(";", $_REQUEST["ids"]);
	  foreach($ids as $id) {
	    $id = trim($id);
	    insertMessage($id, $_REQUEST["message"], $filename, $_REQUEST["searchid"], 1, $from_id);
	  }
	} else {
	  $filename = uploadFile($_REQUEST["ids"]);
	  //if ($filename == '') echo "NO FILE PROVIDED ";
	  insertMessage($_REQUEST["ids"], $_REQUEST["message"], $filename, $_REQUEST["searchid"], 0, $from_id);
	}
}

function getFile() {
  $attachment_location = "/var/local/patrac/".$_REQUEST["id"]."/".$_REQUEST["filename"];
	if (file_exists($attachment_location)) {
	  header($_SERVER["SERVER_PROTOCOL"] . " 200 OK");
	  header("Cache-Control: public"); // needed for internet explorer
	  header("Content-Type: application/octet-stream");
	  header("Content-Transfer-Encoding: Binary");
	  header("Content-Length:".filesize($attachment_location));
	  header("Content-Disposition: attachment; filename=".$_REQUEST["filename"]);
	  readfile($attachment_location);
	  die();
	} else {
	  die("E;getFile:1:File not found.");
	}
}

function processOperationBasedOnSearchId() {
  if (!isset($_REQUEST["searchid"])) die("E;processOperationBasedOnSearchId:1");
	if ($_REQUEST["searchid"] == '') die("E;processOperationBasedOnSearchId:2");
	if (!ctype_alnum($_REQUEST["searchid"])) die("E;processOperationBasedOnSearchId:3");
  if (isset($_REQUEST["id"]) && !ctype_alnum($_REQUEST["id"])) die("E;processOperationBasedOnSearchId:4");
  if (isset($_REQUEST["from_id"]) && !ctype_alnum($_REQUEST["from_id"])) die("E;processOperationBasedOnSearchId:5");
	$SQL = "SELECT searchid FROM searches WHERE searchid = '".$_REQUEST["searchid"]."'";
	$res = mysql_query($SQL) or die("E;processOperationBasedOnSearchId:6");
	$row = mysql_fetch_array($res);
	if ($row["searchid"] != $_REQUEST["searchid"]) {
	   $SQL = "INSERT INTO searches (searchid) VALUES ('".$_REQUEST["searchid"]."')";
	   mysql_query($SQL) or die("E;processOperationBasedOnSearchId:7");
	   mkdir("/var/local/patrac/coordinator".$id."/", 0777);
	}
	
	switch ($_REQUEST["operation"]) {    
	    case "getid":
	        getId();    
          break;	    
	    case "sendlocation":
	        sendLocation();
	        break;	
	    case "sendlocations":
	        sendLocations();
	        break;	
	    case "getlocations":
	        getLocations();
	        break;
	    case "gettracks":
	        getTracks();
	        break;
	    case "getgpx_last":
	        getGpxLast();
	        break;	    
	    case "getgpx":
	        getGpx();
	        break;	
	    case "getmessages":
	        getMessages();
	        break;
      case "markmessage":
          markMessageAsReaded();
          break;	
	    case "insertmessage":
	        insertSingleMessage();
	        break;	
	    case "insertmessages":
	        insertMessages();
	        break;	
	    case "getfile":
	        getFile();
	        break;
	}
}

//file_put_contents("/tmp/mserver_debug.txt", file_get_contents("php://input"));
//echo "OK";

mysql_connect($_HOSTNAME, $_USERNAME, $_PASSWORD) or die(mysql_error());;
mysql_select_db($_DBNAME) or die("E;db");
mysql_query("set names utf8") or die("E;SetNames");

if (!isset($_REQUEST["operation"])) {
  die("E;operation:1");
}

switch ($_REQUEST["operation"]) {
    case "searches":
        getSearches();
        break;
    case "changestatus":
        changeStatus();
        break;
    case "getsystemusers":
        getSystemUsers($kraje);
        break;
    case "getallusers":
        getAllUsers();
        break;
    case "createnewsearch":
        createNewSearch();
        break;
    case "closesearch":
        closeSearch();
        break;
    default:
        processOperationBasedOnSearchId();
        break;
}

mysql_close();

//http://gisak.vsb.cz/patrac/mserver.php?operation=changestatus&status_to=sleeping&id=pcr1234
//http://gisak.vsb.cz/patrac/mserver.php?operation=changestatus&status_to=callonduty&id=pcr1234
//http://gisak.vsb.cz/patrac/mserver.php?operation=searches&id=pcr1234

/*
http://gisak.vsb.cz/patrac/mserver.php?operation=sendlocation&searchid=AAA111BBB&id=5a6715f7a244c&lat=10&lon=20

curl --form name=myfileparam --form file=@/home/jencek/test.gpx -Fjson='{'message': message, 'id': id, 'operation': 'insertmessage', 'searchid': 'AAA111BBB', 'fileToUpload', 'file'}' -Fsubmit=Build http://gisak.vsb.cz/patrac/mserver.php


curl --form name=myfileparam --form file=@/home/jencek/test.gpx -Fjson='{"parameter": {'message': message, 'id': id, 'operation': 'insertmessage', 'searchid': 'AAA111BBB', 'fileToUpload', 'file'}}' -Fsubmit=Build http://gisak.vsb.cz/patrac/mserver.php

data = json.dumps({'message': message, 'id': id, 'operation': 'insertmessage', 'searchid': 'AAA111BBB'})
        with open(filename1, 'rb') as f: r = requests.post('http://gisak.vsb.cz/patrac/mserver.php', data = {'message': message, 'id': id, 'operation': 'insertmessage'}, files={'fileToUpload': f})
        print r.text


-Fjson='{"parameter": {"name": "myfileparam", "file": "file"}}'

curl -X POST -F 'image=@/path/to/pictures/picture.jpg' http://domain.tld/upload
curl -X POST -d "searchid=AAA111BBB&operation=insertmessage&id=5a671dc761847&message=AAA" http://gisak.vsb.cz/patrac/mserver.php
curl -X POST -F 'searchid=AAA111BBB&operation=insertmessage&id=5a671dc761847&message=AAA&fileToUpload=@/home/jencek/test.gpx' http://gisak.vsb.cz/patrac/mserver.php

curl --form searchid=AAA111BBB --form operation=insertmessage --form id=5a671dc761847 --form message=AAA --form fileToUpload=@/home/jencek/test.gpx http://gisak.vsb.cz/patrac/mserver.php

*/

?>
