package de.dualuse.util.logging;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

@Deprecated
class FakeSystem {
	
	static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) { System.arraycopy(src, srcPos, dest, destPos, length); }
	static long currentTimeMillies() { return System.currentTimeMillis(); }
	static void exit(int status) { System.exit(status); }
	static void gc() { System.gc(); }
	
	
	static String getenv(String name) { return System.getenv(name); }
	
	static Properties getProperties() { return System.getProperties(); }
	static String	getProperty(String key)  { return System.getProperty(key); }
	static String	getProperty(String key, String def) { return System.getProperty(key,def); }
	static SecurityManager	getSecurityManager() { return System.getSecurityManager(); }
	
	static int	identityHashCode(Object x) { return System.identityHashCode(x); }
	
	static void	load(String filename) { System.load(filename); }

	static void	loadLibrary(String libname) { System.loadLibrary(libname); };
	
	static String	mapLibraryName(String libname) { return System.mapLibraryName(libname); }
	
	static void	runFinalization() { System.runFinalization(); }

	@Deprecated
	static void	runFinalizersOnExit(boolean value) { System.runFinalizersOnExit(value); }
	
	static void	setErr(PrintStream err) { System.setErr(err); }

	static void	setIn(InputStream in) { System.setIn(in); }
	
	static void	setOut(PrintStream out) { System.setOut(out); }
	
	static void	setProperties(Properties props) { System.setProperties(props); }

	static String setProperty(String key, String value) { return System.setProperty(key, value); }
	
	static void	setSecurityManager(SecurityManager s) { setSecurityManager(s); } 
	
	public static long nanoTime() { return System.nanoTime(); }
	
	
}
