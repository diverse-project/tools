/*$Id: RegisteredPackagesContentProvider.java,v 1.1 2008-04-24 07:46:09 ftanguy Exp $*/
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecoretools.registration.EMFRegistryHelper;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * ContentProvider for the tree of registered packages
 */
public class RegisteredPackagesContentProvider implements ITreeContentProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Nothing to do

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// Nothing to do

	}

	/**
	 * returns the list of children of this element
	 */
	public Object[] getChildren(Object arg0) {
		EPackage p = (EPackage) arg0;
		Set<String> uris = EMFRegistryHelper
				.getRegisteredChildren(p.getNsURI());
		List<EPackage> children = new ArrayList<EPackage>();
		for (String s : uris) {
			Object o = Registry.INSTANCE.getEPackage(s);
			if (o instanceof EPackage)
				children.add((EPackage) o);
		}
		return children.toArray();
	}

	/**
	 * tells if the given element has children
	 */
	public boolean hasChildren(Object arg0) {
		EPackage p = (EPackage) arg0;
		// a good guess about if we have children or not is if we have
		// subpackages
		// a better evaluation should get only valid children , ie. registered
		// children as in getChildren method
		return !p.getESubpackages().isEmpty();
	}

	public Object getParent(Object arg0) {
		EPackage result = null;
		
		EPackage p = (EPackage) arg0;
		if ( (p.eContainer() != null) && (p.eContainer() instanceof EPackage)) {
			String nsURI = ((EPackage) p.eContainer()).getNsURI();
			if (EMFRegistryHelper.isRegistered(nsURI)) {
				result = (EPackage) p.eContainer();
			}
		}
		return result;
	}

	/**
	 * element for the Table from the registered instances
	 */
	public Object[] getElements(Object parent) {
		ArrayList<EPackage> table = new ArrayList<EPackage>();
		for (String uri : Registry.INSTANCE.keySet()) {
			Object obj = Registry.INSTANCE.get(uri);
			if (obj instanceof EPackage) {
				// EPackage p = (EPackage) obj;
				// add only root packages
				if (getParent(obj) == null)
					table.add((EPackage) obj);
			}
		}
		Map<String, URI> map = EcorePlugin
				.getEPackageNsURIToGenModelLocationMap();
		Iterator<String> iter2 = map.keySet().iterator();
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		while (iter2.hasNext()) {

			String uriKey = iter2.next();
			try {

				EPackage epackageObject = resourceSet.getPackageRegistry()
						.getEPackage(uriKey);
				if (getParent(epackageObject) == null) {
					if (!table.contains(epackageObject))
						table.add(epackageObject);
				}
			} catch (Exception e) {
				
				System.err.println(e.getCause());
			}
		}
		return table.toArray();
	}

}
