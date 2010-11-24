package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import remote.IDistributor;
import remote.IRemoteClient;
import cmd.Command;
import cmd.CommandLineParser;
import cmd.CommandParser;
import cmd.DateParameter;
import cmd.IntegerParameter;
import cmd.StringParameter;
import entities.EventInfo;
import entities.RegistryInfo;
import exceptions.ParseException;
import exceptions.ValidationException;


/**
 * Client for the 2nd ds lab
 */
public class Client {

	static final Logger logger = Logger.getLogger( Client.class.getName() );
	static final String PROPERTIES_FILE = "registry.properties";
	
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main( String[] args ) {

		final StringParameter PRM_SERVERNAME = new StringParameter( "serverName", "the name of the remote reference in the RMI registry of the server that shall be responsible for this client." );
		
		CommandLineParser clp = new CommandLineParser( "java client.Client", "Client for the 2nd lab of distributed systems." );
		
		//Parse command line arguments
		clp.addParameter( PRM_SERVERNAME );
		
		try {
			clp.parse( args );
		} catch( ParseException e ) {
			System.err.println( "Cannot parse command line arguments: " + e.getMessage() );
			System.out.println( clp.getUsageString() );
			return;
		} catch( ValidationException e ) {
			System.err.println( "Cannot validate command line arguments: " + e.getMessage() );
			System.out.println( clp.getUsageString() );
			return;
		}
		
		//Connect to/Create the registry
		Registry reg = null;
		try {
			RegistryInfo regInfo = RegistryInfo.readRegistryInfo( PROPERTIES_FILE );
			reg = regInfo.connect( false );
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
		
		//Receive the server's distributor object
		//And get the clients remote interface
		IRemoteClient server = null;
		try
		{
			IDistributor dst = (IDistributor)reg.lookup( PRM_SERVERNAME.getValue() );
			server = dst.getRemoteClient();

			logger.info( "Successfully connected to " + PRM_SERVERNAME.getValue() );
		} catch( NotBoundException e ) {
			logger.severe( "The server \"" + PRM_SERVERNAME.getValue() + "\" does not appear to be online." );
			return;
		} catch( RemoteException rex )
		{
			logger.severe( "Couldn't receive remote interface for client from \"" + PRM_SERVERNAME.getValue() + "\"" );
			return;
		}

		//Export the clients callback interface
		ClientCallback callback = null;
		IClientCallback stub = null;
		try{
			callback = new ClientCallback();
			stub = (IClientCallback)UnicastRemoteObject.exportObject( callback, 0 );
		}catch( RemoteException rex )
		{
			logger.severe( "Couldn't export callback object for client" );
			return;
		}
		
		
		//Start reading commands from stdin
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		Command cmd = null;
		
		System.out.println( "You can now start entering commands." );
		
		while( cmd != CMD_EXIT )
		{
			
			try{
				String input = br.readLine();
				if( input == null )
					break;
				
				cmd = cmdParser.parse( input );
				
				if( cmd == null )
				{
					System.out.println( "Unknown command: \"" + input + "\"" );
					continue;
				}
				
				if( cmd == CMD_REGISTER )
					doRegister(server, cmd);
				else if( cmd == CMD_LOGIN )
					doLogin(server, stub, cmd);
				else if( cmd == CMD_CREATE )
					doCreate(server, cmd);
				else if( cmd == CMD_ADDDATE )
					doAddDate(server, cmd);
				else if( cmd == CMD_INVITE )
					doInvite(server, cmd);
				else if( cmd == CMD_GET )
					doGet(server, cmd);
				else if( cmd == CMD_VOTE )
					doVote(server, cmd);
				else if( cmd == CMD_FINALIZE )
					doFinalize( server, cmd );
				else if( cmd == CMD_LOGOUT )
					doLogout(server);
				
				
				
			}catch( RemoteException rex )
			{
				Throwable cause = rex.getCause();
				if( cause != null )
					System.out.println( "Error: " + cause.getMessage() );
				else
					System.out.println( "Error: " + rex.getMessage() );
			}
			catch( IOException ioex )
			{ logger.warning( "Couldn't read from stdin: " + ioex.getMessage() ); }
			catch( ParseException pex )
			{ System.out.println( pex.getMessage() ); }
			catch( ValidationException vex )
			{ System.out.println( vex.getMessage() ); }
		}
		
		
		System.out.println( "Shutting down..." );
		
		try {
			UnicastRemoteObject.unexportObject( callback, true );
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
			
		}
	}

	private static void doFinalize(IRemoteClient server, Command cmd) throws RemoteException{
		String name = (String)cmd.getParameter( "name of event" ).getValue();
		
		server.finalizeEvent( name );
		EventInfo ei = server.get( name );
		SimpleDateFormat sdf = new SimpleDateFormat( DateParameter.FORMAT_STRING );
		System.out.println( "Event finalized. Final date/time is: " + sdf.format( ei.getFinalizedDate() ) );
	}

	private static void doLogout(IRemoteClient server) throws RemoteException {
		server.logout();
		System.out.println( "Logged out." );
	}

	private static void doVote(IRemoteClient server, Command cmd)
			throws RemoteException {
		String name = (String)cmd.getParameter( "name of event" ).getValue();
		Set<Date> dates = new TreeSet<Date>();
		
		SimpleDateFormat sdf = new SimpleDateFormat( DateParameter.FORMAT_STRING );
		
		try {						
			for( String str : cmd.getRest().split( "\\s" ) )
			{
				dates.add( sdf.parse( str ) );
			}
			
			server.vote( name, dates );
		} catch (java.text.ParseException e) {
			System.out.println( "The date does not have the valid format. Please use " + DateParameter.FORMAT_STRING );
		}
	}

	private static void doGet(IRemoteClient server, Command cmd)
			throws RemoteException {
		String name = (String)cmd.getParameter( "name of event" ).getValue();
		
		System.out.println( server.get( name ) );
	}

	private static void doInvite(IRemoteClient server, Command cmd)
			throws RemoteException {
		String name = (String)cmd.getParameter( "name of event" ).getValue();
		String user = (String)cmd.getParameter( "username" ).getValue();
		
		server.invite( name, user );
		System.out.println( "Invitation sent." );
	}

	private static void doAddDate(IRemoteClient server, Command cmd)
			throws RemoteException {
		String name = (String)cmd.getParameter( "name of event" ).getValue();
		Date dt = (Date)cmd.getParameter( "date" ).getValue();

		if( server.addDate( name, dt ) )
			System.out.println( "Date option added." );
		else
			System.out.println( "This date option already exists." );
	}

	private static void doCreate(IRemoteClient server, Command cmd)
			throws RemoteException {
		String name = (String)cmd.getParameter( "name" ).getValue();
		String location = (String)cmd.getParameter( "location" ).getValue();
		int duration = (Integer)cmd.getParameter( "duration" ).getValue();
		
		if( server.create( name, location, duration ) )
			System.out.println( "Event created successfully." );
		else
			System.out.println( "Error: An event of with this name already exists." );
	}

	private static void doLogin(IRemoteClient server, IClientCallback stub,
			Command cmd) throws RemoteException {
		String user = (String)cmd.getParameter( "username" ).getValue();
		String pwd = (String)cmd.getParameter( "password" ).getValue();
		
		if( server.login( user, pwd, stub ) )
			System.out.println( "Successfully logged in" );
		else
			System.out.println( "Wrong username or password" );
	}

	private static void doRegister(IRemoteClient server, Command cmd)
			throws RemoteException {
		String user = (String)cmd.getParameter( "username" ).getValue();
		String pwd = (String)cmd.getParameter( "password" ).getValue();
		if( server.register( user, pwd ) )
			System.out.println( "Successfully registered." );
		else
			System.out.println( "Username already registered." );
	}
	
	/** Command parsing stuff... */
	static final Command CMD_REGISTER;
	static final Command CMD_LOGIN;
	static final Command CMD_CREATE;
	static final Command CMD_ADDDATE;
	static final Command CMD_INVITE;
	static final Command CMD_GET;
	static final Command CMD_VOTE;
	static final Command CMD_FINALIZE;
	static final Command CMD_LOGOUT;
	static final Command CMD_EXIT;
	static CommandParser cmdParser;
	
	static
	{
		CMD_REGISTER = new Command( "register" );
		CMD_REGISTER.addParameter( new StringParameter( "username", "The name to use for registering." ) );
		CMD_REGISTER.addParameter( new StringParameter( "password", "The password required for registration." ) );
		
		CMD_LOGIN = new Command( "login" );
		CMD_LOGIN.addParameter( new StringParameter( "username", "The name to use for logging in." ) );
		CMD_LOGIN.addParameter( new StringParameter( "password", "The password required for logging in." ) );
		
		CMD_CREATE = new Command( "create" );
		CMD_CREATE.addParameter( new StringParameter( "name", "The name of the event to create." ) );
		CMD_CREATE.addParameter( new StringParameter( "location", "The location of the event to create." ) );
		CMD_CREATE.addParameter( new IntegerParameter( "duration", 0, Integer.MAX_VALUE, "The duration (in minutes) of the event." ) );
		
		CMD_ADDDATE = new Command( "addDate" );
		CMD_ADDDATE.addParameter( new StringParameter( "name of event", "The name of the event to add a possible date to" ) );
		CMD_ADDDATE.addParameter( new DateParameter( "date", "The date to add to the given event." ) );
		
		CMD_INVITE = new Command( "invite" );
		CMD_INVITE.addParameter( new StringParameter( "name of event", "The name of the event to add the given user to." ) );
		CMD_INVITE.addParameter( new StringParameter( "username", "The user to invite to the given event." ) );
		
		CMD_GET = new Command( "get" );
		CMD_GET.addParameter( new StringParameter( "name of event", "The name of the event to retrieve information about." ) );
	
		CMD_VOTE = new Command( "vote" );
		CMD_VOTE.addParameter( new StringParameter( "name of event", "The name of the event to vote on." ) );
		CMD_VOTE.setHasRest( true );
		
		CMD_FINALIZE = new Command( "finalize" );
		CMD_FINALIZE.addParameter( new StringParameter( "name of event", "The name of the event to finalize." ) );
		
		CMD_LOGOUT = new Command( "logout" );
		
		CMD_EXIT = new Command( "exit" );
		
		cmdParser = new CommandParser();
		cmdParser.addCommands(	CMD_ADDDATE, CMD_CREATE, CMD_EXIT, CMD_FINALIZE, CMD_GET,
								CMD_INVITE, CMD_LOGIN, CMD_LOGOUT, CMD_REGISTER, CMD_VOTE );
	}

}
