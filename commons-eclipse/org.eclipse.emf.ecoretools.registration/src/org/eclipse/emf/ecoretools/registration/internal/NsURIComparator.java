/*$Id: NsURIComparator.java,v 1.1 2008-01-28 15:44:46 dvojtise Exp $ */
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

package org.eclipse.emf.ecoretools.registration.internal;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.jface.viewers.Viewer;

/**
 * Comparator for NsURI
 */
public class NsURIComparator extends RegisteredPackageComparator {
	
	/**
	 * Constructor
	 */
	public NsURIComparator(int order) {
		super(order);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		EPackage p1 = (EPackage) e1;
		EPackage p2 = (EPackage) e2;
		
		switch ( getOrder() ) {
		case ASCENDING_ORDER :
			return super.compare(viewer, p1.getNsURI(), p2.getNsURI());
		case DESCENDING_ORDER :
			return super.compare(viewer, p2.getNsURI(), p1.getNsURI());
		default:
			return 0;
		}
	}
	
}


