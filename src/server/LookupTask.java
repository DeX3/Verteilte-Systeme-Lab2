package server;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import remote.Distributor;


public class LookupTask extends TimerTask {
	
	public static final int LOOKUP_TIME = 500;
	protected static final Logger logger = Logger.getLogger( LookupTask.class.getName() );
	
	Timer timer;
	Server server;

	public LookupTask( Server server )
	{
		this.server = server;
		this.timer = new Timer();
	}
	
	public void start()
	{
		this.timer.schedule( this, 0, LOOKUP_TIME );
	}
	
	@Override
	public void run() {
		Registry reg = this.server.getRegistry();
		
		try{
			for( String serverName : this.server.getServerNames() )
			{
				Distributor d = (Distributor)reg.lookup( serverName );
				
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
	
	public void stop()
	{
		this.timer.cancel();
	}

}