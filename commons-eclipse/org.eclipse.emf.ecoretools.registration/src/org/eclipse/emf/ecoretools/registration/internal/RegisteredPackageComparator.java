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
 *
 * $Id: RegisteredPackageComparator.java,v 1.3 2008/05/12 21:49:28 lbigearde Exp $
 **********************************************************************/

package org.eclipse.emf.ecoretools.registration.internal;

import org.eclipse.emf.ecoretools.registration.view.RegisteredPackagesLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Comparator for Registered package
 * Uses the LabelProvider for the indicated column
 */
public class RegisteredPackageComparator extends ViewerComparator {

	/** Ascending order */
	static public final int ASCENDING_ORDER = 0;

	/** Descending order */
	static public final int DESCENDING_ORDER = 1;

	private int order;

	private int column;
	
	RegisteredPackagesLabelProvider labelProvider = new RegisteredPackagesLabelProvider();
	
	/**
	 * Constructor
	 * 
	 * @param order the default order (ascending or descending)
	 */
	public RegisteredPackageComparator(int order, int column) {
		this.order = order;
		this.column = column;
	}

	/**
	 * Get the order property
	 * 
	 * @return the order property
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	
	/**
	 * Get the column property
	 * 
	 * @return the column property
	 */
	public int getColumn() {
		return column;
	}
	
	/**
	 * @param column the column to set
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		String p1ColumnVal = labelProvider.getColumnText(e1, column);
		String p2ColumnVal = labelProvider.getColumnText(e2, column);
		
		switch (getOrder()) {
			case ASCENDING_ORDER:
				return super.compare(viewer, p1ColumnVal, p2ColumnVal);
			case DESCENDING_ORDER:
				return super.compare(viewer, p2ColumnVal, p1ColumnVal);
			default:
				return 0;
		}
	}



}
