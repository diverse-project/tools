/*$Id: PackageNameComparator.java,v 1.1 2008-01-28 15:44:46 dvojtise Exp $ */
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
 * Comparator for Package name
 *
 */
public class PackageNameComparator extends RegisteredPackageComparator {
	
	public PackageNameComparator(int order) {
		super(order);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		EPackage p1 = (EPackage) e1;
		EPackage p2 = (EPackage) e2;
		
		switch ( getOrder() ) {
		case ASCENDING_ORDER :
			return super.compare(viewer, p1.getName(), p2.getName());
		case DESCENDING_ORDER :
			return super.compare(viewer, p2.getName(), p1.getName());
		default:
			return 0;
		}
	}
	
}


