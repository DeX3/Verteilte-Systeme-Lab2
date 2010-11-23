package remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import server.Server;
import entities.Event;
import entities.User;


public class RemoteServer extends UnicastRemoteObject implements IRemoteServer {

	private static final long serialVersionUID = -5742366079346234674L;
	protected Logger logger = Logger.getLogger( RemoteServer.class.getName() );
	private Server srv;
	
	protected RemoteServer( Server srv ) throws RemoteException {
		super();
		
		this.srv = srv;
	}

	@Override
	public boolean beginRegister( String name, String pwd )
			throws RemoteException {
		
		User u = new User( name, pwd );
		return this.srv.getUsers().putIfAbsent( name, u ) == null;
	}
	
	@Override
	public void rollbackRegister( String name ) throws RemoteException
	{
		User u = this.srv.getUsers().get( name );
		
		if( !u.isCommitted() )
			this.srv.getUsers().remove( name );
	}

	@Override
	public void commitRegister( String name ) throws RemoteException
	{
		this.srv.getUsers().get( name ).commit();
	}

	@Override
	public boolean beginCreate(String name, String location, int duration)
			throws RemoteException {

		Event e = new Event( name, location, duration );
		return this.srv.getEvents().putIfAbsent( name, e ) == null;
	}

	@Override
	public void rollbackCreate(String name) throws RemoteException {
		Event e = this.srv.getEvents().get( name );
		
		if( !e.isCommitted() )
			this.srv.getEvents().remove( e );
	}
	
	@Override
	public void commitCreate(String name) throws RemoteException {
		
		this.srv.getEvents().get( name ).commit();
	}

	
	

}
