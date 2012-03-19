@author: Tarek Almiski
Usage instructions

SETUP INSTRUCTIONS:

The file subscription system requires that at least one accessible server (local or remote) be running in order to subscribe/unsubscribe to files and transfer files. Client and server need not run on the same machine, but they must be accessible to one another across the network.  For best results, disable all firewalls and network restrictions on machines with either client or server program running.  

###########
server.jar
###########

1. Open a command prompt in the directory where server.jar is located.  Note: the location of server.jar is where files for download/subscription shall be placed.  

2. begin server program by issuing command: 
	
	c:\>	java -jar server.jar SERVERNAME IPADDRESS PORT
	
	SERVERNAME is a uniquely chosen name for this server instance across all instances of the server program.  You must select a unique name for each 		server and you must remember the name of each server for future transactions by clients.

	IPADDRESS is the unique IP address for on which this server program will be hosted.  This is the IP that clients will use to interact with server,
	so make sure ot make note of it for future purposes.  Make sure that clients have access to this IP address.  

	PORT is the unique port on IPADDRESS where the server program will run.  It is the location where the RMI registry will be instantiated for this 
	instance of server program

	EXAMPLE: 

		java -jar server.jar server1 192.168.1.100 10000

		this will instantiate a server named 'server1' on IP address 192.168.1.100 and port 10000

	NOTES: 	server program can only be closed via a force close (ctrl-c)
		
		server will monitor active subscriptions that it is maintaining and will display on server terminal

###########
client.jar		
###########

3. open a command prompt in the directory where client.jar is located.  
	
   begin client program by issuing command:

	c:\>	java -jar client.jar

	You will then be prompted for a client name, for which you must supply a string for uniquely naming this client instance.  

	For example:

		c:\>	enter client name

		c:\>	client1

	This client will now be known as 'client1' for the duration of the server and client instances' lives until they are terminated.  

4. client menu

	after entering name for client, user is presented with the following menu:

		+++++++++++++++++++
		|1. Subscribe      |	//allows client to subscribe to files located on a remote file server and update subscriptions
		|2. Unsubscribe    |	//removes a previous subscription to a file on a remote file server
		|3. Upload         |	//uploads a file to a remote file server
		|4. Exit           |	//exits program
		+++++++++++++++++++

	IMPORTANT: selections are made by selecting the number corresponding to each option (i.e. for 'subscribe' select 1 then hit enter).

	1.) Subscribe - upon choosing the 'subscribe' option by selecting 1 then hitting enter, client program will require the following information: 

		server name, server IP, server port, file name for subscription, timestamp for subscription

		here is a sample run of the subscribe command:
		
			specify name of server to connect to
			server1
			specify IP Address of remote server
			192.168.1.100
			specify port of remote server
			10000
			connecting...
			specify file name
			screen.png
			specify hour for updates
				12
			specify minute for updates
			12
			specify second for updates
			12
			specify am or pm
			pm
			just received file: screen.png
			you did not have this file. it has been pushed...download success

	IMPORTANT: it is VERY IMPORTANT that you supply the correct name, IP, and port of the remote server.  errors in the input of these may lead 				to crashes in server program, client program, or both

		When specifying time, it is also important that you select valid times for the hour, minute, and second intervals 
		(1-12, 0-59, 0-59, respectively).  

	NOTE: 	If file is not available, user will get error message.

		If this is the first time user is subscribing to file, they will be pushed the file to the client directory.  

		If user already has file, this option will simply overwrite the previous subscription timestamp.  

		Time is adjusted for remote servers, so it is not a problem to enter desired time for subscription in terms of local time.  


	2.) Unsubscribe - upon choosing the 'unsubscribe' option by selecting 2 then hitting enter, client program will require following info: 

		server name, server IP, server port, file name

		here is a sample run of the unsubscribe command: 
		
			specify name of server to connect to
			server1
			specify IP Address of remote server
			192.168.1.100
			specify port of remote server
			10000
			connecting...
			specify file name
			screen.png
			unsubscription noted		

	NOTE: 	If user is not subscribed to desired file or file does not exist, then user will get error message.  

	3.) Upload - upon choosing the 'upload' option by selecting 3 and then hitting enter, client program will require following info:

		server name, server IP, server port, file name

		here is a sample run of the upload command: 		

			specify name of server to connect to
			server1
			specify IP Address of remote server
			192.168.1.100
			specify port of remote server
			10000
			connecting...
			specify file name to upload
			abc.txt
			upload success

		IMPORTANT: Files for upload must be located in the same directory where the client program is running.  

			It is important that file names that are on the client directory are entered exactly as they are.  Incorrect file names may 
			lead to crash in client program or server program or both.  

		NOTE: 	Upload can be used to introduce new files onto a remote server and can also be used for updating previously existing files.

			If client c1 is subscribed to file f1 on server s1 and client c2 uploads a new version of file f1 that exceeds client c1's 
			timestamp to server s1, then client c1 will automatically be pushed this new version of file f1.

	4.) Exit - upon choosing 'exit' option by selecting 4 and then hitting enter, client program will exit.    


		IMPORTANT: It is VERY IMPORTANT that the user unsubscribe from all subscriptions before exiting.  Failure to do so may result in a 
			crash of server or future clients or unpredictable behavior.  