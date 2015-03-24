package org.eclipse.emf.ecoretools.registration.ui.builder;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecoretools.registration.EcoreRegistering;
import org.eclipse.emf.ecoretools.registration.EcoreUnregistering;
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;
import org.eclipse.emf.ecoretools.registration.view.RegisteredPackageView;
import org.eclipse.swt.widgets.Display;

public class EcoreAutoRegisterBuilder extends IncrementalProjectBuilder {
	
	class EcoreAutoRegisterDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				registerEcoreEPackages(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				registerEcoreEPackages(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class EcoreAutoRegisterResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			registerEcoreEPackages(resource);
			//return true to continue visiting children.
			return true;
		}
	}


	public static final String BUILDER_ID = "org.eclipse.emf.ecoretools.registration.ui.ecoreAutoRegisterBuilder";

	private static final String MARKER_TYPE = "org.eclipse.emf.ecoretools.registration.ui.ecoreAutoRegisterProblem";


	private void addMarker(IFile file, String message, int lineNumber,
			int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void registerEcoreEPackages(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".ecore")) {
			IFile ecoreFile = (IFile) resource;
			deleteMarkers(ecoreFile);
			try {
				EcoreUnregistering.unregisterPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {}
			try {
				EcoreRegistering.registerPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {
				addMarker(ecoreFile, "Cannot completly register EPackages from "+ecoreFile.getName()+" "+e.getMessage(), -1, IMarker.SEVERITY_WARNING);
			}
			if(Display.getDefault() != null){
				Display.getDefault().asyncExec(new Runnable(){
					public void run() {
						RegisteredPackageView.refreshViewIfActive();
					}});
			}
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new EcoreAutoRegisterResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new EcoreAutoRegisterDeltaVisitor());
		
		
	}
}
