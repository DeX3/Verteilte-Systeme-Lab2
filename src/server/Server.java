package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
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

/**
 * Server class for the 2nd ds lab.
 */
public class Server {
	
	static final Logger logger = Logger.getLogger( Server.class.getName() );
	static final String PROPERTIES_FILE = "registry.properties";
	
	
	private String bindingName;
	private boolean initRegistry;
	private String[] serverNames;
	private RegistryInfo regInfo;
	private Registry reg;
	
	/** servers. */
	private ConcurrentHashMap<String, IRemoteServer> servers;
	/** users. */
	private ConcurrentHashMap<String, User> users;
	/** events. */
	private ConcurrentHashMap<String, Event> events;
	
	/** The distributor interface. */
	Distributor dist;

	
	/**
	 * Gets the users.
	 * 
	 * @return the users
	 */
	public ConcurrentHashMap<String, User> getUsers() {
		return users;
	}
	
	/**
	 * Gets the events.
	 * 
	 * @return the events
	 */
	public ConcurrentHashMap<String, Event> getEvents() {
		return this.events;
	}


	/** The lookup task. */
	private LookupTask lookupTask;
	
	/** The stub. */
	private IDistributor stub;
	
	
	/**
	 * Instantiates a new server.
	 * 
	 * @param bindingName the binding name
	 * @param initRegistry the init registry
	 * @param regInfo the reg info
	 * @param serverNames the server names
	 */
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
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main( String[] args ) {
	
		final StringParameter PRM_BINDINGNAME = new StringParameter( "bindingName", "the name this server shall use to bind its remote reference in the RMI registry." );;
		final BooleanParameter PRM_INITREGISTRY = new BooleanParameter( "initRegistry", "a boolean value, i.e. either true or false, indicating whether this server is responsible for creating the RMI registry or not." );
		final StringParameter PRM_SERVERNAMES = new StringParameter( "serverNames", "a list of names, separated by space characters, indicating the name of the other servers' remote references." );
		final CommandLineParser clp = new CommandLineParser( "java server.Server", "Server for the lab2 event scheduling system." );
		clp.addParameters( PRM_BINDINGNAME, PRM_INITREGISTRY, PRM_SERVERNAMES );
		
		try{
			//Parse command line arguments
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

		if( !srv.start() )
			System.exit( 1 );
		
		try {
			new BufferedReader( new InputStreamReader( System.in ) ).readLine();
		} catch (IOException e) {
			logger.warning( "Couldn't read from stdin" );
		}
		
		try {
			srv.stop();
		} catch (Exception ex) {
			System.err.println( "Error: " + ex.getMessage() );
		}
		
		System.out.println( "Shutting down.." );
	}
	
	
	
	
	/**
	 * Start.
	 * 
	 * @return true, if successful
	 */
	public boolean start()
	{
		reg = null;
		
		//Create or connect to the registry
		try{
			reg = this.regInfo.connect( this.initRegistry );
			this.dist = new Distributor( this );
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
	
	
	/**
	 * Stop.
	 */
	public void stop()
	{
		this.lookupTask.stop();
		
		if( this.stub != null )
		{
			try {
				this.reg.unbind( this.bindingName );
			
				this.dist.unexportClients();
			
				this.dist.unexportServer();
			
			
				UnicastRemoteObject.unexportObject( this.dist, true );
			}catch( RemoteException rex )
			{
				logger.warning( rex.getMessage() );
			}catch( NotBoundException nbex )
			{
				logger.warning( nbex.getMessage() );
			}
		}
		
		try {
			UnicastRemoteObject.unexportObject( this.reg, true );
		} catch (NoSuchObjectException ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
	/**
	 * Adds the server.
	 * 
	 * @param name the name
	 * @param server the server
	 * @return true, if successful
	 */
	public boolean addServer( String name, IRemoteServer server )
	{
		return this.servers.put( name, server ) == null;
	}
	
	/**
	 * Gets the server.
	 * 
	 * @param name the name
	 * @return the server
	 */
	public IRemoteServer getServer( String name )
	{
		return this.servers.get( name );
	}

	/**
	 * Checks if is network complete.
	 * 
	 * @return true, if is network complete
	 */
	public boolean isNetworkComplete()
	{
		return this.servers.size() == this.serverNames.length;
	}

	/**
	 * Gets the registry.
	 * 
	 * @return the registry
	 */
	public Registry getRegistry() {
		return this.reg;
	}


	/**
	 * Gets the server names.
	 * 
	 * @return the server names
	 */
	public String[] getServerNames() {
		return this.serverNames;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return this.bindingName;
	}

}
