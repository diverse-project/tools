/*$Id: PackageNsURIColumnSelectionListener.java,v 1.1 2008-04-24 07:46:09 ftanguy Exp $ */
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

import org.eclipse.emf.ecoretools.registration.internal.NsURIComparator;
import org.eclipse.emf.ecoretools.registration.internal.RegisteredPackageComparator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Listener for the selection in the Package NsURI column
 */
public class PackageNsURIColumnSelectionListener implements SelectionListener {

	private TreeViewer viewer = null;
	
	public PackageNsURIColumnSelectionListener(TreeViewer viewer) {
		this.viewer = viewer;
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		ViewerComparator newComparator = null;
		ViewerComparator comparator = viewer.getComparator();
		
		if ( comparator instanceof NsURIComparator ) {
			if ( ((NsURIComparator) comparator).getOrder() == RegisteredPackageComparator.ASCENDING_ORDER )
				newComparator = new NsURIComparator( RegisteredPackageComparator.DESCENDING_ORDER );
			else if ( ((NsURIComparator) comparator).getOrder() == RegisteredPackageComparator.DESCENDING_ORDER )
				newComparator = new NsURIComparator( RegisteredPackageComparator.ASCENDING_ORDER );
		} else
			newComparator = new NsURIComparator( RegisteredPackageComparator.ASCENDING_ORDER );
		
		if ( newComparator != null )
			viewer.setComparator( newComparator );
	}

}


