package org.eclipse.emf.ecoretools.registration.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecoretools.registration.UMLRegistering;
import org.eclipse.jface.action.IAction;

public class UMLRegisteringAction extends EMFRegisterAction {

	/**
	 * Constructor
	 */
	public UMLRegisteringAction() {
		super();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		for(IFile umlFile : umlFiles) {	
			//try {
			// run in a job
	    	UMLRegisteringJob myJob = new UMLRegisteringJob("UML Profile registration ", umlFile, this);
	    	myJob.setUser(true);
	    	

			myJob.schedule();
			
			
			
			//System.out.println("Calling UMLRegistering.registerPackages for " + umlFile.getName());
				
			/*} catch (NotValidEPackageURIException e) {
				Shell shell = new Shell();
				MessageDialog.openWarning(
					shell,
					"EPackage registration",
					"The EPackage: " + e.getEPackage().getName() + " cannot be registered, because its nsUri is not defined, all its subpackages have not been registered.");
			}*/
		}
		
		//displayRegisteredPackages();
	}

}
