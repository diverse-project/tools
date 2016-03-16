/*$Id: BundleProvisionnerPreferencePage.java 15525 2011-07-13 15:22:05Z dvojtise $
* Project : org.kermeta.utils.provisionner.eclipse
* License : EPL
* Copyright : IRISA / INRIA / Universite de Rennes 1
* ----------------------------------------------------------------------------
* Creation date : 2010
* Authors : 
*			Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.eclipse.messagingsystem.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fr.inria.diverse.commons.eclipse.messagingsystem.ui.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class EclipseMessagingSystemPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public EclipseMessagingSystemPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configuration of DiverSE console");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		/* HelpfulTextFieldEditor headerFE = new HelpfulTextFieldEditor(PreferenceConstants.P_BUNDLE_URI_LIST, 
				"&Bundle URI List (one per line):\n", 
				getFieldEditorParent());		
		Font font = new Font(this.getShell().getDisplay(), "Courier", 8, SWT.NORMAL);;
		headerFE.getTextControl(getFieldEditorParent()).setFont(font);
		headerFE.setToolTipText("Add these bundles to your current Eclipse installation.\n");
		addField(headerFE); */
		//addField(
		//	new StringFieldEditor(PreferenceConstants.P_CAPTURE_SYSTEM_ERROUT, "Bundle URI List:", getFieldEditorParent());
		addField(
			new org.eclipse.jface.preference.BooleanFieldEditor(PreferenceConstants.P_CAPTURE_SYSTEM_ERROUT, "try to capture System.out and System.err in DiverSE default console:", getFieldEditorParent()));
		addField(
				new org.eclipse.jface.preference.IntegerFieldEditor(PreferenceConstants.P_MAX_CONSOLE_OUTPUT, "consoles buffer size (characters)(0 for no limit):", getFieldEditorParent()));
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performApply() {
		super.performApply();
		doIt();
	}

	
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		doIt();
		return result;
	}

	protected void doIt(){
		Boolean mustCapture = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_CAPTURE_SYSTEM_ERROUT);
		if(mustCapture){
			Activator.getDefault().captureSystemOutAndErr();
		}
		else{
			Activator.getDefault().releaseSystemOutAndErr();
		}
	}
	
	
}