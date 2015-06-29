/*
 * Created on 27.09.2006
 */

package helper;

import java.text.NumberFormat;

public class RuntimeFacade
{
	final private static Runtime runtime = Runtime.getRuntime();
	
	public static void collectGarbage()
	{
		for( int i=0;i<4;i++ ) {
			runtime.runFinalization();
			runtime.gc();
			Thread.yield();
		}
	}
	
	public static void printMemoryUsage( String mark )
	{
	  NumberFormat numberFormat = NumberFormat.getPercentInstance();
		long total = runtime.totalMemory();
		long free  = runtime.freeMemory();
		long used  = total-free;
		
		StringBuilder sb = new StringBuilder( 32 );
		sb.append( mark );
		sb.append( ": " );
		sb.append( numberFormat.format(used/(double)total));
		sb.append( " of ");
    sb.append( (total/1000000) );
    sb.append( "MB" );
		System.out.println( sb.toString() );
	}
	
	public static void assertJavaVersion()
	{
		assertJavaVersion( System.getProperty( "java.version" ) );
	}

	static void assertJavaVersion( String javaVersion )
	{
		if( javaVersion.compareTo( "1.7" )<0 ) {
			throw new RuntimeException( "Dieses Spiel verlangt mindestens eine Javaversion von 1.7. Sie verwenden Version:"+javaVersion+". Um eine aktuelle Javaversion downzuloaden wenden Sie sich an www.sun.com .");
		}
	}

}
