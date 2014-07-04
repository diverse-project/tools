/* $Id: EcoreUnregistering.java,v 1.2 2008-11-21 16:15:55 dvojtise Exp $ */
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
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;

public class EcoreUnregistering {

	/**
	 * Unregister the contained EPackages by the given IFile
	 * Security: It ignores request to unregister statically registered EPackages
	 * @param ecoreFile
	 * @throws NotValidEPackageURIException 
	 */
	public static void unregisterPackages(IFile ecoreFile) throws NotValidEPackageURIException {
		String strURI = "platform:/resource" + ecoreFile.getFullPath().toString(); 
		URI mmURI = URI.createURI(strURI);
		ResourceSet rs = new ResourceSetImpl();
		Resource res = rs.getResource(mmURI, true);

		for(EObject eobj : res.getContents()) {
			if( eobj instanceof EPackage) {
				unregisterPackages((EPackage) eobj);
			}
		}
	}

	/**
	 * Unregister the package and its nested packages
	 * Security: It ignores request to unregister statically registered EPackages
	 * @param pack
	 * @throws NotValidEPackageURIException 
	 */
	public static void unregisterPackages(EPackage pack) throws NotValidEPackageURIException {
		
		String pack_NsURI = pack.getNsURI();
		
		if( pack_NsURI != null && !pack_NsURI.equals("") ) {
			
			if(EMFRegistryHelper.isDynamicallyRegistered(pack_NsURI)) {
				for(EPackage subpack : pack.getESubpackages()) {
					unregisterPackages(subpack);
				}
				Registry.INSTANCE.remove(pack_NsURI);
			}
			
		} else {
			throw new NotValidEPackageURIException(pack);
		}
	}

}


