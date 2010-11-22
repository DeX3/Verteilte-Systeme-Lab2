package remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class RemoteClient extends UnicastRemoteObject implements IRemoteClient {

	private static final long serialVersionUID = -8185660595617187810L;

	public RemoteClient() throws RemoteException
	{ super(); }
}
