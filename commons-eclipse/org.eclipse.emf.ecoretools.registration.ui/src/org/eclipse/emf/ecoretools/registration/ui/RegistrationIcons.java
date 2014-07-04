/* $Id: RegistrationIcons.java,v 1.1 2008-04-25 07:42:50 cfaucher Exp $ */
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
package org.eclipse.emf.ecoretools.registration.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * This class is used as an images registry.
 * It automatically loads some images in the initialize method and provides
 * a getImage method to access to image corresponding to the given path.
 * 
 */
public class RegistrationIcons {

	/**
	 * This class is a singleton.
	 */
	static private RegistrationIcons instance = null;
	
	/**
	 * We need to access to images in the plugin hierarchy. This URL will help us.
	 */
	static final private URL pluginURL = RegistrationUIPlugin.getDefault().getBundle().getEntry("/");
	
	/**
	 * This is the icons registry : a name corresponds to an image.
	 */
	private Hashtable<String, Image> icons = new Hashtable<String, Image>();
	
	/**
	 * A private constructor which just call an initialize method. 
	 *
	 */
	private RegistrationIcons() {
		initialize();
	}
	
	/**
	 * Loads some images whose name come from RegistrationConstants class.
	 *
	 */
	private void initialize() {
		try {
			Image icon_folder = ImageDescriptor.createFromURL(new URL(pluginURL, "/images/folder.gif")).createImage();			
			Image icon_project = ImageDescriptor.createFromURL(new URL(pluginURL, "/images/project.gif")).createImage();
			Image icon_ecoreModelFile = ImageDescriptor.createFromURL(new URL(pluginURL, "/images/EcoreModelFile.gif")).createImage();
			Image icon_generatedPackage = ImageDescriptor.createFromURL(new URL(pluginURL, "/images/generated_package.gif")).createImage();
			icons.put(RegistrationConstants.FOLDER, icon_folder); // instance
			icons.put(RegistrationConstants.PROJECT, icon_project);
			icons.put(RegistrationConstants.IMG_ECORE_FILE, icon_ecoreModelFile);
			icons.put(RegistrationConstants.IMG_GENERATED_PACKAGE, icon_generatedPackage);
		} catch (MalformedURLException e) { 
			e.printStackTrace(); 
		}	
	}
	
	/**
	 * Retreives an image for the given path
	 * @param path
	 * @return
	 */
	static public Image get(String path) {
		if ( instance == null )
			instance = new RegistrationIcons();
		return (Image)instance.icons.get(path);
	}
}
