import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

public interface Subscriber extends Remote{
	public String sub(String clientName, String filename, String timestamp) throws RemoteException;
	public String unsub(String clientName, String filename) throws RemoteException;
	public String upload(File f) throws RemoteException;
	public String fileSize(int i) throws RemoteException;
	
	public String upload2(String cname, String filename, int filesize, byte[] buf) throws RemoteException;
	public Map<String, String> test(String client) throws RemoteException;
	public long getServerTime() throws RemoteException;
	public void acceptClient(String name, RemoteClient rc) throws RemoteException;
}
