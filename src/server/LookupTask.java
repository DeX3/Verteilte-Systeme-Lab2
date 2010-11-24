package server;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import remote.IDistributor;

/**
 * Timertask for looking up servers.
 */
public class LookupTask extends TimerTask {
	
	public static final int LOOKUP_TIME = 500;
	protected static final Logger logger = Logger.getLogger( LookupTask.class.getName() );
	
	Timer timer;
	Server server;

	/**
	 * Instantiates a new lookup task.
	 * 
	 * @param server the server
	 */
	public LookupTask( Server server )
	{
		this.server = server;
	}
	
	/**
	 * Start looking up.
	 */
	public void start()
	{

		this.timer = new Timer();
		this.timer.schedule( this, 0, LOOKUP_TIME );
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		Registry reg = this.server.getRegistry();
		
		try{
			for( String serverName : this.server.getServerNames() )
			{
				IDistributor d = (IDistributor)reg.lookup( serverName );
				
				if( this.server.addServer( serverName, d.getRemoteServer() ) )
				{
					logger.info( "Successfully looked up \"" + serverName + "\"" );
				}
			}
		}catch( NotBoundException nbex )
		{
			//Interface is not yet bound
			return;
		}catch( AccessException aex )
		{
			logger.severe( "Cannot access remote interface." );
			System.exit( 1 );
		}catch( RemoteException rex )
		{
			logger.severe( rex.getMessage() );
			System.exit( 1 );
		}
	}
	
	/**
	 * Stop looking up.
	 */
	public void stop()
	{
		this.timer.cancel();
	}

}