package org.eclipse.emf.ecoretools.registration.popup.actions;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.ecoretools.registration.RegistrationPlugin;
import org.eclipse.emf.ecoretools.registration.UMLRegistering;
import org.eclipse.emf.ecoretools.registration.view.RegisteredPackageView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class UMLRegisteringJob extends Job {

	private IFile umlFile;
	private UMLRegisteringAction action;
	
	public UMLRegisteringJob(String name, IFile file, UMLRegisteringAction act) {
		super(name);
		umlFile = file;
		action = act;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		monitor.beginTask("UMLRegisteringJob", Monitor.UNKNOWN);
		
		UMLRegistering.registerPackages(umlFile);

		
		/*EMFRegisterAction a = new EMFRegisterAction();
		a.displayRegisteredPackages();*/
		Display.getDefault().asyncExec(new Runnable() {
	           public void run() {
	        	   action.displayRegisteredPackages();
	           }
	        });

		
		
		
		
		monitor.done();
		return new Status(IStatus.OK, "org.eclipse.emf.ecoretools.registration", "registration done");
	}


	
}
