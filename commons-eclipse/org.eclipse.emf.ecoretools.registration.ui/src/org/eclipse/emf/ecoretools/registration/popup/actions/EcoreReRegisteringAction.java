/* $Id: EcoreReRegisteringAction.java,v 1.2 2008-11-13 10:51:23 cfaucher Exp $ */
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

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecoretools.registration.EcoreRegistering;
import org.eclipse.emf.ecoretools.registration.EcoreUnregistering;
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * Action that unregisters and re-registers an Ecore file
 * It will register all the contained EPackages
 */
public class EcoreReRegisteringAction extends EMFRegisterAction {
	
	/**
	 * Constructor
	 */
	public EcoreReRegisteringAction() {
		super();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		// Unregistering phase
		for(IFile ecoreFile : ecoreFiles) {
			try {
				EcoreUnregistering.unregisterPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {
				Shell shell = new Shell();
				MessageDialog.openWarning(
					shell,
					"EPackage registration",
					"The EPackage: " + e.getEPackage().getName() + " cannot be unregistered, because its nsUri is not defined, all its subpackages have not been unregistered.");
			}
		}
		
		// Re-registering phase
		for(IFile ecoreFile : ecoreFiles) {	
			try {
				EcoreRegistering.registerPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {
				Shell shell = new Shell();
				MessageDialog.openWarning(
					shell,
					"EPackage registration",
					"The EPackage: " + e.getEPackage().getName() + " cannot be registered, because its nsUri is not defined, all its subpackages have not been registered.");
			}
		}
		
		displayRegisteredPackages();
	}
	
}
