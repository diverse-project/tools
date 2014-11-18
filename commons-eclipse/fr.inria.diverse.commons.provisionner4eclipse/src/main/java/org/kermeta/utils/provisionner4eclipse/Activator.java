/*$Id$
* Project : org.kermeta.utils.provisionner.eclipse
* License : EPL
* Copyright : IRISA / INRIA / Universite de Rennes 1
* ----------------------------------------------------------------------------
* Creation date : 2010
* Authors : 
*			Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package org.kermeta.utils.provisionner4eclipse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	//public String[] requiredStartedBundleNames = {"org.ops4j.pax.url.mvn",
	//		"org.ops4j.pax.url.assembly"};
	public String[] requiredStartedBundleNames = {};
	// The plug-in ID
	public static final String PLUGIN_ID = "org.kermeta.utils.provisionner.eclipse";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		for (String requiredStartedBundleName : requiredStartedBundleNames) {
			Bundle requiredbundle = Platform.getBundle(requiredStartedBundleName);
			try {
				if(requiredbundle.getState() != Bundle.ACTIVE){
					requiredbundle.start();
				}
			} catch (BundleException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "failed to start "+requiredStartedBundleName +". Some URI protocols won't be available", e));
			}
		}
		
		super.start(context);
		plugin = this;
		Job job = new Job("OSGI bundle provisionner job") {
			protected IStatus run(IProgressMonitor monitor) {
				new Provisionner().provisionFromPreferences(monitor);
				return Status.OK_STATUS;
			}
		};
	    job.setPriority(Job.LONG);
	    job.schedule(); // start as soon as possible
	
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
