package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import cmd.Command;
import cmd.CommandLineParser;
import cmd.CommandParser;
import cmd.DateParameter;
import cmd.IntegerParameter;
import cmd.StringParameter;
import exceptions.ParseException;
import exceptions.ValidationException;


public class Client {

	static final Logger logger = Logger.getLogger( Client.class.getName() );
	
	/**
	 * @param args
	 */
	public static void main( String[] args ) {

		final StringParameter PRM_SERVERNAME = new StringParameter( "serverName", "the name of the remote reference in the RMI registry of the server that shall be responsible for this client." );
		
		CommandLineParser clp = new CommandLineParser( "java client.Client", "Client for the 2nd lab of distributed systems." );
		
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
				{
					
				}
				
				
			}catch( IOException ioex )
			{ logger.warning( "Couldn't read from stdin" ); }
			catch( ParseException pex )
			{ System.out.println( pex.getMessage() ); }
			catch( ValidationException vex )
			{ System.out.println( vex.getMessage() ); }
		}
		
		
		System.out.println( "Shutting down..." );
		
	}
	
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
		CMD_REGISTER.addParameter( new StringParameter( "passsword", "The password required for registration." ) );
		
		CMD_LOGIN = new Command( "login" );
		CMD_LOGIN.addParameter( new StringParameter( "username", "The name to use for logging in." ) );
		CMD_LOGIN.addParameter( new StringParameter( "passsword", "The password required for logging in." ) );
		
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
		
		CMD_FINALIZE = new Command( "finalize" );
		CMD_FINALIZE.addParameter( new StringParameter( "name of event", "The name of the event to finalize." ) );
		
		CMD_LOGOUT = new Command( "logout" );
		
		CMD_EXIT = new Command( "exit" );
		
		cmdParser = new CommandParser();
		cmdParser.addCommands(	CMD_ADDDATE, CMD_CREATE, CMD_EXIT, CMD_FINALIZE, CMD_GET,
								CMD_INVITE, CMD_LOGIN, CMD_LOGOUT, CMD_REGISTER, CMD_VOTE );
	}

}
