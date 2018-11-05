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
 * $Id: EcoreRegisteringAction.java,v 1.3 2008/05/12 21:50:25 lbigearde Exp $
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
 * Action that register an ecore file. It will register all the contained
 * EPackages
 */
public class EcoreRegisteringAction extends EMFRegisterAction {

	/**
	 * Constructor
	 */
	public EcoreRegisteringAction() {
		super();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		for(IFile ecoreFile : ecoreFiles) {	
			try {
				EcoreRegisteringHelper.registerPackages(ecoreFile);
			} catch (NotValidEPackageURIException e) {
				MessageDialog.openWarning(
					null,
					Messages.EcoreRegisteringAction_EPackageRegistration,
					NLS.bind(Messages.EcoreRegisteringAction_CanNotBeRegistered, e.getEPackage().getName()));
			}
		}

		displayRegisteredPackages();
	}

}
