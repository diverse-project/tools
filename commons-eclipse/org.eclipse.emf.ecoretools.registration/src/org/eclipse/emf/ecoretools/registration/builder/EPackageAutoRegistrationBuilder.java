/***********************************************************************
 * Copyright (c) 2007, 2018 INRIA and others
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    INRIA - initial API and implementation
 *
 **********************************************************************/
package org.eclipse.emf.ecoretools.registration.builder;

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
import org.eclipse.emf.ecoretools.registration.EcoreRegisteringHelper;
import org.eclipse.emf.ecoretools.registration.Messages;
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;
import org.eclipse.emf.ecoretools.registration.view.RegisteredPackageView;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class EPackageAutoRegistrationBuilder extends IncrementalProjectBuilder {

	class EPackageAutoRegistrationDeltaVisitor implements IResourceDeltaVisitor {
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


	public static final String BUILDER_ID = "org.eclipse.emf.ecoretools.registration.EPackageAutoRegistrationBuilder";

	private static final String MARKER_TYPE = "org.eclipse.emf.ecoretools.registration.ecoreAutoRegisterProblem";


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

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
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
				EcoreRegisteringHelper.unregisterPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {}
			try {
				EcoreRegisteringHelper.registerPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {
				addMarker(ecoreFile, 
						NLS.bind(Messages.EcoreRegisteringBuilder_CanNotBeRegistered, ecoreFile.getName(), e.getEPackage().getName()), 
						-1, 
						IMarker.SEVERITY_WARNING);
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
		delta.accept(new EPackageAutoRegistrationDeltaVisitor());
		
		
	}
}
