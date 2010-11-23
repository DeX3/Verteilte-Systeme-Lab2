package remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import server.Server;
import client.IClientCallback;
import entities.Event;
import entities.User;


public class RemoteClient extends UnicastRemoteObject implements IRemoteClient {

	private static final long serialVersionUID = -8185660595617187810L;
	protected Logger logger = Logger.getLogger( RemoteClient.class.getName() );
	
	Server srv;
	User user;
	
	public RemoteClient() throws RemoteException
	{ super(); }
	
	
	public RemoteClient( Server srv ) throws RemoteException
	{
		this();
		this.srv = srv;
		this.user = null;
	}
	

	@Override
	public boolean register(String name, String pwd) throws RemoteException {
		
		ensureAllServersOnline();
		
		logger.info( "\"" + name + "\" wants to register" );
		
		User u = new User( name, pwd );
		
		//First, check own users
		if( this.srv.getUsers().putIfAbsent( name, u ) != null )
			return false;
		
		boolean ok = true;
		for( String serverName : this.srv.getServerNames() )
		{
			ok &= this.srv.getServer( serverName ).beginRegister( name, pwd );
		}
		
		for( String serverName : this.srv.getServerNames() )
		{
			if( !ok )
				this.srv.getServer( serverName ).rollbackRegister( name );
			else
				this.srv.getServer( serverName ).commitRegister( name );
		}
		
		if( !ok )
			this.srv.getUsers().remove( name );
		else
			u.commit();
		
		return ok;
	}
	
	@Override
	public boolean login(String user, String pwd, IClientCallback callback )
			throws RemoteException {
		
		ensureAllServersOnline();
		
		User u = this.srv.getUsers().get( user );
		
		if( u == null )
			return false;
		
		if( pwd.equals( u.getPassword() ) )
		{
			u.login();
			this.user = u;
			
			return true;
		}else
			return false;
		
	}
	
	@Override
	public boolean create( String name, String location, int duration ) throws RemoteException
	{
		ensureAllServersOnline();
		ensureLogin();
		
		Event e = new Event( name, location, duration );
		
		if( this.srv.getEvents().putIfAbsent( name, e ) != null )
			return false;
		
		boolean ok = true;
		for( String serverName : this.srv.getServerNames() )
		{
			ok &= this.srv.getServer( serverName ).beginCreate( name, location, duration );
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
	
	
	public void ensureAllServersOnline() throws RemoteException
	{
		assert srv != null;
		
		if( !srv.isNetworkComplete() )
			throw new RemoteException( "The server network is not completely available yet, please try again later." );
	}

	public boolean ensureLogin() throws RemoteException
	{
		return this.user != null && this.user.isOnline();
	}
	
}
