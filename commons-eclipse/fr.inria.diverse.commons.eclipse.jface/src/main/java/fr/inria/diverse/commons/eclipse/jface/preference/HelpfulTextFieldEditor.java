/*
    Created on 27-May-2004
 */
package fr.inria.diverse.commons.eclipse.jface.preference;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;


public class HelpfulTextFieldEditor extends TextFieldEditor implements
        HelpfulControl {

    /**
     * 
     */
    public HelpfulTextFieldEditor() {
        super();
        // 
    }

    /**
     * @param name
     * @param labelText
     * @param width
     * @param strategy
     * @param parent
     */
    public HelpfulTextFieldEditor(String name, String labelText, int width,
            int strategy, Composite parent) {
        super(name, labelText, width, strategy, parent);
        // 
    }

    /**
     * @param name
     * @param labelText
     * @param width
     * @param parent
     */
    public HelpfulTextFieldEditor(String name, String labelText, int width,
            Composite parent) {
        super(name, labelText, width, parent);
        // 
    }

    /**
     * @param name
     * @param labelText
     * @param parent
     */
    public HelpfulTextFieldEditor(String name, String labelText,
            Composite parent) {
        super(name, labelText, parent);
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
    
}
