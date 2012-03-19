import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;


public interface RemoteClient extends Remote{
	public Map<String, String> getSubs() throws RemoteException;
	public String download(String filename, int filesize, byte[] buf) throws RemoteException;

}
