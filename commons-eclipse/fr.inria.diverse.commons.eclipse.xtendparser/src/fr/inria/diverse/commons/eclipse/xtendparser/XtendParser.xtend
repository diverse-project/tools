package fr.inria.diverse.commons.eclipse.xtendparser

import com.google.inject.Inject
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IResourceVisitor
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtend.core.xtend.XtendFile
import org.eclipse.xtend.ide.internal.XtendActivator
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.common.types.access.TypeResource
import org.eclipse.xtext.ui.resource.IResourceSetProvider
import org.eclipse.xtext.xbase.resource.BatchLinkableResource

/**
 * Helper class to parse some Xtend project and to obtain an Xtend model.  
 * 
 * @author Erwan Bousse <erwan.bousse@irisa.fr>
 * @author Thomas Degueule <thomas.degueule@irisa.fr>
 */
public class XtendParser {

	/**
	 * Input: the eclipse java JDT  project
	 */
	private val IJavaProject javaProject

	/**
	 * Output: the set of XtendFile objects
	 */
	@Accessors(PUBLIC_GETTER, PROTECTED_SETTER)
	public val Set<XtendFile> xtendModel = new HashSet

	// Transient stuff
	@Inject
	private var IResourceSetProvider rsProvider
	private var ResourceSet rs
	private var boolean processed

	new(IJavaProject javaProject) {
		this.javaProject = javaProject
	}

	new(IProject javaProject) {
		this.javaProject = JavaCore::create(javaProject)
	}

	/**
	 * Parse the xtend files from the java project and produces the model.
	 * Once done, both "xtendModel" and "getJavaResource" are available.
	 */
	public def void process() {

		if (!processed) {
			processed = true;
			// We ask Xtend for an "IResourceSetProvider" object, which is injected in "rsProvider"
			val injector = XtendActivator::instance.getInjector("org.eclipse.xtend.core.Xtend")
			injector.injectMembers(this)

			// And we use the "IResourceSetProvider"  to create a resource set configured with the Java project containing the xtend sources, so that it has the complete classpath to parse xtend files  
			rs = rsProvider.get(javaProject.project)
			val Set<Resource> resources = new HashSet

			// Now we can find all .xtend files and load them as resources using the configured ResourceSet
			// We browse each configured src folder recursively
			val srcFolders = findSrcFoldersOf(javaProject)
			for (IFolder folder : srcFolders) {
				val Set<IFile> xtendfiles = new HashSet
				folder.accept(new IResourceVisitor() {
					override visit(IResource resource) throws CoreException {
						if (resource != null && resource instanceof IFile && resource.fileExtension.equals("xtend")) {
							xtendfiles.add(resource as IFile)
							return false
						} else {
							return true
						}
					}

				})

				// For each xtend file we find, we load it using the configured ResourceSet
				for (m : xtendfiles) {
					val URI uri = URI::createPlatformResourceURI(m.getFullPath().toString(), true)
					resources.add(rs.getResource(uri, true))
				}
			}

			// And we find the xtend roots, to return that
			for (Resource resource : resources) {
				if (resource instanceof BatchLinkableResource) {
					for (EObject o : resource.allContents.toSet) {
						if (o instanceof XtendFile) {
							xtendModel.add(o);
						}
					}
				}
			}
		}
	}

	/**
	 * Retrieves the TypeResource associated with a Java class that can be found in the classpath.
	 * e.g. to find the JvmAnnotationType corresponding to an annotation.
	 */
	public def TypeResource getJavaResource(String classFQN) {
		if (processed) {
			// We only resolve everything if we need to find a precise java resource 
			val Resource result = rs.getResource(URI.createURI("java:/Objects/" + classFQN), true)
			if (result != null && result instanceof TypeResource)
				return result as TypeResource
		}
		return null
	}

	private def static List<IFolder> findSrcFoldersOf(IJavaProject p) {
		val IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return findClassPathEntriesOf(p, IClasspathEntry.CPE_SOURCE).map[e|root.getFolder(e.path)]
	}

	private def static List<IClasspathEntry> findClassPathEntriesOf(IJavaProject p, int type) {

		// Finding the "src folder" in which to generate code
		val List<IClasspathEntry> res = new ArrayList();
		val IClasspathEntry[] entries = p.getResolvedClasspath(true);
		for (var int i = 0; i < entries.length; i++) {
			val IClasspathEntry entry = entries.get(i);
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				res.add(entry);
			}
		}
		return res
	}

}
