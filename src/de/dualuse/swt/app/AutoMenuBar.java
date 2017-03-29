package de.dualuse.swt.app;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

interface Test {
	static public final int bla = 10;
}

public interface AutoMenuBar {
	static public final String SEPARATOR = "_";
	
	static public final int CTRL = 1<<31;
	static public final int ALT = 1<<30;
	static public final int SHIFT = 1<<29;
	static public final int META = 1<<28;
	static public final int FUNCTION = 1<<27;
	
	///
	
	static public final char VK_BACK_SPACE     = '\b';
	static public final char DELETE         = 0x7F; /* ASCII DEL */
    static public final char CAPS_LOCK      = 0x14;
    static public final char ESCAPE         = 0x1B;
    static public final char SPACE          = 0x20;
    static public final char PAGE_UP        = 0x21;
    static public final char PAGE_DOWN      = 0x22;
    static public final char END            = 0x23;
    static public final char HOME           = 0x24;
    static public final char LEFT           = 0x25;
    static public final char UP             = 0x26;
    static public final char RIGHT          = 0x27;
    static public final char DOWN           = 0x28;

	
    
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	static public @interface AutoMenu {
		String[] value();
		int rank() default 0;
		int[] ranks() default 0;
		boolean enabled() default true;
		MenuScope[] scope() default MenuScope.UNSPECIFIED;
	}

	@Target({ElementType.FIELD,ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	static public @interface AutoMenuItem {
		String[] path();
		int rank() default 0;
		boolean split() default false;
		int accelerator() default 0;
		boolean check() default false;
		String group() default "";
		boolean enabled() default true;
		String icon() default "";
		MenuScope[] scope() default MenuScope.UNSPECIFIED; 
	}
	
	static public enum MenuScope {
		UNSPECIFIED,
//		TRAY,
		WINDOW,
		SYSTEM,
		APPLICATION;
	}
	
}











