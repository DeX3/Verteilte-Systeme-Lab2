package remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class RemoteServer extends UnicastRemoteObject implements IRemoteServer {

	private static final long serialVersionUID = -5742366079346234674L;
	
	protected RemoteServer() throws RemoteException {
		super();
	}

	

}
