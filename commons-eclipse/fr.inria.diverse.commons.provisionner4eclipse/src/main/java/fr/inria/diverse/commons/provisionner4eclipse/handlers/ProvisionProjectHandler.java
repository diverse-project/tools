package fr.inria.diverse.commons.provisionner4eclipse.handlers;

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

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ProvisionProjectHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ProvisionProjectHandler() {
	}

	
	IProject exportedProject;
	
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		exportedProject = getSelectedProject(event);
		String projectName = exportedProject.getName();
		
		/*MessageDialog.openInformation(
				window.getShell(),
				"Provisionner for Eclipse",
				"Provisionning project "+projectName+" into eclipse");*/
		
		// run in a job to make sure to release the UI
		Job job = new Job("Provisionning "+projectName){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return new Provisionner().provisionFromProject(exportedProject, monitor);
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
