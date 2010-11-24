package remote;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import server.Server;
import client.IClientCallback;
import entities.Event;
import entities.EventInfo;
import entities.User;


/**
 * The Class RemoteClient.
 */
public class RemoteClient implements IRemoteClient {

	
	protected Logger logger = Logger.getLogger( RemoteClient.class.getName() );
	
	Server srv;
	User user;
	
	/**
	 * Instantiates a new remote client.
	 * 
	 * @throws RemoteException the remote exception
	 */
	public RemoteClient() throws RemoteException
	{ super(); }
	
	
	/**
	 * Instantiates a new remote client.
	 * 
	 * @param srv the srv
	 * @throws RemoteException the remote exception
	 */
	public RemoteClient( Server srv ) throws RemoteException
	{
		this();
		this.srv = srv;
		this.user = null;
	}
	

	/* (non-Javadoc)
	 * @see remote.IRemoteClient#register(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean register(String name, String pwd) throws RemoteException {
		
		ensureAllServersOnline();
		
		logger.info( "\"" + name + "\" wants to register" );
		
		User u = new User( name, pwd, this.srv.getName() );
		
		//First, check own users
		//If this fails, no other servers have to be informed
		if( this.srv.getUsers().putIfAbsent( name, u ) != null )
			return false;
		
		//Continue, asking each server about the availability of the username
		boolean ok = true;
		for( String serverName : this.srv.getServerNames() )
		{
			ok &= this.srv.getServer( serverName ).beginRegister( name, pwd, this.srv.getName() );
		}
		
		//If ok, commit the changes, rollback
		for( String serverName : this.srv.getServerNames() )
		{
			if( !ok )
				this.srv.getServer( serverName ).rollbackRegister( name );
			else
				this.srv.getServer( serverName ).commitRegister( name );
		}
		
		//Finally, commit or rollback own changes
		if( !ok )
			this.srv.getUsers().remove( name );
		else
			u.commit();
		
		return ok;
	}
	
	/* (non-Javadoc)
	 * @see remote.IRemoteClient#login(java.lang.String, java.lang.String, client.IClientCallback)
	 */
	@Override
	public boolean login(String user, String pwd, IClientCallback callback )
			throws RemoteException {
		
		ensureAllServersOnline();
		
		User u = this.srv.getUsers().get( user );
		
		//Check if user exists
		if( u == null )
			return false;
		
		//Check if this is the right server
		if( !u.getServer().equals( this.srv.getName() ) )
			throw new RemoteException( "The user \"" + user + "\" can only login from server \"" + u.getServer() + "\"" );
		
		if( pwd.equals( u.getPassword() ) )
		{
			//log the user in
			if( u.login( callback ) )
			{
				this.user = u;
				return true;
			}else
				throw new RemoteException( "This user is already logged in." );
			
		}else
			return false;
		
	}
	
	/* (non-Javadoc)
	 * @see remote.IRemoteClient#create(java.lang.String, java.lang.String, int)
	 */
	@Override
	public boolean create( String name, String location, int duration ) throws RemoteException
	{
		// Same as register
		
		ensureAllServersOnline();
		ensureLogin();
		
		Event e = new Event( name, location, duration, this.user.getName() );
		
		if( this.srv.getEvents().putIfAbsent( name, e ) != null )
			return false;
		
		boolean ok = true;
		for( String serverName : this.srv.getServerNames() )
		{
			ok &= this.srv.getServer( serverName ).beginCreate( name, location, duration, this.user.getName() );
		}
		
		for( String serverName : this.srv.getServerNames() )
		{
			if( !ok )
				this.srv.getServer( serverName ).rollbackCreate( name );
			else
				this.srv.getServer( serverName ).commitCreate( name );
		}
		
		if( !ok )
			this.srv.getEvents().remove( name );
		else
			e.commit();
		
		return ok;
	}
	
	
	


	/* (non-Javadoc)
	 * @see remote.IRemoteClient#addDate(java.lang.String, java.util.Date)
	 */
	@Override
	public boolean addDate(String name, Date dt) throws RemoteException {
		
		Event evt = this.srv.getEvents().get( name );
		
		ensureAvailability( evt );
		
		if( !this.user.getName().equals( evt.getAuthor() ) )
			throw new RemoteException( "Only the author of an event can add dates to it." );
		
		return evt.addDate( dt );
	}
	
	/* (non-Javadoc)
	 * @see remote.IRemoteClient#invite(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean invite( String eventName, String userName ) throws RemoteException
	{
		ensureAllServersOnline();
		ensureLogin();
		
		Event evt = this.srv.getEvents().get( eventName );
		
		ensureAvailability( evt );
		
		if( userName.equals( this.user.getName() ) )
			throw new RemoteException( "You cannot invite yourself." );
		
		User u = this.srv.getUsers().get( userName );
		
		if( u == null )
			throw new RemoteException( "The specified user does not exist." );
		
		//Let the event know about the invitation
		evt.invite( userName );
		
		//Mein User? => direkt inviten
		if( this.srv.getName().equals( u.getServer() ) )
		{
			u.invite( eventName, this.user.getName() );
		}
		else	//Sonst an den verantwortlichen Server weiterleiten
			this.srv.getServer( u.getServer() ).invite( u.getName(), eventName, this.user.getName() );
		
		
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see remote.IRemoteClient#get(java.lang.String)
	 */
	@Override
	public EventInfo get( String name ) throws RemoteException {
		ensureAllServersOnline();
		ensureLogin();
		
		Event evt = this.srv.getEvents().get( name );
		
		if( evt == null )
			throw new RemoteException( "The given event does not exist." );
		
		String responsible = findServerForEvent( evt );
		
		if( this.srv.getName().equals( responsible ) )
		{			
			return new EventInfo( evt );
		}else
		{
			IRemoteServer srv = this.srv.getServer( responsible );
			
			return srv.get( name );
		}
		
		
	}


	/* (non-Javadoc)
	 * @see remote.IRemoteClient#vote(java.lang.String, java.util.Set)
	 */
	@Override
	public void vote( String name, Set<Date> dates ) throws RemoteException {
		ensureAllServersOnline();
		ensureLogin();
		
		Event evt = this.srv.getEvents().get( name );
		
		ensureAvailability( evt );
		
		String responsible = findServerForEvent( evt );
		
		//Check if this is the responsible server
		if( this.srv.getName().equals( responsible ) )
		{
			//yes? Vote directly
			if( !evt.vote( this.user.getName(), dates ) )
				throw new RemoteException( "You are not allowed to vote for this event." );
		}else
		{
			//Else, send the invitation to the responsible server
			this.srv.getServer( responsible ).vote( name, dates, this.user.getName() );
		}
		
	}

	/* (non-Javadoc)
	 * @see remote.IRemoteClient#finalizeEvent(java.lang.String)
	 */
	@Override
	public void finalizeEvent( String eventName ) throws RemoteException {
		ensureAllServersOnline();
		ensureLogin();
		
		Event evt = this.srv.getEvents().get( eventName );
		
		ensureAvailability( evt );
		
		//finalizing is only allowed for the author
		if( this.user.getName().equals( evt.getAuthor() ) )
		{
			if( !evt.finalizeDate() )
				throw new RemoteException( "The event does not have any date options." );
			
			//Notify each paticipant of the finalization
			for( String participant : evt.getInvited().keySet() )
			{
				User u = this.srv.getUsers().get(participant);
				if( u != null )
				{
					//Check if this is the responsible server
					if( this.srv.getName().equals(u.getServer()) )
						u.getCallback().onFinalization( eventName, evt.getFinalizedDate() );
					else
					{
						//If not, send the notification to the responsible server
						IRemoteServer server = this.srv.getServer( u.getServer() );
						
						server.onFinalization( eventName, evt.getFinalizedDate(), participant );
					}
				}
			}
			
		}else
			throw new RemoteException( "You can only finalize your own events." );
		
		
		
	}


	/* (non-Javadoc)
	 * @see remote.IRemoteClient#logout()
	 */
	@Override
	public void logout() throws RemoteException {
		this.user.setCallback( null );
		this.user.logout();
		this.user = null;		
	}
	
	
	/**
	 * Throws a RemoteException if the server network is not completely available.
	 * 
	 * @throws RemoteException the remote exception
	 */
	public void ensureAllServersOnline() throws RemoteException
	{
		assert srv != null;
		
		if( !srv.isNetworkComplete() )
			throw new RemoteException( "The server network is not completely available yet, please try again later." );
	}

	/**
	 * Throws a RemoteException if the user is not logged in.
	 * 
	 * @throws RemoteException the remote exception
	 */
	public void ensureLogin() throws RemoteException
	{
		if( this.user == null || !this.user.isOnline() )
			throw new RemoteException( "You must be logged in to use this command." );
	}
	
	/**
	 * Throws a RemoteException if the given event is not available (that is, it exists, and is not yet finalized)
	 * 
	 * @param evt the evt
	 * @throws RemoteException the remote exception
	 */
	protected void ensureAvailability( Event evt ) throws RemoteException
	{
		if( evt == null )
			throw new RemoteException( "The given event does not exist." );

		if( evt.isFinalized() )
			throw new RemoteException( "The given event is already finalized." );
	}
	
	
	/**
	 * Find the responsible server for the given event via it's author.
	 * 
	 * @param evt the evt
	 * @return the string
	 */
	protected String findServerForEvent( Event evt )
	{
		User u = this.srv.getUsers().get( evt.getAuthor() );
	
		if( u != null )
			return u.getServer();
		else
			return null;
	}

	
}
