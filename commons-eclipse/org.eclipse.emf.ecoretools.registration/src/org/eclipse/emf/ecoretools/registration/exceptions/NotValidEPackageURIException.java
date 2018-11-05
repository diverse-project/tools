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

package org.eclipse.emf.ecoretools.registration.exceptions;

import org.eclipse.emf.ecore.EPackage;


/**
 * Exception thrown by register action due to missing nsURI 
 * It keeps track of the faulty EPackage in order to report the error to the end user
 *
 */
public class NotValidEPackageURIException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private EPackage ePackage;
	
	/**
	 * Constructor
	 * 
	 * @param the EPackage that cause the Exception
	 */
	public NotValidEPackageURIException(EPackage epack) {
		ePackage = epack;
	}

	/**
	 * Get the ePackage property
	 * 
	 * @return the ePackage property
	 */
	public EPackage getEPackage() {
		return ePackage;
	}
	
	/**
	 * @param package1 the EPackage to set as cause of the exception
	 */
	public void setEPackage(EPackage package1) {
		ePackage = package1;
	}
}


