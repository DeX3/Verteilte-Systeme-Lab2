package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import client.IClientCallback;


public interface IRemoteClient extends Remote {
	
	public boolean register( String user, String pwd ) throws RemoteException;

	public boolean login(String user, String pwd, IClientCallback stub) throws RemoteException;

	boolean create(String name, String location, int duration)
			throws RemoteException;
}
