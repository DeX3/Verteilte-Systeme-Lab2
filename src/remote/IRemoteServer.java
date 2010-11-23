package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IRemoteServer extends Remote {

	boolean beginRegister(String user, String pwd) throws RemoteException;
	void rollbackRegister(String name) throws RemoteException;
	void commitRegister(String name) throws RemoteException;
	
	boolean beginCreate( String name, String location, int duration ) throws RemoteException;
	void rollbackCreate( String name ) throws RemoteException;
	void commitCreate( String name ) throws RemoteException;

}
