/* $Id: EcoreRegistering.java,v 1.3 2008-11-21 16:15:55 dvojtise Exp $ */
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

package org.eclipse.emf.ecoretools.registration;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;

public class EcoreRegistering {

	/**
	 * Register the contained EPackages by the given IFile
	 * Ignores EPackages for which the NsURI is already present in the registry
	 * @param ecoreFile
	 * @throws NotValidEPackageURIException 
	 */
	public static void registerPackages(IFile ecoreFile) throws NotValidEPackageURIException {
		String strURI = "platform:/resource" + ecoreFile.getFullPath().toString(); 
		URI mmURI = URI.createURI(strURI);
		registerPackages(mmURI);
	}
	
	/**
	 * Registers the given ecore file and all its contained EPackages
	 * Ignores EPackages for which the NsURI is already present in the registry
	 * @param pack
	 * @throws NotValidEPackageURIException 
	 */
	public static void registerPackages(URI ecoreFileUri) throws NotValidEPackageURIException {
		ResourceSet rs = new ResourceSetImpl();
		
		String ecore_ext = org.eclipse.emf.ecore.EcorePackage.eINSTANCE.getName().toLowerCase();
		if( !rs.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey(ecore_ext) ) {
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(ecore_ext, new XMIResourceFactoryImpl());
		}
		
		Resource res = rs.getResource(ecoreFileUri, true);
		
		for(EObject eobj : res.getContents()) {
			if( eobj instanceof EPackage) {
				registerPackages((EPackage) eobj);
			}
		}
	}

	/**
	 * Registers the given EPackage and all its contained EPackages
	 * Ignores EPackages for which the NsURI is already present in the registry
	 * @param pack
	 * @throws NotValidEPackageURIException 
	 */
	public static void registerPackages(EPackage pack) throws NotValidEPackageURIException {
		String pack_NsURI = pack.getNsURI();
		if( pack_NsURI != null && !pack_NsURI.equals("") ) {
			
			if( !Registry.INSTANCE.containsKey(pack_NsURI) ) {
				Registry.INSTANCE.put(pack_NsURI, pack);
			}

			for(EPackage subPack : pack.getESubpackages()) {
				registerPackages(subPack);
			}

		} else {
			throw new NotValidEPackageURIException(pack);
		}
	}

}


