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
 * $Id: RegisteredPackagesLabelProvider.java,v 1.3 2008/05/12 21:52:48 lbigearde Exp $
 **********************************************************************/

package org.eclipse.emf.ecoretools.registration.view;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecoretools.registration.EMFRegistryHelper;
import org.eclipse.emf.ecoretools.registration.Messages;
import org.eclipse.emf.ecoretools.registration.RegistrationPlugin;
import org.eclipse.emf.ecoretools.registration.internal.RegistrationConstants;
import org.eclipse.emf.ecoretools.registration.internal.RegistrationIcons;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

/**
 * LabelProvider for the registered packages
 */
public class RegisteredPackagesLabelProvider extends LabelProvider implements ITableLabelProvider {

	/** Column nsURI */
	static public final int NSURI_COLUMN = 0;

	/** Column Package Name */
	static public final int PACKAGE_NAME_COLUMN = 1;

	/** Column Origin */
	static public final int ORIGIN_COLUMN = 2;

	/** Column Status */
	static public final int STATUS_COLUMN = 3;
	
	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText(Object obj, int index) {
		String nsURI = (String)obj;
		Object registeredObj = Registry.INSTANCE.get(nsURI);
		EPackage registeredPackage = null;
		if (registeredObj instanceof EPackage) {
			registeredPackage = (EPackage) registeredObj;
		}
		switch (index) {
		case NSURI_COLUMN: // URI column
			return nsURI;

		case PACKAGE_NAME_COLUMN: // Package name column
			if(registeredPackage != null) 
				return registeredPackage.getName();
			else
				return Messages.RegisteredPackagesLabelProvider_Unknown;
		case ORIGIN_COLUMN: // origin column
			if (obj != null) {
				if (EMFRegistryHelper.isDynamicallyRegistered( nsURI ) && registeredPackage != null)
					// if this is exactly an EPackage then this comes from a 
					// file, otherwise it comes from generated java
					return "" + getText(registeredPackage.eResource().getURI()); //$NON-NLS-1$
				else
					return "" + EcorePlugin.getEPackageNsURIToGenModelLocationMap(false).get(nsURI); //$NON-NLS-1$
			}
		case STATUS_COLUMN: //status column
			if (obj != null) {
				if (EMFRegistryHelper.isDynamicallyRegistered(nsURI)) {
					return  Messages.RegisteredPackagesLabelProvider_DynamicallyRegistered;
				} else if (EMFRegistryHelper.isRegisteredAndLoaded(nsURI)) {
					return NLS.bind(Messages.RegisteredPackagesLabelProvider_RegisteredGeneratedJavaFromPlugin, 
							RegistrationPlugin.getDefault().getPluginID(nsURI));
				} else {
					return NLS.bind(Messages.RegisteredPackagesLabelProvider_NotLoadedGeneratedJavaFromPlugin, 
							RegistrationPlugin.getDefault().getPluginID(nsURI));
				}
			}
			return ""; //$NON-NLS-1$
		default:
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(Object obj, int index) {
		if ((index == ORIGIN_COLUMN)) {
			String nsURI = (String) obj;
			if (EMFRegistryHelper.isDynamicallyRegistered(nsURI)) {
				return RegistrationIcons.get(RegistrationConstants.IMG_ECORE_FILE);
			} else if (EMFRegistryHelper.isRegisteredAndLoaded(nsURI)) {
				return RegistrationIcons.get(RegistrationConstants.IMG_GENERATED_PACKAGE);
			} else {
				return RegistrationIcons.get(RegistrationConstants.IMG_LAZY_GENERATED_PACKAGE);
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object obj) {
		return null;
	}
}
