/***********************************************************************
 * Copyright (c) 2007, 2018 INRIA and others
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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;

public class EcoreRegisteringHelper {

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
	
	/**
	 * Unregister the contained EPackages by the given IFile
	 * Security: It ignores request to unregister statically registered EPackages
	 * In case of change on the IFile, it also unregisters all previously registered packages that comes from the given file 
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
		
		// search for previously registered EPAckage that comes from a previous version of this file. ie case of a change on the nsURI
		Collection<Object> packages =  new ArrayList<Object>();
		packages.addAll(Registry.INSTANCE.values());
		for(Object pack : packages){
			if(pack instanceof EPackage){
				EPackage p = ((EPackage) pack);
				if(p.eResource() != null && p.eResource().getURI().equals(res.getURI())){
					try{
						unregisterPackages(p);
					} catch (NotValidEPackageURIException e){
						// ignore because may have been unregistered by recursion
					}
				}
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


