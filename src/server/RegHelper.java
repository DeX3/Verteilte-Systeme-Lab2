package server;

import java.rmi.registry.Registry;

import entities.RegistryInfo;


public class RegHelper {

	/**
	 * @param args
	 */
	public static void main( String[] args ) throws Exception {
		RegistryInfo regInfo = RegistryInfo.readRegistryInfo( "registry.properties" );
		
		Registry reg = regInfo.connect( false );
		
		reg.unbind( "server1" );
	}

}
