package org.kermeta.utils.provisionner4eclipse;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public class DynamicJarsUnprovisionnerJob extends Job {

	File jarFolder;
	
	/**
	 * 
	 * @param jobname
	 * @param jarFolder, might be a folder containing *.jar files or directly one jar file
	 */
	public DynamicJarsUnprovisionnerJob(String jobname, File jarFolder) {
		super(jobname);
		this.jarFolder = jarFolder;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return new Unprovisionner().unprovision(jarFolder, monitor);
	}

	
}
