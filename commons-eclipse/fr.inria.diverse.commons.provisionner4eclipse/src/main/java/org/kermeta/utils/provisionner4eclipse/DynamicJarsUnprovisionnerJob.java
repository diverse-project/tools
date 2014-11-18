package org.kermeta.utils.provisionner4eclipse;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

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
		/*ArrayList<String> bundleToInstall = new ArrayList<String>();
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
		}*/
		return new Unprovisionner().unprovision(jarFolder, monitor);
	}

	
}
