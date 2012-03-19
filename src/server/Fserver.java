import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class Fserver extends UnicastRemoteObject implements Subscriber{
	
	//map remote client name
	public Map<String, RemoteClient> RCMAP = new HashMap<String, RemoteClient>();
	
	//list of all files available on server
	public List<File> availableFiles = new LinkedList<File>();
	
	//map <client : <file : timestamp> > 
	//each client has a list of files, where each file has a distinct timestamp for updates
	public Map<String, Map<String, String>> clientSubs = new HashMap<String, Map<String, String>>();
	
	public String serverName;
	public String serverIP;
	public int serverPort;
	public Fserver() throws RemoteException{}

	@Override
	public String sub(String clientName, String filename, String timestamp) throws RemoteException {
		File requestedFile = new File(filename);
		if(requestedFile.exists()){
			//see if client is a subscriber (i.e. has a subscriber list)
			Map<String, String> fileToStamp = clientSubs.get(clientName);
			if(fileToStamp == null){//no subscriptions! put em in!
				fileToStamp = new HashMap<String, String>();
				clientSubs.put(clientName, fileToStamp);
			}
			//see if they have this file in their list yet
			String message = null;
//			int serverTS = (int)requestedFile.lastModified();
			long serverTS = requestedFile.lastModified();
			if(fileToStamp.keySet().contains(filename)){
				for(String s : fileToStamp.keySet()){
					if(s.equals(filename)){//you have a subscription for this file
						//see if the server copy t.s. > your t.s.
//						int serverTS = (int)requestedFile.lastModified();
//						int reqTS = new Integer(timestamp).intValue();
						long reqTS = new Long(timestamp).longValue();
						System.out.println("client TS became: "+reqTS);
						if(serverTS > reqTS){//local copy newer than client copy
							message =  "file updated since client timestamp provided!" +
									"pushing update...";
							try {
								InputStream fis = new FileInputStream(requestedFile);
								int len = (int) requestedFile.length();
								byte[] buf = new byte[len];
								fis.read(buf, 0, len);
								Registry r = LocateRegistry.getRegistry(serverIP, serverPort);
//								RemoteClient rc = (RemoteClient)r.lookup(clientName);
								RemoteClient rc = RCMAP.get(clientName);
								message += rc.download(filename, len, buf);
//								fileToStamp.put(filename, ""+serverTS);
								fileToStamp.put(filename, ""+reqTS);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} 
//							catch (NotBoundException e) {
//								e.printStackTrace();
//							}
						}
						else{
							message =  "subscription recorded";
							fileToStamp.put(filename, ""+reqTS);
						}
					}
				}
			}
			if(message == null){
				System.out.println("that file last updated on: "+
						new Date(requestedFile.lastModified()));///do not cast to int!

				message = "you did not have this file. it has been pushed...";
				try {
					InputStream fis = new FileInputStream(requestedFile);
					int len = (int) requestedFile.length();
					byte[] buf = new byte[len];
					fis.read(buf, 0, len);
					Registry r = LocateRegistry.getRegistry(serverIP, serverPort);
//					RemoteClient rc = (RemoteClient)r.lookup(clientName);
					RemoteClient rc = RCMAP.get(clientName);
					
					message += rc.download(filename, len, buf);
//					fileToStamp.put(filename, ""+serverTS);
					long reqTS = new Long(timestamp).longValue();
					fileToStamp.put(filename, ""+reqTS);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
				
//				catch (NotBoundException e) {
//					e.printStackTrace();
//				}
			}
			//clientSubs:   <clientname : <filename : timestamp> > 
			System.out.println("active subscribers:");
			for(String s : clientSubs.keySet()){
				for(String s2 : clientSubs.get(s).keySet()){
					System.out.println("("+s+") "+s2+": "+ 
							new Date(new Long(clientSubs.get(s).get(s2)).longValue()) );
				}
			}
			System.out.println("end participants");
			return message;
		}
	else
		return "Sorry, but file does not exist on server.";
	}
	@Override
	public String unsub(String clientName, String filename) throws RemoteException {
		String message = "unsubscription noted";
		File f = new File(filename);
		if(f.exists() && clientSubs.get(clientName)!=null){
			// <client : <filename : timestamp> > 
			Map<String, String> m  = clientSubs.get(clientName);//get map <file : timestamp>
			for(String s : m.keySet()){
				if(s.equals(filename)){
					String removed = m.remove(s);
					System.out.println("removed "+s+" from "+clientName+"'s list");
					break;
				}
			}
		}
		else
			message = "Sorry but you don't have a subscription to that file or it doesn't exist!";
		
		return message;
	}
	@Override
	public String upload(File f) throws RemoteException {
			try{
				//File original = f;
				File copy = new File("clone_of_"+f.getName());
				copy.createNewFile();
				System.out.println("line 65");
				System.out.println("file size (on server): "+f.length());
				InputStream fis = new FileInputStream(f);
				System.out.println("just tried to make an fis(f)");
				System.out.println("f.length"+f.length());
				OutputStream fos = new FileOutputStream(copy, false);
				
			    byte[] buf = new byte[(int)f.length()];
			    int len;
			    while ((len = fis.read(buf)) > 0) {
			        fos.write(buf, 0, len);
			    }
			    fis.close();
			    fos.close();
			    System.out.println("just got a new file: "+copy.getName());
				
			} catch (IOException e) {
				System.out.println("IOE fail\n"+e);
			} catch (ArrayIndexOutOfBoundsException e){
				System.out.println("AIOOBE fail");
			}
			System.out.println("bout to return");
			return "uploaded successfully";
	}
	
	@Override
	public String fileSize(int i){
		System.out.println("size of file: "+i);
		return "size of file is: "+i+". got it";
	}

	@Override
	public String upload2(String cname, String filename, int filesize, byte[] buf) throws RemoteException {
		File copy = new File(filename);
		try {
			OutputStream fos = new FileOutputStream(copy, false);
			fos.write(buf, 0, filesize);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		int serverTS = (int)copy.lastModified();
		long serverTS = copy.lastModified();
		//map <client : <file : timestamp> > 
		if(copy.length() == filesize){
			for(String clientStr : clientSubs.keySet()){
				Map<String, String> fName2TS = clientSubs.get(clientStr);
				for(String fileStr : fName2TS.keySet()){//loop through file : timestamp pairs
//					int clientTS = new Integer(fName2TS.get(fileStr)).intValue();
					long clientTS = new Long(fName2TS.get(fileStr)).longValue();
					if(filename.equals(fileStr) && serverTS > clientTS
							&& !cname.equals(clientStr)){//if time, update
						try{
							InputStream fis = new FileInputStream(copy);
							int len = (int) copy.length();
							byte[] cpBuf = new byte[len];
							fis.read(cpBuf, 0, len);
							Registry r = LocateRegistry.getRegistry(serverIP, serverPort);
//							RemoteClient rc = (RemoteClient)r.lookup(clientStr);
							RemoteClient rc = RCMAP.get(clientStr);
							String message = rc.download(filename, len, cpBuf);
							System.out.println("pushed update to "+clientStr);
							fName2TS.put(filename, ""+serverTS);
						} catch (IOException e) {
							e.printStackTrace();
						} 
						
//						catch (NotBoundException e) {
//							e.printStackTrace();
//						}
					}
				}
			}System.out.println();

			return "upload success";
		}
		else
			return "didn't completely copy";
	}
	
	
	public static void main(String[] args) throws RemoteException, AlreadyBoundException, UnknownHostException{
//		if (System.getSecurityManager() == null) {
//		    System.setSecurityManager(new SecurityManager());
//		}
		System.setProperty("java.security.policy", "/no.policy");
		if(args.length != 3){
			System.out.println("usage: java Fserver name ip_address rmi_port");
			System.exit(0);
		}
		String name = args[0];//name of Fileserver
//		String myIP = InetAddress.getLocalHost().toString();
//		System.out.println("my ip: "+myIP);
		String myIP = args[1];
		int rPort = new Integer(args[2]).intValue();
		
		Fserver fserver = new Fserver();
		fserver.serverName = name;
		fserver.serverIP = myIP;
		fserver.serverPort = rPort;
		
		Registry r = LocateRegistry.createRegistry(rPort);
//		r.bind(fserver.serverName, fserver);
		
		try {
			Naming.rebind("rmi://"+fserver.serverIP+":"+fserver.serverPort+"/"+fserver.serverName
					, fserver);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		System.out.println("registry is up on: "+myIP+":"+rPort);
		System.out.println("waiting for subscribers...");
		
	}

	@Override
	public Map<String, String> test(String client) throws RemoteException {
		RemoteClient rc = null;
		Map<String, String> hm = null;
		try {
//			Registry r = LocateRegistry.getRegistry("192.168.1.100", 10000);
			Registry r = LocateRegistry.getRegistry(serverIP, serverPort);
			
//			rc = (RemoteClient)Naming.lookup(client);
//			rc = (RemoteClient) r.lookup(client);
			rc = RCMAP.get(client);
			
			
			System.out.println("size of map before my action: "+rc.getSubs().size());
			hm = rc.getSubs();
			hm.put("will it", "work");
			System.out.println("size of map after my action: "+rc.getSubs().size());
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		return hm;
	}

	@Override
	public long getServerTime() throws RemoteException {
//		System.out.println("someone asked me what time it was");
		return new java.util.Date().getTime();
	}

	@Override
	public void acceptClient(String name, RemoteClient rc) throws RemoteException {

		RCMAP.put(name, rc);
	}

}