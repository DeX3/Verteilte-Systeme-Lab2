package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Set;

import client.IClientCallback;
import entities.EventInfo;


/**
 * Remote interface for clients
 */
public interface IRemoteClient extends Remote {
	
	/**
	 * Register the given client.
	 * 
	 * @param user the user
	 * @param pwd the pwd
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean register( String user, String pwd ) throws RemoteException;

	/**
	 * Login the given client.
	 * 
	 * @param user the user
	 * @param pwd the pwd
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean login( String user, String pwd, IClientCallback stub ) throws RemoteException;

	/**
	 * Creates the given event.
	 * 
	 * @param name the name
	 * @param location the location
	 * @param duration the duration
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	boolean create( String name, String location, int duration )
			throws RemoteException;
	
	/**
	 * Adds the date to the given event.
	 * 
	 * @param name the name
	 * @param dt the dt
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	boolean addDate( String name, Date dt ) throws RemoteException;

	/**
	 * Invites the given user to the specified event.
	 * 
	 * @param eventName the event name
	 * @param userName the user name
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	boolean invite( String eventName, String userName ) throws RemoteException;

	/**
	 * Gets the EventInfo about the given event.
	 * 
	 * @param name the name
	 * @return the event info
	 * @throws RemoteException the remote exception
	 */
	public EventInfo get( String name ) throws RemoteException;

	/**
	 * Vote for the given dates on the specified event.
	 * 
	 * @param name the name
	 * @param dates the dates
	 * @throws RemoteException the remote exception
	 */
	public void vote(String name, Set<Date> dates) throws RemoteException;

	/**
	 * Finalize the given event.
	 * 
	 * @param name the name
	 * @throws RemoteException the remote exception
	 */
	public void finalizeEvent(String name) throws RemoteException;

	/**
	 * Logout the client.
	 * 
	 * @throws RemoteException the remote exception
	 */
	public void logout() throws RemoteException;
}
