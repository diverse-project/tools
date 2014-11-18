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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import fr.inria.diverse.commons.aether.AetherUtil;
import org.kermeta.utils.provisionner4eclipse.preferences.PreferenceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Provisionner {
	protected List<String> repoToUse = new ArrayList<String>();
	protected List<String> bundleToInstall = new ArrayList<String>();
	protected List<Bundle> bundleToStart = new ArrayList<Bundle>();
	
	/** provisionner that is able to deal with the definition order, will retry if necessary
	 * Note: maybe we can extract the core of the algorithm and have a cleaner code ...
	 * @param monitor
	 */
	public void provisionFromPreferences(IProgressMonitor monitor){
		String repoList = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_REPO_URL_LIST);
		String bundleList = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_BUNDLE_URI_LIST);
		Boolean offline = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_MVN_AETHER_OFFLINE);
		String[] bundleRawURIs = bundleList.split("\n");
		String[] repoRawURIs = repoList.split("\n");
		monitor.beginTask("Provisionning OSGI Bundles", bundleRawURIs.length*2 );
		ArrayList<IStatus> statusChildren = new ArrayList<IStatus>();
		boolean hasStartError = false;
		for (int i = 0; i < bundleRawURIs.length; i++) {
			String bundleURI = bundleRawURIs[i].trim();
			if(!bundleURI.startsWith("#") && !bundleURI.isEmpty()){
				bundleToInstall.add(bundleURI);
			}
		}
		for (int i = 0; i < repoRawURIs.length; i++) {
			String repoUrl = repoRawURIs[i].trim();
			if(!repoUrl.startsWith("#") && !repoUrl.isEmpty()){
				repoToUse.add(repoUrl);
			}
		}
		
		
		
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		
		List<String> bundleLeftToInstall = new ArrayList<String>(bundleToInstall);
		List<Status> lastErrors = new ArrayList<Status>();

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
		
		String message= hasStartError? "One or more bundle failed to start or install":"All bundles have successfully started";
		MultiStatus mstatus = new MultiStatus(Activator.PLUGIN_ID, 0, message, null);
		for (IStatus iStatus : statusChildren) {
			mstatus.add(iStatus);
		}
		Activator.getDefault().getLog().log(mstatus);						
		monitor.done();
	}
	
	
	/**
	 * Provision from the preferences 
	 * the bundles must be in the correct order directly in the preferences
	 * @param monitor
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
