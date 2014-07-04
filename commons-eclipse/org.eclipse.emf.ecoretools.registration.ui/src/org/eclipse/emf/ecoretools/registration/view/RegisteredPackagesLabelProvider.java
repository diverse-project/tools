/*$Id: RegisteredPackagesLabelProvider.java,v 1.2 2008-04-25 07:42:50 cfaucher Exp $ */
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

package org.eclipse.emf.ecoretools.registration.view;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecoretools.registration.EMFRegistryHelper;
import org.eclipse.emf.ecoretools.registration.ui.RegistrationConstants;
import org.eclipse.emf.ecoretools.registration.ui.RegistrationIcons;
import org.eclipse.emf.ecoretools.registration.ui.RegistrationUIPlugin;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * LabelProvider for the registered packages
 */
public class RegisteredPackagesLabelProvider extends LabelProvider implements ITableLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object obj, int index) {
		EPackage p = (EPackage) obj;
		switch (index) {
		case 0: // URI column
			return p.getNsURI();

		case 1: // Package name column
			return p.getName();
		case 2: // origin column
			if(obj != null){
				if(EMFRegistryHelper.isDynamicallyRegistered( p.getNsURI() ) )
				// if this is exactly an EPackage then this comes from a files, otherwise it comes from generated java
					return getText(p.eResource().getURI());
				else {
					return RegistrationUIPlugin.getDefault().getPluginID(p.getNsURI());
				}
			}
			return "";
		case 3: //status column
			if(obj != null){
				if(EMFRegistryHelper.isDynamicallyRegistered( p.getNsURI() ) )
				// if this is exactly an EPackage then this comes from a files, otherwise it comes from generated java
					return "dynamically registered";
				else if(EMFRegistryHelper.isRegistered(p.getNsURI())){
					return "registered from a plugin";
				}
				else {
					return "installed from a plugin but not registered";
				}
			}
			return "";
		default:
			return "";

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object obj, int index) {
		if ((index == 2)){				
			return getImage( obj );

		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object obj) {
		if(obj.getClass() == EPackageImpl.class){ // if this is exactly an EPackage then this comes from a files, otherwise it comes from generated java
			return RegistrationIcons.get(RegistrationConstants.IMG_ECORE_FILE);
		}
		else return RegistrationIcons.get(RegistrationConstants.IMG_GENERATED_PACKAGE); 
	}
}


