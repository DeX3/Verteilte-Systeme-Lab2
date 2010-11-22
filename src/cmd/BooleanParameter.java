package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;


public class BooleanParameter extends Parameter<Boolean> {

	public BooleanParameter( String name ) {
		super(name);
	}
	
	public BooleanParameter( String name, String description )
	{
		this( name );
		this.description = description;
	}

	@Override
	public void parse( String str ) throws ParseException {
		this.value = Boolean.parseBoolean( str );
	}

	@Override
	public void validate() throws ValidationException {
	}

}
