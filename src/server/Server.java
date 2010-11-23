package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import remote.Distributor;
import remote.IDistributor;
import remote.IRemoteServer;
import cmd.BooleanParameter;
import cmd.CommandLineParser;
import cmd.StringParameter;
import entities.Event;
import entities.RegistryInfo;
import entities.User;
import exceptions.ParseException;
import exceptions.ValidationException;


public class Server {
	
	static final Logger logger = Logger.getLogger( Server.class.getName() );
	
	static final String PROPERTIES_FILE = "registry.properties";
	
	private String bindingName;
	private boolean initRegistry;
	private String[] serverNames;
	private RegistryInfo regInfo;
	private Registry reg;
	
	private ConcurrentHashMap<String, IRemoteServer> servers;
	private ConcurrentHashMap<String, User> users;
	private ConcurrentHashMap<String, Event> events;

	
	public ConcurrentHashMap<String, User> getUsers() {
		return users;
	}
	
	public ConcurrentHashMap<String, Event> getEvents() {
		return this.events;
	}


	private LookupTask lookupTask;
	private IDistributor stub;
	
	
	public Server( String bindingName, boolean initRegistry, RegistryInfo regInfo, String...serverNames )
	{
		this.bindingName = bindingName;
		this.initRegistry = initRegistry;
		this.serverNames = serverNames;
		this.regInfo = regInfo;
		
		this.servers = new ConcurrentHashMap<String, IRemoteServer>();
		this.users = new ConcurrentHashMap<String, User>();
		this.events = new ConcurrentHashMap<String, Event>();
		this.lookupTask = new LookupTask( this );
	}
	

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
	
		final StringParameter PRM_BINDINGNAME = new StringParameter( "bindingName", "the name this server shall use to bind its remote reference in the RMI registry." );;
		final BooleanParameter PRM_INITREGISTRY = new BooleanParameter( "initRegistry", "a boolean value, i.e. either true or false, indicating whether this server is responsible for creating the RMI registry or not." );
		final StringParameter PRM_SERVERNAMES = new StringParameter( "serverNames", "a list of names, separated by space characters, indicating the name of the other servers' remote references." );
		final CommandLineParser clp = new CommandLineParser( "java server.Server", "Server for the lab2 event scheduling system." );
		clp.addParameters( PRM_BINDINGNAME, PRM_INITREGISTRY, PRM_SERVERNAMES );
		
		try{	
			clp.parse( args );
			

			
		}catch( ParseException pex )
		{
			logger.severe( "Command parse error: " + pex.getMessage() );
			System.out.println( clp.getUsageString() );
			return;
		}
		catch( ValidationException vex )
		{
			logger.severe( "Parameter validation error: " + vex.getMessage() );
			System.out.println( clp.getUsageString() );
			return;
		}
		
		RegistryInfo regInfo = null;
		try{
			regInfo = RegistryInfo.readRegistryInfo( PROPERTIES_FILE );
		}catch( ParseException pex )
		{
			logger.severe( "Couldn't read properties file: " + pex.getMessage() );
			return;
		}catch( FileNotFoundException fnfex )
		{
			logger.severe( "The file \"" + PROPERTIES_FILE + "\" could not be found" );
			return;
		}catch( IOException ioex )
		{
			logger.severe( "Couldn't read properties file: " + ioex.getMessage() );
			return;
		}
		
		
		Server srv = new Server(	PRM_BINDINGNAME.getValue(),
									PRM_INITREGISTRY.getValue(),
									regInfo,
									PRM_SERVERNAMES.getValue().split( "\\s" ) );

		srv.start();
	}
	
	public boolean start()
	{
		reg = null;
		
		//Create or connect to the registry
		try{
			reg = this.regInfo.connect( this.initRegistry );
			Distributor dist = new Distributor( this );
			this.stub = (IDistributor)UnicastRemoteObject.exportObject( dist, 0 );
			reg.bind( this.bindingName, this.stub );
			
		}catch( RemoteException rex )
		{
			logger.severe( "Remote error: " + rex.getMessage() );
			return false;
		}catch( AlreadyBoundException abex )
		{
			logger.severe( "The binding name \"" + this.bindingName + "\" is already in use. Maybe this server is already started?" );
			return false;
		}
		
		logger.info( "\"" + this.bindingName + "\" started up" );
		
		this.lookupTask.start();
		
		
		return true;
	}
	
	
	public void stop() throws AccessException, NotBoundException, RemoteException
	{
		if( this.stub != null )
			UnicastRemoteObject.unexportObject( this.stub, true );
		
		UnicastRemoteObject.unexportObject( this.reg, true );
		this.reg.unbind( this.bindingName );
		
	}
	
	public boolean addServer( String name, IRemoteServer server )
	{
		return this.servers.put( name, server ) == null;
	}
	
	public IRemoteServer getServer( String name )
	{
		return this.servers.get( name );
	}

	public boolean isNetworkComplete()
	{
		return this.servers.size() == this.serverNames.length;
	}

	public Registry getRegistry() {
		return this.reg;
	}


	public String[] getServerNames() {
		return this.serverNames;
	}



}
