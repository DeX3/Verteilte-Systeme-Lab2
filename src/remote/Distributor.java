package remote;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.Logger;


public class Distributor implements Remote, Serializable {
	private static final long serialVersionUID = 1360533913014116850L;

	static final Logger logger = Logger.getLogger( Distributor.class.getName() );
	
	static IRemoteServer server = null;
	
	
	public Distributor()
	{ }
	
	public void hello( String str )
	{
		System.out.println( "Hello: " + str );
	}
	
	public IRemoteClient getRemoteClient() throws RemoteException
	{
		return new RemoteClient();
	}
	
	public synchronized IRemoteServer getRemoteServer() throws RemoteException
	{
		if( Distributor.server == null )
		{
			try {
				Distributor.server = new RemoteServer();
			} catch( RemoteException rex ) {
				logger.warning( "Could not create remote server: " + rex.getMessage() );
			}
		}
		
		return Distributor.server;
	}

}
