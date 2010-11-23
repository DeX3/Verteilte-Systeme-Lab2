package remote;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import server.Server;


public class Distributor implements IDistributor {
	private static final long serialVersionUID = 1360533913014116850L;

	static final Logger logger = Logger.getLogger( Distributor.class.getName() );
	
	static IRemoteServer server = null;
	
	Server srv;
	public Distributor( Server srv )
	{
		super();
		this.srv = srv;
	}

	
	/** (non-Javadoc)
	 * @see remote.IDistributor#getRemoteClient()
	 */
	public IRemoteClient getRemoteClient() throws RemoteException
	{
		return new RemoteClient( this.srv );
	}
	
	
	/** (non-Javadoc)
	 * @see remote.IDistributor#getRemoteServer()
	 */
	public synchronized IRemoteServer getRemoteServer() throws RemoteException
	{
		if( Distributor.server == null )
		{
			try {
				Distributor.server = new RemoteServer( this.srv );
			} catch( RemoteException rex ) {
				logger.warning( "Could not create remote server: " + rex.getMessage() );
			}
		}
		
		return Distributor.server;
	}
}
