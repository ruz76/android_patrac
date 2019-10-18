<?php

function printPolyLine($row, $points, $ts) {
    if ($row["diff"] > 300) {
        echo $row["sessionid"].";".$ts.";D;".$row["name"].";".$points."\n";
    } else {
        echo $row["sessionid"].";".$ts.";A;".$row["name"].";".$points."\n";
    }
}

// Connexion, sélection de la base de données
$dbconn = pg_connect("host=localhost dbname= user= password=")
or die('{"ERROR":"connect"}');

$SQL = "SELECT sessionid, name, dt_updated, (NOW() - dt_updated) diff FROM users WHERE searchid = '".$_REQUEST["searchid"]."'";
$res = pg_query($SQL) or die("E;getTracks:1");
while ($row = pg_fetch_array($res, null, PGSQL_ASSOC)) {
    $SQL2 = "SELECT lat, lon, EXTRACT(EPOCH FROM ts) ts, ts ts2 FROM locations WHERE sessionid = '".$row["sessionid"]."' AND searchid = '".$_REQUEST["searchid"]."' ORDER BY locid";
    $res2 = pg_query($SQL2) or die("E;getTracks:2");
    $points="";
    $position = 0;
    $ts = 0;
    $lat = 0;
    $lon = 0;
    while ($row2 = pg_fetch_array($res2, null, PGSQL_ASSOC)) {
        if ($position > 0) {
            // if the time distance between points is more than 60 seconds we close the polyline
            if (($row2["ts"] - $ts) > 60) {
                printPolyLine($row, $points, $row2["ts2"]);
                $points=$row2["lon"]." ".$row2["lat"];
                $ts = $row2["ts"];
            } else {
                $points=$points.";".$row2["lon"]." ".$row2["lat"];
                $ts = $row2["ts"];
            }
        } else {
            $points=$row2["lon"]." ".$row2["lat"];
            $ts = $row2["ts"];
            $lat = $row2["lat"];
            $lon = $row2["lon"];
        }
        $position++;
    }
    printPolyLine($row, $points, $row2["ts2"]);
}

pg_close($dbconn);

?>

