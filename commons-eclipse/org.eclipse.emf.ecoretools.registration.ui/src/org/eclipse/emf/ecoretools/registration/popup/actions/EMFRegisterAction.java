/*$Id: EMFRegisterAction.java,v 1.1 2008-04-24 07:46:10 ftanguy Exp $ */
/* **********************************************************************
 * Copyright (c) 2007, 2008 INRIA and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    INRIA - initial API and implementation
 **********************************************************************/
package org.eclipse.emf.ecoretools.registration.popup.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecoretools.registration.RegistrationPlugin;
import org.eclipse.emf.ecoretools.registration.view.RegisteredPackageView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Action on the RegisteredPackageView that register a Ns uri 
 * and the corresponding children
 *
 */
public class EMFRegisterAction implements IObjectActionDelegate {
	
	protected StructuredSelection currentSelection;
    protected ArrayList<IFile> ecoreFiles; 

    protected ArrayList<IFile> umlFiles;
    
	/**
	 * Constructor
	 */
	public EMFRegisterAction() {
		ecoreFiles = new ArrayList<IFile>();
		umlFiles = new ArrayList<IFile>();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			// The selection could be a set of Ecore files
			currentSelection = (StructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator it = currentSelection.iterator();

			ecoreFiles.clear(); // remove the previous selection, else the old selected packages will be re-registered
			umlFiles.clear();
			while(it.hasNext()) {
				IFile file = (IFile) it.next();
				ecoreFiles.add( file );
				umlFiles.add( file );
			}
		}
	}
	
	/**
	 * refresh the view with the current content of the registry
	 */
	protected void displayRegisteredPackages() {
		System.out.println("Displaying registered packages ...");
		
		try {
			RegisteredPackageView view;
			view = (RegisteredPackageView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( RegisteredPackageView.ID );
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop(view);
			view.refresh();
		} catch (PartInitException e) {
			RegistrationPlugin.getDefault().getLog().log(new Status(Status.WARNING, "org.eclipse.emf.ecoretools.registration",
                    Status.OK, 
                    "not able to open Registered Package View : \""+RegisteredPackageView.ID+"\"", 
                    e));
		}
	}	
}
