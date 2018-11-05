/***********************************************************************
 * Copyright (c) 2007, 2008 INRIA and others
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    INRIA - initial API and implementation
 *
 * $Id: EcoreUnregisteringAction.java,v 1.3 2008/05/12 21:50:50 lbigearde Exp $
 **********************************************************************/
package org.eclipse.emf.ecoretools.registration.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecoretools.registration.EcoreRegisteringHelper;
import org.eclipse.emf.ecoretools.registration.Messages;
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

/**
 * Action that unregister an ecore file. It will unregister all the contained
 * packages
 */
public class EcoreUnregisteringAction extends EMFRegisterAction {

	/**
	 * Constructor
	 */
	public EcoreUnregisteringAction() {
		super();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		for(IFile ecoreFile : ecoreFiles) {
			try {
				EcoreRegisteringHelper.unregisterPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {
				MessageDialog.openWarning(
					null,
					Messages.EcoreUnregisteringAction_EPackageRegistration,
					NLS.bind(Messages.EcoreUnregisteringAction_CanNotBeUnregistered, e.getEPackage().getName()));
			}
		}

		displayRegisteredPackages();

	}


}
