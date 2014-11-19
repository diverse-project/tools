package org.kermeta.utils.provisionner4eclipse;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public class DynamicJarsProvisionnerJob extends Job {

	File jarFolder;
	
	/**
	 * 
	 * @param jobname
	 * @param jarFolder, might be a folder containing *.jar files or directly one jar file
	 */
	public DynamicJarsProvisionnerJob(String jobname, File jarFolder) {
		super(jobname);
		this.jarFolder = jarFolder;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Provisionner provisionner = new Provisionner();
		return provisionner.provision(provisionner.getJarsInFolder(jarFolder), new ArrayList<String>(), true, monitor);
	}

	
}
