package remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import server.Server;


/**
 * Represents the distributor interface fot clients and servers.
 */
public class Distributor implements IDistributor {

	static final Logger logger = Logger.getLogger( Distributor.class.getName() );
	
	
	static IRemoteServer server = null;
	
	
	List<IRemoteClient> clients;
	
	Server srv;
	
	/**
	 * Instantiates a new distributor.
	 * 
	 * @param srv the srv
	 */
	public Distributor( Server srv )
	{
		super();
		this.srv = srv;
		this.clients = new ArrayList<IRemoteClient>();
	}

	/**
	 * Unexport clients.
	 * 
	 * @throws RemoteException the remote exception
	 */
	public void unexportClients() throws RemoteException
	{
		for( IRemoteClient client : this.clients )
			UnicastRemoteObject.unexportObject( client, true );
		
	}
	
	/**
	 * Unexport server.
	 * 
	 * @throws RemoteException the remote exception
	 */
	public void unexportServer() throws RemoteException
	{
		if( Distributor.server != null )
			UnicastRemoteObject.unexportObject( Distributor.server, true );
	}
	
	/**
	 *  (non-Javadoc).
	 * 
	 * @return the remote client
	 * @throws RemoteException the remote exception
	 * @see remote.IDistributor#getRemoteClient()
	 */
	public IRemoteClient getRemoteClient() throws RemoteException
	{
		RemoteClient client = new RemoteClient( this.srv );
		IRemoteClient stub = (IRemoteClient)UnicastRemoteObject.exportObject( client,0 );
		
		this.clients.add( client );
		
		return stub;
	}
	
	
	/**
	 * (non-Javadoc).
	 * 
	 * @return the remote server
	 * @throws RemoteException the remote exception
	 * @see remote.IDistributor#getRemoteServer()
	 */
	public synchronized IRemoteServer getRemoteServer() throws RemoteException
	{
		if( Distributor.server == null )
		{
			try {
				Distributor.server = new RemoteServer( this.srv );
				
				UnicastRemoteObject.exportObject( Distributor.server, 0 );
			} catch( RemoteException rex ) {
				logger.warning( "Could not create remote server: " + rex.getMessage() );
			}
		}
		
		return Distributor.server;
	}
}
