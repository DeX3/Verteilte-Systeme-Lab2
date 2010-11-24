package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Set;

import entities.EventInfo;


/**
 * Remote interface for servers.
 */
public interface IRemoteServer extends Remote {

	/**
	 * Begin to register the given user.
	 * 
	 * @param userName the user name
	 * @param pwd the pwd
	 * @param server the server
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	boolean beginRegister(String userName, String pwd, String server) throws RemoteException;
	
	/**
	 * Rollback registering the given user.
	 * 
	 * @param userName the user name
	 * @throws RemoteException the remote exception
	 */
	void rollbackRegister(String userName) throws RemoteException;
	
	/**
	 * Commit registering the given user.
	 * 
	 * @param userName the user name
	 * @throws RemoteException the remote exception
	 */
	void commitRegister(String userName) throws RemoteException;
	
	/**
	 * Begin creating the given event.
	 * 
	 * @param eventName the event name
	 * @param location the location
	 * @param duration the duration
	 * @param authorName the author name
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	boolean beginCreate( String eventName, String location, int duration, String authorName ) throws RemoteException;
	
	/**
	 * Rollback creating the given event.
	 * 
	 * @param eventName the event name
	 * @throws RemoteException the remote exception
	 */
	void rollbackCreate( String eventName ) throws RemoteException;
	
	/**
	 * Commit creating the given event.
	 * 
	 * @param eventName the event name
	 * @throws RemoteException the remote exception
	 */
	void commitCreate( String eventName ) throws RemoteException;
	
	/**
	 * Invite the given user to the given event.
	 * 
	 * @param user the user
	 * @param event the event
	 * @param author the author
	 * @throws RemoteException the remote exception
	 */
	void invite(String user, String event, String author) throws RemoteException;
	
	/**
	 * Vote for the given dates at the given events for the given user
	 * 
	 * @param eventName the event name
	 * @param dates the dates
	 * @param user the user
	 * @throws RemoteException the remote exception
	 */
	void vote(String eventName, Set<Date> dates, String user) throws RemoteException;
	
	/**
	 * Executed when an event finalization occurs.
	 * 
	 * @param eventName the event name
	 * @param finalizedDate the finalized date
	 * @param user the user
	 * @throws RemoteException the remote exception
	 */
	void onFinalization(String eventName, Date finalizedDate, String user ) throws RemoteException;
	
	/**
	 * Gets the EventInfo for the given event.
	 * 
	 * @param name the name
	 * @return the event info
	 * @throws RemoteException the remote exception
	 */
	EventInfo get(String name) throws RemoteException;
	

}
