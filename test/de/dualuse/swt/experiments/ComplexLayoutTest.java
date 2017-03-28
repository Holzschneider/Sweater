package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

public class ComplexLayoutTest {
	public static void main(String[] args) {
		
		Display dsp = new Display();
		Shell sh = new Shell();
		
		Label avatar = new Label(sh, NONE);
		
		Label surnameLabel = new Label(sh, RIGHT); surnameLabel.setText("Surname: ");
		Label nameLabel = new Label(sh, RIGHT); nameLabel.setText("First name: ");
		Text surnameText = new Text(sh, SINGLE); surnameText.setText("<enter surname>");
		Text nameText = new Text(sh, SINGLE); nameText.setText("<enter name>");
		
		
		Label descriptionLabel = new Label(sh, NONE); descriptionLabel.setText("Description");
		Text descriptionText = new Text(sh, MULTI|WRAP); descriptionText.setText("<enter description here>");
		
		Button cancelButton = new Button(sh, NONE); cancelButton.setText("Cancel");
		Button acceptButton = new Button(sh, NONE); acceptButton.setText("Accept");
		
		
		int S = 100, M = 16;
		
		sh.setLayout(new Layout() {
			@Override
			protected void layout(Composite composite, boolean flushCache) {
				Rectangle r = composite.getClientArea();
				
				Point cancelButtonSize = cancelButton.computeSize(-1, -1);
				Point acceptButtonSize = acceptButton.computeSize(-1, -1);
				Point surnameLabelSize = surnameLabel.computeSize(-1, -1);
				Point nameLabelSize = nameLabel.computeSize(-1, -1);
				
				Point surnameTextSize = surnameText.computeSize(-1, -1);
				Point nameTextSize = nameText.computeSize(-1, -1);
				
				int namesLabelWidth = Math.max(nameLabelSize.x,surnameLabelSize.x);
				int namesLabelHeight = Math.max(nameTextSize.y,surnameTextSize.y);
				
				avatar.setBounds(r.x, r.y, S, S);
				surnameLabel.setBounds(r.x+S+M, r.y, namesLabelWidth, namesLabelHeight );
				nameLabel.setBounds(r.x+S+M, r.y+namesLabelHeight+M, namesLabelWidth, namesLabelHeight);
				
				surnameText.setBounds(r.x+S+M+namesLabelWidth, r.y, r.width-(r.x+S+M+namesLabelWidth), namesLabelHeight);
				nameText.setBounds(r.x+S+M+namesLabelWidth, r.y+namesLabelHeight+M, r.width-(r.x+S+M+namesLabelWidth), namesLabelHeight);
				
				descriptionLabel.setBounds(r.x, r.y+S+M+namesLabelHeight, r.width, namesLabelHeight);
				descriptionText.setBounds(r.x, r.y+S+M+namesLabelHeight+namesLabelHeight+M, r.width, r.height-(r.y+S+M+namesLabelHeight)-M-acceptButtonSize.y-M);
				
				acceptButton.setBounds(r.x+r.width-acceptButtonSize.x, r.y+r.height-acceptButtonSize.y, acceptButtonSize.x, acceptButtonSize.y);
				cancelButton.setBounds(r.x+r.width-acceptButtonSize.x-cancelButtonSize.x, r.y+r.height-acceptButtonSize.y, cancelButtonSize.x, acceptButtonSize.y);
			}
			
			@Override
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				return new Point(500,400);
			}
		});
		
		
		
		
		
		sh.setBounds(900, 100, 800, 800);
		sh.setVisible(true);
		
		while (!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
	}
}
