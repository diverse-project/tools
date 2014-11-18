package org.kermeta.utils.provisionner4eclipse;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class Unprovisionner {

	
	public IStatus unprovision(File jarFolder, IProgressMonitor monitor){
		String unprovisionnedPath = jarFolder.toURI().toASCIIString();
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		for (Bundle bundle : context.getBundles()) {
			String location = bundle.getLocation();
			bundle.getSymbolicName();
			if(location.startsWith(unprovisionnedPath)){
				try {
					bundle.stop();
					bundle.uninstall();
				} catch (BundleException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "failed to unprovision "+bundle.getSymbolicName()+", "+e.getLocalizedMessage(), e));
				}
			}
		}
		return Status.OK_STATUS;
	}
}
