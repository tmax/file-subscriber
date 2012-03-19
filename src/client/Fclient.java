import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Fclient extends UnicastRemoteObject implements RemoteClient{
	
	static String menu = 
			"+++++++++++++++++++\n" +
			"|1. Subscribe      |\n" +
			"|2. Unsubscribe    |\n" +
			"|3. Upload         |\n" +
			"|4. Exit           |\n" +
			"+++++++++++++++++++";
	public String name;
	public Map<String, String> mySubscriptions;
	
	public Fclient() throws RemoteException{}
	
	public String[] connectArgs(BufferedReader in)throws IOException{
		System.out.println("specify name of server to connect to");
		String serverName = in.readLine();
		System.out.println("specify IP Address of remote server");
		String ip = in.readLine();
		System.out.println("specify port of remote server");
		String port = in.readLine();				
		System.out.println("connecting...");
		
		return new String[] {serverName, ip, port};
	}
	
	public static void main(String[] args) throws IOException{
		Fclient fclient = new Fclient();
		fclient.mySubscriptions = new HashMap<String, String>();
		fclient.mySubscriptions.put("hi", "there");
		fclient.mySubscriptions.put("you", "stink");
		fclient.mySubscriptions.put("and", "something");
		BufferedReader in = new BufferedReader(
				new InputStreamReader(System.in));
		System.out.println("enter client name");
		fclient.name = in.readLine();
		while(true){
			String choice, ip, filename;
			int port;
			Registry reg = null;
			Subscriber s = null;
//			System.out.println("size of map: "+fclient.mySubscriptions.size());
			System.out.println(menu);
			choice = in.readLine();
			
			//subscription case
			if(choice.equals("1")){
				//connect to remote server
				//-------------------------------------------------------------
				System.out.println("specify name of server to connect to");
				String server = in.readLine();
				System.out.println("specify IP Address of remote server");
				ip = in.readLine();
				System.out.println("specify port of remote server");
				port = new Integer(in.readLine()).intValue();				
				System.out.println("connecting...");
				//-------------------------------------------------------------
				//get a local stub of remote object (server)
				try{
//					reg = LocateRegistry.getRegistry(ip, port);
//					s = (Subscriber) reg.lookup(server);
//					reg.rebind(fclient.name, fclient);
					
					//rmi://hostname:port/LookupName 
					s = (Subscriber) Naming.lookup("rmi://"+ip+":"+port+"/"+server);
//					Naming.rebind("rmi://"+ip+":"+port+"/"+fclient.name, fclient);
					s.acceptClient(fclient.name, fclient);

//					System.out.println("about to try something...");
//					System.out.println("time on server: "+s.getServerTime());
//					System.exit(0);
					
				}catch(RemoteException e){
					System.out.println("RemoteException fail\n"+e);
				}catch (NotBoundException e) {
					System.out.println("NotBoundException fail");
				}
				//subscribe to a file
				System.out.println("specify file name");
				filename = in.readLine();
				//timestamp
				//need to get hour, minute, and am/pm
				//-------------------------------------------------------------
				System.out.println("specify hour for updates");
				int hour = new Integer(in.readLine()).intValue();
				System.out.println("specify minute for updates");
				int minute = new Integer(in.readLine()).intValue();
				System.out.println("specify second for updates");
				int second = new Integer(in.readLine());
				System.out.println("specify am or pm");
				int am_pm = (in.readLine().toLowerCase().equals("am"))? 0 : 1;
				Calendar c = Calendar.getInstance();
				hour = hour == 12 ? 0 : hour;//12 (noon/midnight) = 0 in calendar
				c.set(Calendar.HOUR, hour);
				c.set(Calendar.MINUTE, minute);
				c.set(Calendar.SECOND, second);
				c.set(Calendar.AM_PM, am_pm);
				long clientTS = c.getTimeInMillis();
//				System.out.println(clientTS);
//				System.out.println(c.getTimeInMillis());
				
				long start = new Date().getTime();//record initial time of getting request
//				System.out.println("start: "+start);
				long serverTime = s.getServerTime();//get time on server
//				System.out.println("serverTime: "+serverTime);
				long end = new Date().getTime();//record end of transaction
//				System.out.println("end: "+end);
				
				long diffTime = (serverTime - end) + (end - start);
//				System.out.println("diffTime: "+diffTime);
				
				clientTS += diffTime;
//				System.out.println("clientTS+=diffTime: "+clientTS);
				
//				System.out.println("adjusted timestamp: "+clientTS);
//				System.out.println("or: "+new Date(clientTS));
				
				//we have the time, now adjust it to server time
				
				//-------------------------------------------------------------
				String string = s.sub(fclient.name, filename, ""+clientTS);
				System.out.println(string);
			}
			//unsubscription case
			else if(choice.equals("2")){
				//connect to remote server
				//-------------------------------------------------------------
				System.out.println("specify name of server to connect to");
				String server = in.readLine();
				System.out.println("specify IP Address of remote server");
				ip = in.readLine();
				System.out.println("specify port of remote server");
				port = new Integer(in.readLine()).intValue();				
				System.out.println("connecting...");
				//-------------------------------------------------------------
				//get a local stub of server, and register client
				//-------------------------------------------------------------
				try{
					reg = LocateRegistry.getRegistry(ip, port);
					s = (Subscriber) reg.lookup(server);
//					reg.rebind(fclient.name, fclient);
					s.acceptClient(fclient.name, fclient);
				}catch(RemoteException e){
					System.out.println("RemoteException fail\n"+e);
				}catch (NotBoundException e) {
					System.out.println("NotBoundException fail");
				}
				//-------------------------------------------------------------
				//UNsubscribe to a file
				//-------------------------------------------------------------
				System.out.println("specify file name");
				filename = in.readLine();
				System.out.println(s.unsub(fclient.name, filename));
				//-------------------------------------------------------------
			}
			//upload case
			else if(choice.equals("3")){
				//connect to remote server
				//-------------------------------------------------------------
				System.out.println("specify name of server to connect to");
				String server = in.readLine();
				System.out.println("specify IP Address of remote server");
				ip = in.readLine();
				System.out.println("specify port of remote server");
				port = new Integer(in.readLine()).intValue();				
				System.out.println("connecting...");
				//-------------------------------------------------------------
				//get local stub of server, register client
				//-------------------------------------------------------------
				try{
					reg = LocateRegistry.getRegistry(ip, port);
					s = (Subscriber) reg.lookup(server);
				}catch(RemoteException e){
					System.out.println("RemoteException fail");
				}catch (NotBoundException e) {
					System.out.println("NotBoundException fail");
				}
				//-------------------------------------------------------------
				//upload, push updates to subscribers
				//-------------------------------------------------------------
				System.out.println("specify file name to upload");
				filename = in.readLine();
				File up = new File(filename);
//				System.out.println("local size of file: "+up.length());
				int len = (int)up.length();
				InputStream fis = new FileInputStream(up);
				byte[] buf = new byte[len];
				fis.read(buf, 0, len);
//				System.out.println("'remote' size of file: "+s.fileSize(len));
				if(up.exists()){
//					System.out.println(s.upload(new File(filename)));
					System.out.println(s.upload2(fclient.name, filename, len, buf));
				}
				else
					System.out.println("that file don't exist!");
			}
			else if(choice.equals("4")){
				System.out.println("byebye");
				System.exit(1);
			}
			
//			else if(choice.equals("7")){
//			System.out.println("specify name of server to connect to");
//				String server = in.readLine();
//				System.out.println("specify IP Address of remote server");
//				ip = in.readLine();
//				System.out.println("specify port of remote server");
//				port = new Integer(in.readLine()).intValue();				
//				System.out.println("connecting...");
//				try{
//					reg = LocateRegistry.getRegistry(ip, port);
//					s = (Subscriber) reg.lookup(server);
//					fclient.mySubscriptions = s.test(fclient.name);
//					System.out.println("just ran 'test'");
////					System.out.println(s.test(fclient.name));
//				}catch(RemoteException e){
//					System.out.println("RemoteException fail");
//				}catch (NotBoundException e) {
//					System.out.println("NotBoundException fail");
//				}
//			}
			else
				System.out.println("make a valid selection!");
		}
	}

	@Override
	public Map<String, String> getSubs() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("someone touched my map!");
		return mySubscriptions;
	}

	@Override
	public String download(String filename, int filesize, byte[] buf)
			throws RemoteException {
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
		if(copy.length() == filesize){
			System.out.println("just received file: "+filename);
			return "download success";
		}
		else
			return "didn't completely download :S";
	}


}
