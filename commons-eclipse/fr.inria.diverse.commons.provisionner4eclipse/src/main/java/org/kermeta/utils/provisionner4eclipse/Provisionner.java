/*$Id$
* Project : org.kermeta.utils.provisionner.eclipse
* License : EPL
* Copyright : IRISA / INRIA / Universite de Rennes 1
* ----------------------------------------------------------------------------
* Creation date : 10 aout 2010
* Authors : 
*			Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package org.kermeta.utils.provisionner4eclipse;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.ui.progress.IProgressConstants;
import org.kermeta.utils.provisionner4eclipse.preferences.PreferenceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import fr.inria.diverse.commons.aether.AetherUtil;

public class Provisionner {
	
	public final static String DYNAMIC_PROVISIONNER_FOLDER = "target/dynamic_provisionner";
	public final static String DYNAMIC_JAR_FOLDER = DYNAMIC_PROVISIONNER_FOLDER+"/plugins";
	
	/**
	 * Unprovision if required 
	 * then export and provision the given project
	 * @param project
	 * @param monitor
	 * @return
	 */
	public IStatus provisionFromProject(final IProject project, IProgressMonitor monitor){
		return provisionFromProject(project, false,  monitor);
	}
	
	/**
	 * 
	 * Unprovision if required 
	 * then export and provision the given project and all its dependencies defined in the current workspace
	 * @param project
	 * @param provisionDependecies
	 * @param monitor
	 * @return
	 */
	public IStatus provisionFromProject(final IProject project, boolean provisionDependecies, IProgressMonitor monitor){
		IFolder jarFolder = project.getFolder(DYNAMIC_JAR_FOLDER);
		File jarFolderFile = new File(project.getFolder(DYNAMIC_JAR_FOLDER).getRawLocation().toOSString());
		
		IStatus res = new Unprovisionner().unprovision(jarFolderFile, monitor);
		if(!res.isOK()) return res;
		
		res = exportProject(project, provisionDependecies);
		try {
			jarFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e) {}
		if(!res.isOK()) return res;
		
		return provision(getJarsInFolder(jarFolderFile), new ArrayList<String>(), true, monitor);
	}
	
	
	public IStatus provisionFromPreferences(IProgressMonitor monitor){
		String repoList = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_REPO_URL_LIST);
		String bundleList = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_BUNDLE_URI_LIST);
		Boolean offline = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_MVN_AETHER_OFFLINE);
		String[] bundleRawURIs = bundleList.split("\n");
		String[] repoRawURIs = repoList.split("\n");
		List<String> bundleToInstall = new ArrayList<String>();
		for (int i = 0; i < bundleRawURIs.length; i++) {
			String bundleURI = bundleRawURIs[i].trim();
			if(!bundleURI.startsWith("#") && !bundleURI.isEmpty()){
				bundleToInstall.add(bundleURI);
			}
		}
		List<String> repoToUse = new ArrayList<String>();
		for (int i = 0; i < repoRawURIs.length; i++) {
			String repoUrl = repoRawURIs[i].trim();
			if(!repoUrl.startsWith("#") && !repoUrl.isEmpty()){
				repoToUse.add(repoUrl);
			}
		}
		return provision(bundleToInstall, repoToUse, offline, monitor);
	}
	
	
	
	/** provisionner that is able to deal with the definition order, will retry if necessary
	 * Note: maybe we can extract the core of the algorithm and have a cleaner code ...
	 * @param monitor
	 */
	public IStatus provision(List<String> allBundleToInstall, List<String> repoToUse, Boolean offline, IProgressMonitor monitor){
		

		monitor.beginTask("Provisionning OSGI Bundles", allBundleToInstall.size()*2 );
		List<String> bundleToInstall = new ArrayList<String>(allBundleToInstall);
		ArrayList<IStatus> statusChildren = new ArrayList<IStatus>();
		boolean hasStartError = false;
		
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		
		List<String> bundleLeftToInstall = new ArrayList<String>(bundleToInstall);
		List<Status> lastErrors = new ArrayList<Status>();
		
		List<Bundle> bundleToStart = new ArrayList<Bundle>();

		// install all bundle, take care of the order
		while(bundleLeftToInstall.size() != 0){
			bundleLeftToInstall.clear();

			boolean hasProcessedAtLeastOne = false;
			for (String bundleURI : bundleToInstall) {
				if(monitor.isCanceled()){
					statusChildren.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "user interruption : bundle not provisionned " + bundleURI, null));
					bundleLeftToInstall.clear();
				}
				else{
					try{
						monitor.subTask("Installing "+bundleURI);
						String resolvedURI = bundleURI;
						if(bundleURI.startsWith("mvn:")){
							AetherUtil aetherUtil = new AetherUtil();
							aetherUtil.setOffline(offline);
							File theFile = aetherUtil.resolveMavenArtifact4J(bundleURI, repoToUse);
							if(theFile.exists()){
								//result.add(theFile.getAbsolutePath());
								resolvedURI = theFile.toURI().toString();
							}
						}
						Bundle bundle = context.installBundle(resolvedURI);
						bundleToStart.add(bundle);
						hasProcessedAtLeastOne = true;
						monitor.worked(1);
					}
					
					catch (Exception e) {
						if(e.getClass().getName().equals("org.eclipse.osgi.framework.internal.core.Framework$DuplicateBundleException")){
							statusChildren.add(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "ingored installation of already installed bundle " + bundleURI, e));
							hasProcessedAtLeastOne = true;
						}
						else{
							lastErrors.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "failed to install " + bundleURI, e));
							bundleLeftToInstall.add(bundleURI);
						}					
					}	
				}
			}
			if(!hasProcessedAtLeastOne){
				// nothing more can be done, report the errors and exit the loop
				// report the errors
				for(Status error : lastErrors){
					statusChildren.add(error);
					monitor.worked(2);
				}
				bundleLeftToInstall.clear();
				hasStartError = true;
			}
			else{
				bundleToInstall.clear();
				bundleToInstall.addAll(bundleLeftToInstall);
			}
			lastErrors.clear();
		}
		
		// Starting bundle
		List<Bundle> bundleLeftToStart = new ArrayList<Bundle>(bundleToStart);
		while(bundleLeftToStart.size() != 0){
			bundleLeftToStart.clear();

			boolean hasProcessedAtLeastOne = false;
			for (Bundle bundle : bundleToStart) {
				if(monitor.isCanceled()){
					monitor.subTask("Ignoring start of "+bundle.getSymbolicName() );
					lastErrors.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "user interruption : bundle not started " + bundle.getSymbolicName(), null));
					hasStartError = true;
					monitor.worked(1);
					bundleLeftToStart.clear();
				}
				else {
					try{
						monitor.subTask("Starting "+bundle.getSymbolicName() );
						bundle.start();
						statusChildren.add(new Status(IStatus.INFO, Activator.PLUGIN_ID, 0, bundle.getSymbolicName() + " installed and started ",null));
						hasProcessedAtLeastOne = true;
						monitor.worked(1);
					}
					catch (Exception e) {
						lastErrors.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "failed to start " + bundle.getSymbolicName(), e));
						bundleLeftToStart.add(bundle);
						hasStartError = true;
					}
				}
			}
			if(!hasProcessedAtLeastOne){
				// nothing more can be done, report the errors and exit the loop
				// report the errors
				for(Status error : lastErrors){
					statusChildren.add(error);
				}
				bundleLeftToStart.clear();
			}
			else{
				bundleToStart.clear();
				bundleToStart.addAll(bundleLeftToStart);
			}
			lastErrors.clear();
		}

		monitor.done();
		if(allBundleToInstall.size() > 0){ // no log if no bundle to install and start
			String message= hasStartError? "One or more bundle failed to start or install":"All bundles have successfully started";
			MultiStatus mstatus = new MultiStatus(Activator.PLUGIN_ID, 0, message, null);
			for (IStatus iStatus : statusChildren) {
				mstatus.add(iStatus);
			}
			
			Activator.getDefault().getLog().log(mstatus);
			if(hasStartError)
				return mstatus;
			else 
				return Status.OK_STATUS;
		}
		else {
			return Status.OK_STATUS;
		}
	}
	
	
	protected IStatus exportProject(final IProject exportedProject, boolean exportDependencies) {
		// NOTE: Any changes to the content here must also be copied to generateAntTask() and PluginExportTask
		final FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = true;
		info.useJarFormat = true;
		info.exportSource = false;
		info.exportSourceBundle = false;
		info.allowBinaryCycles = true;
		info.useWorkspaceCompiledClasses = true;

		IFolder target = exportedProject.getFolder(DYNAMIC_PROVISIONNER_FOLDER);
		info.destinationDirectory = target.getRawLocation().toOSString();
		info.zipFileName = null;		
		info.items = getExportedItems(exportedProject, exportDependencies).toArray();
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
			target.refreshLocal(IResource.DEPTH_INFINITE, null);
			target.delete(true, null);
		} catch (CoreException e1) {}
		
		final PluginExportOperation job = new PluginExportOperation(info, PDEUIMessages.PluginExportJob_name);
		job.setUser(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return job.getResult();
		
	}
	
	protected List<Object> getExportedItems(IProject exportedProject, boolean exportDependencies){
		HashSet<Object> set = new HashSet<Object>();
		getExportedItems(exportedProject, set, exportDependencies);
		List<Object> list = new ArrayList<Object>();
		list.addAll(set);
		return list;
	}
	
	protected void getExportedItems(IProject exportedProject, HashSet<Object> set, boolean exportDependencies){		
		IModel projectModel = findModelFor(exportedProject);
		if(set.contains(projectModel)) return; // ensure exit in case of circular dependency
		set.add(projectModel);
		if(exportDependencies && projectModel instanceof IPluginModelBase){
			IPluginModelBase pluginModel = (IPluginModelBase)projectModel;
			BundleDescription[] requiredDescriptions = pluginModel.getBundleDescription().getResolvedRequires();
			for (BundleDescription bundleDescription : requiredDescriptions) {
				IPluginModelBase dependencyModel = PluginRegistry.findModel(bundleDescription.getSymbolicName());
				IResource depRes= dependencyModel.getUnderlyingResource();
				if(depRes!=null){
					// recursively add indirect dependencies
					getExportedItems(depRes.getProject(), set, exportDependencies);
				}
			}
		}
	}
	
	
	
	public List<String> getJarsInFolder(File jarFolder){
		ArrayList<String> bundleToInstall = new ArrayList<String>();
		if(jarFolder.isFile()){
			// use current file as jar
			bundleToInstall.add(jarFolder.toURI().toASCIIString());
		}
		File[] jars = jarFolder.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().endsWith(".jar");
			}
		});
		for (int i = 0; i < jars.length; i++) {
			bundleToInstall.add(jars[i].toURI().toASCIIString());
		}
		return bundleToInstall;
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
	
	/**
	 * Provision from the preferences 
	 * the bundles must be in the correct order directly in the preferences
	 * @param monitor
	 * @deprecated
	 */
	public void provisionFromPreferences_fixedOrder(IProgressMonitor monitor){
		String bundleList = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_BUNDLE_URI_LIST);
		String[] bundleRawURIs = bundleList.split("\n");
		monitor.beginTask("Provisionning OSGI Bundles", bundleRawURIs.length);
		ArrayList<IStatus> statusChildren = new ArrayList<IStatus>();
		boolean hasStartError = false;
		for (int i = 0; i < bundleRawURIs.length; i++) {
			String bundleURI = bundleRawURIs[i].trim();
			if(!bundleURI.startsWith("#") && !bundleURI.isEmpty()){
				if(monitor.isCanceled()){
					statusChildren.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "user interruption : bundle not provisionned " + bundleURI, null));
				}
				else{
					monitor.subTask("Provisionning "+bundleURI);
					try{
						BundleContext context = Activator.getDefault().getBundle().getBundleContext();
						Bundle bundle = context.installBundle(bundleURI);
						try{
							bundle.start();
							statusChildren.add(new Status(IStatus.INFO, Activator.PLUGIN_ID, 0, bundleURI + " installed and started ",null));
						}
						catch (Exception e) {
							statusChildren.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "failed to start " + bundleURI, e));
							hasStartError = true;
						}
					}
					
					catch (Exception e) {
						if(e.getClass().getName().equals("org.eclipse.osgi.framework.internal.core.Framework$DuplicateBundleException")){
							statusChildren.add(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "ingored installation of already installed bundle " + bundleURI, e));
						}
						else{
							statusChildren.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "failed to install " + bundleURI, e));
						}
						hasStartError = true;
					}
				}
			}
			monitor.worked(1);
		}
		
		String message= hasStartError? "One or more bundle failed to start or install":"All bundles have successfully started";
		MultiStatus mstatus = new MultiStatus(Activator.PLUGIN_ID, 0, message, null);
		for (IStatus iStatus : statusChildren) {
			mstatus.add(iStatus);
		}
		Activator.getDefault().getLog().log(mstatus);						
		monitor.done();
	}
}
