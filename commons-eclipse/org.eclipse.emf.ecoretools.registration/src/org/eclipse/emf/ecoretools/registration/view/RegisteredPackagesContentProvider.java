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
 * $Id: RegisteredPackagesContentProvider.java,v 1.3 2008/05/12 21:52:32 lbigearde Exp $
 **********************************************************************/

package org.eclipse.emf.ecoretools.registration.view;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecoretools.registration.EMFRegistryHelper;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * ContentProvider for the tree of registered packages
 */
public class RegisteredPackagesContentProvider implements ITreeContentProvider {

	
	/** Column nsURI */
	static public final int NSURI_COLUMN = 1;

	/** Column Package Name */
	static public final int PACKAGE_NAME_COLUMN = 1;

	/** Column Origin */
	static public final int ORIGIN_COLUMN = 1;

	/** Column Status */
	static public final int STATUS_COLUMN = 1;
	
	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Nothing to do
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// Nothing to do
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object arg0) {
		Object obj = Registry.INSTANCE.get(arg0);
		if (obj instanceof EPackage) {
			EPackage p = (EPackage) obj;
			Set<String> uris = EMFRegistryHelper.getRegisteredChildren(p.getNsURI());
			return uris.toArray();
		} else {
			return new Object[0];
		}
	}

	/**
	 * Tells if the given element has children
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object arg0) {
		Object obj = Registry.INSTANCE.get(arg0);
		if (obj instanceof EPackage) {
			EPackage p = (EPackage) obj;
			// a good guess about if we have children or not is if we have
			// subpackages
			// a better evaluation should get only valid children , ie. registered
			// children as in getChildren method
			return !p.getESubpackages().isEmpty();
		} else {
			return false;
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object arg0) {
		String result = null;
		Object obj = Registry.INSTANCE.get(arg0);
		if( obj instanceof EPackage) {
			EPackage p = (EPackage) obj;
			if ( (p.eContainer() != null) && (p.eContainer() instanceof EPackage)) {
				String nsURI = ((EPackage) p.eContainer()).getNsURI();
				if (EMFRegistryHelper.isRegistered(nsURI)) {
					result = nsURI;
				}
			}
		}
		return result;
	}

	/**
	 * Element for the Table from the registered instances
	 * returns only top level nsURIs as String (ie. do not consider nsURI for children EPackages) 
	 * If a nsURI is related to a resource that has not been loaded yet (lazy loading) it is considered as top
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		ArrayList<String> table = new ArrayList<String>();
		for (String uri : Registry.INSTANCE.keySet()) {
			Object obj = Registry.INSTANCE.get(uri);
			if (obj instanceof EPackage) {
				// add only root packages
				if (getParent(obj) == null) {
					table.add(uri);
				}
			} else {
				table.add(uri);
			}
		}	
		return table.toArray();
	}

}
