/*
    Created on 27-May-2004
 */
package fr.inria.diverse.commons.eclipse.jface.preference;


import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;


public class HelpfulFileFieldEditor extends FileFieldEditor implements
        HelpfulControl {

    /**
     * 
     */
    public HelpfulFileFieldEditor() {
        super();
        // 
    }

    /**
     * @param name
     * @param labelText
     * @param parent
     */
    public HelpfulFileFieldEditor(String name, String labelText,
            Composite parent) {        
        super(name, labelText, false, parent);
        // 
    }

    /**
     * @param name
     * @param labelText
     * @param enforceAbsolute
     * @param parent
     */
    public HelpfulFileFieldEditor(String name, String labelText,
            boolean enforceAbsolute, Composite parent) {
        super(name, labelText, enforceAbsolute, parent);
        // 
    }


    public Control[] getControls() {
        return new Control[] {getLabelControl(), getTextControl()};
    }
    public void setToolTipText(String text) {
        Control[] controls = getControls();
        for(int i=0; i<controls.length; i++) {
            controls[i].setToolTipText(text);
        }
    }
    public void setHelp(String contextId) {
        Control[] controls = getControls();
        for(int i=0; i<controls.length; i++) {
        	PlatformUI.getWorkbench().getHelpSystem().setHelp(controls[i], contextId);
        }
    }
    
   
    /**
     * over-ridden to allow findFile a chance to locate the file
     */
	protected boolean checkState() {
		return super.checkState();
	}

}
