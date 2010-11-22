package entities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import exceptions.ParseException;


public class RegistryInfo {
	String host;
	int port;
	
	static final String KEY_HOST = "registry.host";
	static final String KEY_PORT = "registry.port";
	
	
	public String getHost() {
		return host;
	}
	
	public void setHost( String host ) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}
	
	public void setPort( int port ) {
		this.port = port;
	}


	public Registry connect( boolean create ) throws RemoteException
	{
		if( create )
			return LocateRegistry.createRegistry( this.port );
		else
			return LocateRegistry.getRegistry( this.host, this.port );
	}

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
