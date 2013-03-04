/*
 * Created on 30.09.2006
 */

package helper;

import junit.framework.TestCase;

public class RuntimeFacadeTest
extends TestCase
{
	public void testAssertJavaVersion()
	{
		try {
			RuntimeFacade.assertJavaVersion( "1.4" );
			RuntimeFacade.assertJavaVersion( "1.4.0" );
			RuntimeFacade.assertJavaVersion( "1.5" );
			RuntimeFacade.assertJavaVersion( "5.0" );
		}catch( RuntimeException e ) {
			fail( e.toString() );
		}
		
		try {
			RuntimeFacade.assertJavaVersion( "1.0" );
			fail( "Should have thrown Exception:1.0");
		}catch( RuntimeException e ) {}
		try {
			RuntimeFacade.assertJavaVersion( "1.3.0_14" );
			fail( "Should have thrown Exception:1.3.0_14");
		}catch( RuntimeException e ) {}
	}

}
