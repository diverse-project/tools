package fr.inria.diverse.commons.eclipse.pde.classpath;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.diverse.commons.eclipse.resources.IFileUtils;

public class BuildPropertiesHelper {
	/**
	 * Add a new source entry (for the main jar . ) to build.properties file
	 * @throws IOException 
	 */
	public static void addMainJarSourceEntry(IProject project, String newSourceEntry, IProgressMonitor monitor) throws CoreException, IOException {
		Properties properties = new Properties();
		IFile file = project.getFile("build.properties");
		if(file.exists()) {
			properties.load(file.getContents(true));
			String commaSeparatedPropvalues = properties.getProperty("source..");
			if(commaSeparatedPropvalues != null){
				// check if it is already there
				List<String> propValues = Arrays.asList(commaSeparatedPropvalues.split(","));
				for (String srcEntry : propValues) {
					if(srcEntry.trim().replaceAll("\\", "").equals(newSourceEntry)){
						// nothing to do, already there!
						return;
					}
				}
				// not found, so add it to the end
				String newcommaSeparatedPropvalues = commaSeparatedPropvalues + ",\\\n           "+newSourceEntry;
				properties.setProperty("source..", newcommaSeparatedPropvalues);
			} else {
				properties.setProperty("source..", newSourceEntry);
			}
		} else {
			properties.setProperty("source..", newSourceEntry);
		}
		StringWriter writer = new StringWriter();
		properties.store(writer, "");
		IFileUtils.writeInFile(file, writer.toString(), monitor);
	}
}
