<?php

/*
create table users (sysid SERIAL, sessionid VARCHAR(50), id VARCHAR(50), name VARCHAR(50), searchid VARCHAR(20), status VARCHAR(20), lat FLOAT, lon FLOAT, arrive VARCHAR(10),   0 TEXT, dt_updated timestamp NOT NULL DEFAULT NOW());

alter table users add column status_requested varchar(20);
alter table users add column role varchar(10);

create table locations (sysid SERIAL PRIMARY KEY, sessionid VARCHAR(50), lat FLOAT, lon FLOAT, locid INT, searchid VARCHAR(20), dt_updated timestamp NOT NULL DEFAULT NOW());

alter table locations add column ts timestamp;

CREATE TABLE searches (sysid SERIAL PRIMARY KEY, searchid varchar(20), dt_created timestamp default now(), status varchar(20), name varchar(255), description varchar(255), region varchar(2));

insert into searches (searchid, name, description, region) values ('A', 'Kounov 15.9.2019', 'Dítě. 8 let. Akci řídí kap. Makeš tel. 775032091', 'kh');

CREATE TABLE messages (
  sysid SERIAL PRIMARY KEY,
  id INT,
  from_id varchar(50),
  to_id varchar(50),
  message varchar(255),
  file varchar(255),
  searchid varchar(20),
  dt_created timestamp DEFAULT NOW(),
  readed INT DEFAULT 0,
  shared INT DEFAULT 0);

*/

function getStatus($sessionid)
{
    $query = "SELECT id, status FROM users WHERE sessionid = '" . $sessionid . "'";
    $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    $row = pg_fetch_array($result, null, PGSQL_ASSOC);
    return $row["status"];
}

function getSearch($sessionid)
{
    $query = "SELECT searchid FROM users WHERE sessionid = '" . $sessionid . "'";
    $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    $row = pg_fetch_array($result, null, PGSQL_ASSOC);
    $searchid = $row["searchid"];
    $query = "SELECT searchid, name, description FROM searches WHERE searchid = '" . $searchid . "'";
    $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    $row = pg_fetch_array($result, null, PGSQL_ASSOC);
    return '"searchid":"' . $row["searchid"] . '","searchname":"' . $row["name"] . '","searchdesc":"' . $row["description"] . '",';
}

function saveLocations($sessionid, $locations, $searchid)
{
// Exécution de la requête SQL
    $query = "SELECT MAX(locid) id FROM locations WHERE sessionid = '" . $sessionid . "'";
    $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    $row = pg_fetch_array($result, null, PGSQL_ASSOC);

    $currentid = 0;
    if ($row["id"] != "") {
        $currentid = $row["id"];
    }

    foreach ($locations as $value) {
        if ($value->id > $currentid) {
            $query = "INSERT INTO locations (sessionid, searchid, locid, lon, lat, ts) VALUES ('" . $sessionid . "', '" . $searchid . "', " . $value->id . ", " . $value->lon . ", " . $value->lat . ", to_timestamp(" . $value->ts . " / 1000))";
            pg_query($query) or die('Échec de la requête : ' . pg_last_error());
        }
    }

    $query = "UPDATE users SET lon = ".$locations[sizeof($locations) - 1]->lon
        .", lat = ".$locations[sizeof($locations) - 1]->lat
        .", dt_updated = NOW()"
        ." WHERE sessionid = '".$sessionid."'";
    pg_query($query) or die('Échec de la requête : ' . pg_last_error());

}

function checkStatus($status, $sessionid, $arriveat, $searchid)
{
    $statusOnServer = getStatus($sessionid);

    if ($statusOnServer == "callonduty") {
        if ($status == "readytogo" || $status == "cannotarrive") {
            $query = "UPDATE users SET status = '" . $status . "', arrive = '" . $arriveat . "'  WHERE sessionid = '" . $sessionid . "'";
            pg_query($query) or die('Échec de la requête : ' . pg_last_error());
        }
    }

    if ($statusOnServer == "calltocome" || $statusOnServer == "callonduty") {
        if ($status == "onduty") {
            $query = "UPDATE users SET status = '" . $status . "' WHERE sessionid = '" . $sessionid . "'";
            pg_query($query) or die('Échec de la requête : ' . pg_last_error());
        }
    }

    if ($statusOnServer == "waiting") {
        if ($status == "onduty" && $searchid != "") {
            $query = "UPDATE users SET status = '" . $status . "', searchid = '" . $searchid . "' WHERE sessionid = '" . $sessionid . "'";
            pg_query($query) or die('Échec de la requête : ' . pg_last_error());
        }
    }
}

function getSessionId($userid) {
   $query = "SELECT sessionid FROM users WHERE id = '" . $userid . "'";
   $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
   $row = pg_fetch_array($result, null, PGSQL_ASSOC);
   return $row["sessionid"]; 
}

function getNewMessagesCount($lastMessageId, $sessionid) {
    $query = "SELECT COUNT(sysid) ct FROM messages WHERE to_id = '" . $sessionid . "' AND sysid > " . $lastMessageId;
    $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    $row = pg_fetch_array($result, null, PGSQL_ASSOC);
    return $row["ct"];
}

$input = json_decode($_REQUEST["content"]);
$username = $input->username;
$status = $input->status;
$arriveat = $input->arriveat;
$firebaseid = $input->firebaseid;
$userid = $input->userid;
$sessionid = $input->sessionid;
$searchid = $input->searchid;
$lastReceivedMessageId = $input->lastreceivedmessageid;

// Connexion, sélection de la base de données
$dbconn = pg_connect("host=localhost dbname= user= password=")
or die('{"ERROR":"connect"}');

$firebaseidchanged = "";
if ($firebaseid != "") {
    $sessionid = hash('ripemd160', $firebaseid);
    $firebaseidchanged = '"firebaseidchanged":1,"sessionid":"' . $sessionid . '",';
    mkdir("/var/local/patrac/".$sessionid."/", 0777);
    // pcr id or other id
    if ($userid != "") {
        $query = "SELECT id FROM users WHERE id = '" . $userid . "'";
        $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
        $row = pg_fetch_array($result, null, PGSQL_ASSOC);
        if ($row["id"] == $userid) {
            $query = "UPDATE users SET firebase = '" . $firebaseid . "', sessionid = '" . $sessionid . "', searchid = '" . $searchid . "' WHERE id = '" . $userid . "'";
            pg_query($query) or die('Échec de la requête : ' . pg_last_error());
        } else {
            //TODO wrong id - we will register it as registered user - not good for production, but nice for testing
            $query = "INSERT INTO users (firebase, sessionid, id, name, searchid, status, role) VALUES ('" . $firebaseid . "', '" . $sessionid . "', '" . $userid . "', '" . $username . "', '" . $searchid . "', 'waiting', 'registered')";
            pg_query($query) or die('Échec de la requête : ' . pg_last_error());
        }
    } else {
        $query = "INSERT INTO users (firebase, sessionid, id, name, searchid, status) VALUES ('" . $firebaseid . "', '" . $sessionid . "', '" . $sessionid . "', '" . $username . "', '" . $searchid . "', 'onduty')";
        pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    }
}

$sessionid_notknow = "";
if ($sessionid == '') {
  $current_sessionid = getSessionId($userid);
  $sessionid_notknow = '"sessionid":"' . $current_sessionid . '",';
}

$locations = $input->locations;
$lastlocationid = $locations[sizeof($locations) - 1]->id;

saveLocations($sessionid, $locations, $searchid);
checkStatus($status, $sessionid, $arriveat, $searchid);

$statusOnServer = getStatus($sessionid);
$search = getSearch($sessionid);

if ($lastReceivedMessageId == "") {
    $lastReceivedMessageId = 0;
}
$countOfNewMessages = getNewMessagesCount($lastReceivedMessageId, $sessionid);

echo '{
    "lastlocationid":' . $lastlocationid . ', 
    "countofnewmessages":' . $countOfNewMessages . ', '
    . $search . '' . $firebaseidchanged . '' . $sessionid_notknow .
    '"status":"' . $statusOnServer . '"
}';

pg_close($dbconn);

?>

