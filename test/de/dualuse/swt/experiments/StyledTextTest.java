package de.dualuse.swt.experiments;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.dualuse.swt.layout.BorderLayout;
import de.dualuse.swt.util.SWTUtil;

public class StyledTextTest {
	
	static String[] keywords = {
		"int", "float", "double", "char", "byte", "long", "short",
		"public", "private", "protected",
		"static", "volatile", "final",
		"class", "implements", "extends"
	};
	
	static void updateStyle(Display dsp, StyledText text, int from, int to) {
		String str = text.getText();
		str = str.substring(from, to);
		
		ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();
		
		for (String keyword : keywords) {
			int cur = 0;
			while((cur = str.indexOf(keyword, cur)) != -1) {
				System.out.println(cur);
				StyleRange range = new StyleRange();
				range.start = from + cur;
				range.length = keyword.length();
				range.foreground = dsp.getSystemColor(SWT.COLOR_BLUE);
				ranges.add(range);
				cur++;
			}
		}
		
		int[] startLength = new int[ranges.size()*2];
		for (int i=0, I=ranges.size(); i<I; i++) {
			StyleRange range = ranges.get(i);
			startLength[i] = range.start;
			startLength[i+1] = range.length;
		}
		
		text.setStyleRanges(from, to-from, startLength, ranges.toArray(new StyleRange[0]));
	}
	
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp, SWT.BORDER);
		shell = new Shell();
		shell.setLayout(new FillLayout());
		
		SWTUtil.center(shell, 800, 600);
		SWTUtil.exitOnClose(shell);
		
		final StyledText text = new StyledText(shell, SWT.V_SCROLL);
		
		text.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
//				
//				String str = text.getText();
//				System.out.println(str);
//				
//				StyleRange range = new StyleRange();
//				range.start = 0;
//				range.length = text.getCharCount();
//				range.foreground = dsp.getSystemColor(SWT.COLOR_RED);
//				
//				text.setStyleRange(range);
//				
			}		
		});
		
		text.addExtendedModifyListener(new ExtendedModifyListener() {
			@Override public void modifyText(ExtendedModifyEvent event) {
				String str = text.getText();
				
				int start = event.start;
				int length = event.length;
				int total = text.getCharCount();
				
//				System.out.println("Start: " + start);
//				System.out.println("Length: " + length);
//				System.out.println("Total: " + total);
				
				int from = Math.max(0, Math.min(start, total-1));
				while (from>0 && !Character.isWhitespace(str.charAt(from)))
					from--;
				
				int to = start + length;
				while (to < total && !Character.isWhitespace(str.charAt(to)))
					to++;
				
				updateStyle(dsp, text, from, to);
			}
		});
		
//		text.addVerifyListener((e) -> {
//			if (e.text.contains("e"))
//				e.doit = false;
//		});
		
		text.setText("Test");
		
		shell.open();
		
		SWTUtil.eventLoop();
	}
}
