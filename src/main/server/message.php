<?php

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

function getUserName($sessionid) {
    $query = "SELECT name FROM users WHERE sessionid = '" . $sessionid . "'";
    $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    $row = pg_fetch_array($result, null, PGSQL_ASSOC);
    return $row["name"];
}

function getMessages() {
    $lastReceivedMessageId = $_REQUEST["lastereceivedmessageid"];
    $sessionid = $_REQUEST["sessionid"];
    // TODO check summer time
    $query = "SELECT *, to_char((dt_created + '2 hour'::interval), 'HH24:MI') dt FROM messages WHERE to_id = '" . $sessionid . "' AND sysid > " . $lastReceivedMessageId;
    $result = pg_query($query) or die('Échec de la requête : ' . pg_last_error());
    $messages = '{"messages": [';
    $first = true;
    while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) {
        if (!$first) {
            $messages .= ', ';
        }
        $messages .= '{"messageid": ' . $row["sysid"] . ',';
        $messages .= '"fromid": "' . getUserName($row["from_id"]) . '",';
        $messages .= '"message": "' . $row["message"] . '",';
        $messages .= '"file": "' . $row["file"] . '",';
        $messages .= '"searchid": "' . $row["searchid"] . '",';
        $messages .= '"dt_created": "' . $row["dt"] . '",';
        $messages .= '"shared": ' . $row["shared"];
        $messages .= '}';
        $first = false;
    }
    $messages .= ']}';
    echo $messages;
}

function stripMessage($message) {
    return preg_replace('/[^A-ZĚŠČŘŽÝÁÍÉÚŮĎŤa-zěščřžýáíéúůďť0-9\-\.\ ]/', '_', $message);
}

function insertMessage($id, $message, $filename, $searchid, $shared, $from_id) {
    if (ctype_alnum($id) && ctype_alnum($from_id)) {
        $message = urldecode($message);
        if (strlen($message) > 255) $message = substr($message, 255);
        $SQL = "INSERT INTO messages (to_id, message, file, searchid, shared, from_id) VALUES ('".$id."', '".stripMessage($message)."', '".$filename."', '".$searchid."', ".$shared.", '".$from_id."')";
        pg_query($SQL) or die("E;insertMessage:1");
        echo "I;".$SQL;
    } else {
        echo "E;ERROR (incorrect input): ID: ".$id." FROM_ID: ".$from_id;
    }
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

if (!isset($_REQUEST["operation"])) {
    die("E;operation:1");
}

// Connexion, sélection de la base de données
$dbconn = pg_connect("host=localhost dbname= user= password=")
or die('{"ERROR":"connect"}');

switch ($_REQUEST["operation"]) {
    case "getmessages":
        getMessages();
        break;
    case "insertmessages":
        insertMessages();
        break;
    case "getfile":
        getFile();
        break;
}

pg_close($dbconn);

?>

