<?php

// Connexion, sélection de la base de données
$dbconn = pg_connect("host=localhost dbname= user= password=")
or die('{"ERROR":"connect"}');

// we fix this for Czech republic and surround for now to be sure that bad coords are not in the list
$query = "SELECT MIN(lon) min_lon, MIN(lat) min_lat, MAX(lon) max_lon, MAX(lat) max_lat, AVG(lon) avg_lon, AVG(lat) avg_lat FROM users 
          WHERE searchid = '" . $_REQUEST["searchid"] . "' 
          AND lon > 10 AND lon < 20 AND lat > 45 AND lat < 55";
$result = pg_query($query) or die("E;getLocations:1");
$row = pg_fetch_array($result, null, PGSQL_ASSOC);
echo $row["min_lon"].";".$row["min_lat"].";".$row["max_lon"].";".$row["max_lat"].";".$row["avg_lon"].";".$row["avg_lat"]."\n";

// TODO check summer time
$query = "SELECT sessionid, name, lat, lon, (dt_updated + '2 hour'::interval) dt_updated , (NOW() - dt_updated) diff FROM users WHERE searchid = '" . $_REQUEST["searchid"] . "'";
$result = pg_query($query) or die("E;getLocations:2");
while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) {
    $diff = $row["diff"];
    if ($diff > 300) {
        echo $row["sessionid"].";".$row["dt_updated"].";D;".$row["name"].";".$row["lon"]." ".$row["lat"].";".$diff."\n";
    } else {
        echo $row["sessionid"].";".$row["dt_updated"].";A;".$row["name"].";".$row["lon"]." ".$row["lat"].";".$diff."\n";
    }
}

pg_close($dbconn);

?>

