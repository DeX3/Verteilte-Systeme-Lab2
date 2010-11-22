package cmd;

import java.text.SimpleDateFormat;
import java.util.Date;

import exceptions.ParseException;
import exceptions.ValidationException;


public class DateParameter extends Parameter<Date> {

	public static final String FORMAT_STRING = "dd.MM.yyyy/HH:mm";
	
	public DateParameter( String name )
	{
		super( name );
	}
	
	public DateParameter( String name, String description ) {
		super( name );
		
		this.description = description;
	}
	

	@Override
	public void parse( String str ) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat( FORMAT_STRING );
		
		try {
			this.value = sdf.parse( str );
		} catch( java.text.ParseException e ) {
			throw new ParseException( "The given date does not have the valid format." );
		}
	}

	@Override
	public void validate() throws ValidationException {
		//Nothing to validate
	}

}
