package fr.inria.diverse.commons.eclipse.pde.classpath;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class ClasspathHelper {

	//JavaCore.newProjectEntry(_data.ecoreIFile.getProject().getFullPath())
	/**
	 * Add a new entry to the existing java project
	 * use JavaCore factroy to create the entry itself
	 * ex : JavaCore.newProjectEntry(ecoreIFile.getProject().getFullPath())
	 */
	public static void addEntry(IProject project, IClasspathEntry newEntry, IProgressMonitor monitor) throws CoreException{
		IJavaProject javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		ArrayList<IClasspathEntry> newClassPathArrayList = new ArrayList<IClasspathEntry>();
		IClasspathEntry[] previousentries = javaProject.getRawClasspath();
		for(IClasspathEntry previousentry : previousentries){
			newClassPathArrayList.add(previousentry);
		}
		// 
		newClassPathArrayList.add(newEntry);
		IClasspathEntry[] newClassPath = new IClasspathEntry[newClassPathArrayList.size()];
		javaProject.setRawClasspath(newClassPathArrayList.toArray(newClassPath), monitor);
	}
}
