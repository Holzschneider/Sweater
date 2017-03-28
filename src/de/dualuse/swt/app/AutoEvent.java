package de.dualuse.swt.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@interface AutoAssignment {
//int[] event() default {};
//String[] string() default {};
//double[] number() default {};
//double[] bool() default {};
//
//}

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoEvent {
	
	//events to trigger on
	int[] value() default {};
	///
	
	//semantic handlers (in case of assigned to property) 
	int[] inc() default {};
	int[] dec() default {};
	int[] not() default {};
	int[] nullify() default {};
	//... invent more!
	

	
	
	//Event Value Assignments (also just for properties of the right type)
	int[] x() default {};
	int[] y() default {};
	int[] point() default {}; //virtual event value pair new Point(e.x, e.y)
	

}


/*
  		Component c = new Component(sh, NONE) {
			
			@AutoEvent(not={MouseDown,MouseUp})
			boolean down = false;

			@AutoEvent(point={MouseDown},nullify={MouseUp})
			Point last = new Point(0,0);

			
//			@AutoEventOutlet(assign=@AutoAssignment(string="hallo",))
//			String message;
			
			@AutoEvent(MouseDown) public void onDown(Event e) { down = true; }
			@AutoEvent(MouseUp) public void onUp(Event e) { down = false; }
			
			@AutoEvent(MouseMove)			
			public void onMove(Event e) {
				if (down)
					;
			}

			
		};

*/
