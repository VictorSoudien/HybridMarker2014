<?php
	// Set up a secure connection to nightmare
	$conn_id = ftp_connect($ftp_server);

	$connection = ssh2_connect('nightmare.cs.uct.ac.za', 22);
	$sftp = ssh2_sftp($connection);

	if (ssh2_auth_password($connection, 'zmathews', '800hazhtM')) 
	{
		//echo "Authentication successful";
	}
	else 
	{
		die('Authentication Failed...');
	}

	$url = "nightmare.cs.uct.ac.za:3306";
	$con = mysql_connect($url,"zmathews","quohfeex","zmathews");
	
	// Check if an error occurred
	if (!$con)
	{
		echo "An error occurred while trying to connect to MySQL: " . mysql_error();
	}
	
	//specify table
	mysql_select_db('zmathews');
	
	// Get role of the person with these credentials
	$username = $_POST['username'];
	$password = $_POST['password'];
	
	// Get the operation that needs to be performed
	$operation = $_POST['operation'];
	
	if (strcmp($operation,"insert") == 0)
	{
		$query = "INSERT INTO staff (name,Course,Type,Password) VALUES ('Victor', 'CSC1010H', 'admin', 'admin')";
		$result = mysql_query($query)or die(mysql_error());
		echo "Insert complete";
	}
	else if (strcmp($operation, "select") == 0)
	{
		// Get the result from the database
		$query = "SELECT * FROM staff";
		$result = mysql_query($query)or die(mysql_error());
		
		while($row = mysql_fetch_array($result))
		{
			echo $row['name'] . " " . $row['Password'];
			echo "<br>";
		}
	}
	
	/*$row = mysql_fetch_array($result);
	$data = $row[0];
	
	if ($data)
	{
		echo $data;
	}*/
	
	//echo $result;
?>