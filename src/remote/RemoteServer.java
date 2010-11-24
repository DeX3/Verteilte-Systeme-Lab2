package remote;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import server.Server;
import entities.Event;
import entities.EventInfo;
import entities.User;


/**
 * The Class RemoteServer.
 */
public class RemoteServer implements IRemoteServer {
	
	protected Logger logger = Logger.getLogger( RemoteServer.class.getName() );
	
	private Server srv;
	
	/**
	 * Instantiates a new remote server.
	 * 
	 * @param srv the srv
	 * @throws RemoteException the remote exception
	 */
	protected RemoteServer( Server srv ) throws RemoteException {
		super();
		
		this.srv = srv;
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteServer#beginRegister(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean beginRegister( String name, String pwd, String server )
			throws RemoteException {
		
		User u = new User( name, pwd, server );
		u.setServer( server );
		return this.srv.getUsers().putIfAbsent( name, u ) == null;
	}
	
	/* (non-Javadoc)
	 * @see remote.IRemoteServer#rollbackRegister(java.lang.String)
	 */
	@Override
	public void rollbackRegister( String name ) throws RemoteException
	{
		User u = this.srv.getUsers().get( name );
		
		if( !u.isCommitted() )
			this.srv.getUsers().remove( name );
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteServer#commitRegister(java.lang.String)
	 */
	@Override
	public void commitRegister( String name ) throws RemoteException
	{
		this.srv.getUsers().get( name ).commit();
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteServer#beginCreate(java.lang.String, java.lang.String, int, java.lang.String)
	 */
	@Override
	public boolean beginCreate(String name, String location, int duration, String authorName )
			throws RemoteException {

		Event e = new Event( name, location, duration, authorName );
		return this.srv.getEvents().putIfAbsent( name, e ) == null;
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteServer#rollbackCreate(java.lang.String)
	 */
	@Override
	public void rollbackCreate(String name) throws RemoteException {
		Event e = this.srv.getEvents().get( name );
		
		if( !e.isCommitted() )
			this.srv.getEvents().remove( e );
	}
	
	/* (non-Javadoc)
	 * @see remote.IRemoteServer#commitCreate(java.lang.String)
	 */
	@Override
	public void commitCreate(String name) throws RemoteException {
		this.srv.getEvents().get( name ).commit();
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteServer#invite(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void invite( String user, String event, String author ) throws RemoteException {
		User u = this.srv.getUsers().get( user );
		
		
		if( u != null )
		{
			u.invite( event, author );
		}else
			logger.warning( "The user you want to invite is not on this server." );
	}
	
	
	/* (non-Javadoc)
	 * @see remote.IRemoteServer#vote(java.lang.String, java.util.Set, java.lang.String)
	 */
	@Override
	public void vote( String name, Set<Date> dates, String user ) throws RemoteException
	{
		Event evt = this.srv.getEvents().get( name );
		
		if( evt != null )
		{
			if( !evt.vote( user, dates ) )
				throw new RemoteException( "You are not allowed to vote for this event." );
		}
		
		
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteServer#onFinalization(java.lang.String, java.util.Date, java.lang.String)
	 */
	@Override
	public void onFinalization(String eventName, Date finalizedDate, String userName )
			throws RemoteException {
		
		User u = this.srv.getUsers().get( userName );
		
		if( u != null )
			u.getCallback().onFinalization( eventName, finalizedDate );
		else
			logger.warning( "The given user does not exist on this server" );
		
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteServer#get(java.lang.String)
	 */
	@Override
	public EventInfo get(String name) throws RemoteException {
		
		Event evt = this.srv.getEvents().get( name );
		
		if( evt != null )
			return new EventInfo( evt );
		return null;
	}
	
	

}
