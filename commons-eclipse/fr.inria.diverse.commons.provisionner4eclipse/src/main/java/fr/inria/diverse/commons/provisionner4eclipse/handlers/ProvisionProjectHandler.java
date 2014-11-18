package fr.inria.diverse.commons.provisionner4eclipse.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.PDECoreMessages;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
//import org.eclipse.pde.internal.build.site.QualifierReplacer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;
import org.kermeta.utils.provisionner4eclipse.DynamicJarsProvisionnerJob;
import org.kermeta.utils.provisionner4eclipse.DynamicJarsUnprovisionnerJob;

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
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		exportedProject = getSelectedProject(event);
		String projectName = exportedProject.getName();
		
		/*MessageDialog.openInformation(
				window.getShell(),
				"Provisionner for Eclipse",
				"Provisionning project "+projectName+" into eclipse");*/
		File jarFolder = new File(exportedProject.getFolder("target/dynamic_provisionner/plugins").getRawLocation().toOSString());
		
		DynamicJarsUnprovisionnerJob unprovisionnerJob = new DynamicJarsUnprovisionnerJob("Unprovisionning old jars", jarFolder);
		unprovisionnerJob.setUser(true);
		unprovisionnerJob.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_FEATURE_OBJ);
		unprovisionnerJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				scheduleExportJob();
			}
		});
		unprovisionnerJob.schedule();
		return null;
	}
	
	
	protected IModel findModelFor(IAdaptable object) {
		if (object instanceof IJavaProject)
			object = ((IJavaProject) object).getProject();
		if (object instanceof IProject)
			return PluginRegistry.findModel((IProject) object);
		/*if (object instanceof PersistablePluginObject) {
			IPluginModelBase model = PluginRegistry.findModel(((PersistablePluginObject) object).getPluginID());
			if (model != null && model.getUnderlyingResource() != null) {
				return model;
			}
		}*/
		return null;
	}
	
	protected List<Object> getExportedItems(){
		ArrayList list = new ArrayList<Object>();
		list.add(findModelFor(exportedProject));
		return list;
	}
	
	protected void scheduleExportJob() {
		// NOTE: Any changes to the content here must also be copied to generateAntTask() and PluginExportTask
		final FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = true;
		info.useJarFormat = true;
		info.exportSource = false;
		info.exportSourceBundle = false;
		info.allowBinaryCycles = true;
		info.useWorkspaceCompiledClasses = true;

		IFolder target = exportedProject.getFolder("target/dynamic_provisionner");
		info.destinationDirectory = target.getRawLocation().toOSString();
		info.zipFileName = null;
		info.items = getExportedItems().toArray();
		info.signingInfo = null;
		info.exportMetadata = false;
		//info.qualifier = QualifierReplacer.getDateQualifier();
		info.qualifier = "";

		/*final boolean installAfterExport = fPage.doInstall();
		if (installAfterExport) {
			RuntimeInstallJob.modifyInfoForInstall(info);
		}*/

		// cleaning old folder
		try {
			target.delete(true, null);
		} catch (CoreException e1) {}
		
		final PluginExportOperation job = new PluginExportOperation(info, PDEUIMessages.PluginExportJob_name);
		job.setUser(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				try {
					exportedProject.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {}
				if (job.hasAntErrors()) {
					// If there were errors when running the ant scripts, inform the user where the logs can be found.
					final File logLocation = new File(info.destinationDirectory, "logs.zip"); //$NON-NLS-1$
					if (logLocation.exists()) {
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							public void run() {
								AntErrorDialog dialog = new AntErrorDialog(logLocation);
								dialog.open();
							}
						});
					}
				}  else if (event.getResult().isOK() ) {
					// Provision the export into the current running platform
					File jarFolder = new File(exportedProject.getFolder("target/dynamic_provisionner/plugins").getRawLocation().toOSString());
					DynamicJarsProvisionnerJob provisionJob = new DynamicJarsProvisionnerJob("Project dynamic provisionning", jarFolder);
					provisionJob.setUser(true);
					provisionJob.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_FEATURE_OBJ);
					provisionJob.schedule();
				}
			}
		});
		job.schedule();
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
	
	
	protected class AntErrorDialog extends MessageDialog {
		private File fLogLocation;

		public AntErrorDialog(File logLocation) {
			super(PlatformUI.getWorkbench().getDisplay().getActiveShell(), PDECoreMessages.FeatureBasedExportOperation_ProblemDuringExport, null, null, MessageDialog.ERROR, new String[] {IDialogConstants.OK_LABEL}, 0);
			fLogLocation = logLocation;
		}

		protected Control createMessageArea(Composite composite) {
			Link link = new Link(composite, SWT.WRAP);
			try {
				link.setText(NLS.bind(PDEUIMessages.PluginExportWizard_Ant_errors_during_export_logs_generated, "<a>" + fLogLocation.getCanonicalPath() + "</a>")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				PDEPlugin.log(e);
			}
			GridData data = new GridData();
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			link.setLayoutData(data);
			link.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						Program.launch(fLogLocation.getCanonicalPath());
					} catch (IOException ex) {
						PDEPlugin.log(ex);
					}
				}
			});
			return link;
		}
	}
}
