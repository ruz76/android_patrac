<?php

function getSystemUsers() {
    $SQL = "SELECT * FROM users WHERE searchid LIKE 'tri%'";
    $res = pg_query($SQL) or die("E;getSystemUsers:1");
    while ($row = pg_fetch_array($res, null, PGSQL_ASSOC)) {
        // TODO change with PostGIS
        //select (extract(SECOND from max(ts) - min(ts)) + (extract(MINUTE from max(ts) - min(ts)) * 60) + (extract(HOUR from max(ts) - min(ts)) * 3600)) / 10, count(*) from locations where sessionid = '8b38d041f77ac1576cfbbd0ddb28fe396ef28358' and ts > '2019-10-05 07:00:00';
        echo "<h3>".$row["name"]."</h3>\n";
        //$SQL = "select ((extract(SECOND from max(ts) - min(ts)) + (extract(MINUTE from max(ts) - min(ts)) * 60) + (extract(HOUR from max(ts) - min(ts)) * 3600)) / 10) ct1, count(*) ct2 from locations where sessionid = '".$row["sessionid"]."' and ts > '2019-10-05 ".$_REQUEST["h"].":00:00'";
        $SQL = "select (round((extract(SECOND from now() - TIMESTAMP '2019-10-05 ".$_REQUEST["h"].":".$_REQUEST["m"].":00') + (extract(MINUTE from now() - TIMESTAMP '2019-10-05 ".$_REQUEST["h"].":".$_REQUEST["m"].":00') * 60) + (extract(HOUR from now() - TIMESTAMP '2019-10-05 ".$_REQUEST["h"].":".$_REQUEST["m"].":00') * 3600)) / 10)) ct1, count(*) ct2 from locations where sessionid = '".$row["sessionid"]."' and ts > '2019-10-05 ".$_REQUEST["h"].":".$_REQUEST["m"].":00'";
        $res2 = pg_query($SQL) or die("E;getSystemUsers:2");
        $row2 = pg_fetch_array($res2, null, PGSQL_ASSOC);
        echo "<p>".$row2["ct1"]." ".$row2["ct2"]."</p>\n";
    }
}


// Connexion, sélection de la base de données
$dbconn = pg_connect("host=localhost dbname= user= password=")
or die('{"ERROR":"connect"}');

getSystemUsers();

pg_close($dbconn);

?>

