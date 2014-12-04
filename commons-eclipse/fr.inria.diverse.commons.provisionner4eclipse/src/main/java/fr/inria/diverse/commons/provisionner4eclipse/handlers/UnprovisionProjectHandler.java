package fr.inria.diverse.commons.provisionner4eclipse.handlers;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kermeta.utils.provisionner4eclipse.Provisionner;
import org.kermeta.utils.provisionner4eclipse.Unprovisionner;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class UnprovisionProjectHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public UnprovisionProjectHandler() {
	}

	
	IProject selectedProject;
	
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		selectedProject = getSelectedProject(event);
		String projectName = selectedProject.getName();
		
		/*MessageDialog.openInformation(
				window.getShell(),
				"Provisionner for Eclipse",
				"Provisionning project "+projectName+" into eclipse");*/
		
		// run in a job to make sure to release the UI
		Job job = new Job("Unprovisionning "+projectName){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				File jarFolderFile = new File(selectedProject.getFolder(Provisionner.DYNAMIC_JAR_FOLDER).getRawLocation().toOSString());
				return new Unprovisionner().unprovision(jarFolderFile, monitor);
			}
			
		};
		job.schedule();	
		return null;
	}
	
	
	protected IProject getSelectedProject(ExecutionEvent event){
		IProject selectedProject = null;
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null & selection instanceof IStructuredSelection) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			for (@SuppressWarnings("unchecked")
				Iterator<Object> iterator = strucSelection.iterator(); 
				iterator.hasNext();) {
				
				Object element = iterator.next();

				if (element instanceof IResource) {
					selectedProject = ((IResource) element)
							.getProject();

				}
				if (element instanceof IAdaptable) {
					IResource res = (IResource) ((IAdaptable) element)
							.getAdapter(IResource.class);
					if (res != null) {
						selectedProject = res.getProject();
					}
				}
			}
		}
		return selectedProject;
	}
	

}
