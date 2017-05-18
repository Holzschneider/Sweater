package de.dualuse.util.logging;

import java.io.*;

public interface Silenced {
	
	@Deprecated
	static class System extends FakeSystem {
		@Deprecated
		static final private OutputStream silentStream = new OutputStream() {
			public void write(int b) throws IOException { }
			public void write(byte[] b) throws IOException {  }
			public void write(byte[] b, int off, int len) throws IOException {  }
		};
		
		@Deprecated
		static public final PrintStream out = new PrintStream(silentStream,true);

		@Deprecated
		static public final PrintStream err = new PrintStream(silentStream,true);

		@Deprecated
		static public final InputStream in = java.lang.System.in;
	}

}
