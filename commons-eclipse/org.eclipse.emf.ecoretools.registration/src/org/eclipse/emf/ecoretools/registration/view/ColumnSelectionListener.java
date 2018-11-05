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
 **********************************************************************/

package org.eclipse.emf.ecoretools.registration.view;

import org.eclipse.emf.ecoretools.registration.internal.RegisteredPackageComparator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Listener for the selection in the one of the column
 * Will assign the correct view comparator (ascending or descending)
 */
public class ColumnSelectionListener implements SelectionListener {

	private TreeViewer viewer = null;
	
	int column;

	/**
	 * Constructor
	 * 
	 * @param treeViewer 
	 */
	public ColumnSelectionListener(TreeViewer treeViewer, int column) {
		this.viewer = treeViewer;
		this.column =  column;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// Do nothing
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (viewer.getComparator() instanceof RegisteredPackageComparator) {
			RegisteredPackageComparator comparator = (RegisteredPackageComparator) viewer.getComparator();
			// if same column, switch order
			if (comparator.getColumn() == this.column) {
				if (comparator.getOrder() == RegisteredPackageComparator.ASCENDING_ORDER)
					comparator.setOrder(RegisteredPackageComparator.DESCENDING_ORDER);
				else if (comparator.getOrder() == RegisteredPackageComparator.DESCENDING_ORDER)
					comparator.setOrder(RegisteredPackageComparator.ASCENDING_ORDER);
			} else {
				// change column, but keep order
				comparator.setColumn(this.column);
			}
			viewer.refresh();
		} else {
			// assign default comparator if not set
			viewer.setComparator(new RegisteredPackageComparator(RegisteredPackageComparator.ASCENDING_ORDER, this.column));
		}
	}

}
