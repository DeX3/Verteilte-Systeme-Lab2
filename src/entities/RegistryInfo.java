package entities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import exceptions.ParseException;


/**
 * Helper class for communicating with the java RMI registry.
 */
public class RegistryInfo {
	
	/** Keys in the properties file */
	static final String KEY_HOST = "registry.host";
	static final String KEY_PORT = "registry.port";
	
	String host;
	int port;
	
	
	
	
	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * Sets the host.
	 * 
	 * @param host the new host
	 */
	public void setHost( String host ) {
		this.host = host;
	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets the port.
	 * 
	 * @param port the new port
	 */
	public void setPort( int port ) {
		this.port = port;
	}


	/**
	 * Connect to the registry. If create is true, this will create the registry.
	 * 
	 * @param create if true, create the registry instead of just connecting to it
	 * @return the registry
	 * @throws RemoteException the remote exception
	 */
	public Registry connect( boolean create ) throws RemoteException
	{
		if( create )
			return LocateRegistry.createRegistry( this.port );
		else
			return LocateRegistry.getRegistry( this.host, this.port );
	}
	

	/**
	 * Read registry info from the given properties file.
	 * 
	 * @param propertiesFile the properties file
	 * @return the registry info
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException the file not found exception
	 * @throws ParseException the parse exception
	 */
	public static RegistryInfo readRegistryInfo( String propertiesFile ) throws IOException, FileNotFoundException, ParseException
	{
		RegistryInfo ret = new RegistryInfo();
		
		InputStream in = ClassLoader.getSystemResourceAsStream( propertiesFile );
		
		if (in != null)
		{
			Properties users = new java.util.Properties();
			users.load(in);
			
			//Possible properties are registry.host and registry.port
			ret.host = users.getProperty( KEY_HOST );
			
			if( ret.host == null )
				throw new ParseException( "Incomplete information: no hostname given" );
			
			String szPort = users.getProperty( KEY_PORT );
			if( szPort == null )
				throw new ParseException( "Incomplete information: no port given" );
			
			try
			{
				ret.port = Integer.parseInt( szPort );
			}catch( NumberFormatException nfex )
			{ throw new ParseException( "The given port could not be parsed" ); }
			
		} else {
			throw new FileNotFoundException( "The file \"" + propertiesFile + "\" could not be found" );
		}
		
		return ret;
	}

}
