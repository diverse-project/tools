/*$Id: RegisteredPackageView.java,v 1.1 2008-04-24 07:46:09 ftanguy Exp $ */
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

import java.util.Iterator;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecoretools.registration.EMFRegistryHelper;
import org.eclipse.emf.ecoretools.registration.icons.ButtonIcons;
import org.eclipse.emf.ecoretools.registration.internal.NsURIComparator;
import org.eclipse.emf.ecoretools.registration.internal.RegisteredPackageComparator;
import org.eclipse.emf.ecoretools.registration.popup.actions.CopyNSURIAction;
import org.eclipse.emf.ecoretools.registration.popup.actions.EcoreUnregisterPackageAction;
import org.eclipse.emf.ecoretools.registration.ui.RegistrationUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

/**
 * View that displays the EMF registry and allows to unregistrer the EPackage 
 * that have been manually registered by the user
 *
 */
public class RegisteredPackageView extends ViewPart {

	/**
	 * Internal TreeViewer
	 */
	private TreeViewer viewer;
	
	// internal actions
	private EcoreUnregisterPackageAction unregisterPackageAction;
	private CopyNSURIAction copyNSURIAction;
	private Action refreshViewAction;
	
	Clipboard clipboard;
	
	public static final String ID = "org.eclipse.emf.ecoretools.registration.viewregisteredpackages";

	/**
	 * constructor
	 */
	public RegisteredPackageView() {
		super();
		setContentDescription("Registered packages:");
	}

	@Override
	public void createPartControl(Composite parent) {

		Display display =  PlatformUI.getWorkbench().getDisplay();
		clipboard = new Clipboard(display);
		
		viewer = new TreeViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider( new RegisteredPackagesContentProvider() );
		viewer.setLabelProvider( new RegisteredPackagesLabelProvider() );	
		viewer.setComparator( new NsURIComparator(RegisteredPackageComparator.ASCENDING_ORDER) );

		createActions();
		createContextMenu();
			
		createColumns(viewer);
		viewer.setColumnProperties( new String[] {"Registered NsURI", "EPackage name", "Origin", "Status"} );

		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(false);		

		viewer.setInput(getViewSite());
		
		// connect action keys
		// enable ctrl + C copy action
		IActionBars bars = getViewSite().getActionBars();
	    bars.setGlobalActionHandler(
	    		ActionFactory.COPY.getId(), 
	    		this.copyNSURIAction);
	    

		contributeToActionBars();
	         
	}

	/**
	 * used by createPartControl
	 * Creates the columns in the view
	 * @param viewer
	 */
	private void createColumns(TreeViewer viewer) {
		TreeColumn column1 = new TreeColumn(viewer.getTree(), SWT.LEFT);
		column1.setText("Registered URI");
		column1.setWidth(300);
		column1.setResizable(true);
		column1.addSelectionListener( new PackageNsURIColumnSelectionListener(viewer) );
		
		TreeColumn column2 = new TreeColumn(viewer.getTree(), SWT.LEFT);
		column2.setText("EPackage name");
		column2.setWidth(120);
		column2.setResizable(true);
		column2.addSelectionListener( new PackageNameColumnSelectionListener(viewer) );
		
		TreeColumn column3 = new TreeColumn(viewer.getTree(), SWT.LEFT);
		column3.setText("Origin");
		column3.setWidth(350);
		column3.setResizable(true);
		
		TreeColumn column4 = new TreeColumn(viewer.getTree(), SWT.LEFT);
		column4.setText("Status");
		column4.setWidth(350);
		column4.setResizable(true);
	}
	
	/**
	 * used by createPartControl
	 * Create the actions
	 *
	 */
	private void createActions() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ISharedImages platformImages = workbench.getSharedImages();
		unregisterPackageAction = new EcoreUnregisterPackageAction(this, "Unregister");
		unregisterPackageAction.setImageDescriptor(platformImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		unregisterPackageAction.setDisabledImageDescriptor(platformImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		unregisterPackageAction.setToolTipText("unregister the selected packages");
		copyNSURIAction  = new CopyNSURIAction(this, clipboard, "Copy NsURI");
		copyNSURIAction.setImageDescriptor(platformImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyNSURIAction.setToolTipText("Copy NsURI");
		
		refreshViewAction = new Action() {
			public void run() {				
				refresh();
			}
		};
		refreshViewAction.setText("Refresh view");
		refreshViewAction.setToolTipText("Refresh the view");
		refreshViewAction.setImageDescriptor(ButtonIcons.REFRESH_ICON);
		
	}
	/**
	 * used by createPartControl
	 * Create the contextMenu
	 *
	 */
	private void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				RegisteredPackageView.this.fillContextMenu(m);
			}
		});
		Menu menu =
			menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * used to create the context menu
	 * it is able to filter the actions corresponding to the selection ...
	 *
	 */
	private void fillContextMenu(IMenuManager menuMgr) {
		// filter only dynamically registered packages
		unregisterPackageAction.setEnabled(isSelectionDynamicallyRegistered((IStructuredSelection)viewer.getSelection()));
		menuMgr.add(unregisterPackageAction);
		menuMgr.add(new Separator(
				IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.add(copyNSURIAction);
		menuMgr.add(new Separator(
				IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	private void fillLocalPullDown(IMenuManager manager) {
	//	manager.add(refreshViewAction);
	//	manager.add(new Separator());
		//manager.add(action2);
	}
	
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshViewAction);
	//	manager.add(action2);
	}
	/**
	 * get the selection
	 * @return a table of the URI selected
	 */
	public EPackage[] getSelectedPackages() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		EPackage[] items = new EPackage[selection.size()];
		Iterator<?> iter = selection.iterator();
		int index = 0;
		while (iter.hasNext())
			items[index++] = (EPackage) iter.next();
		return items;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// nothing to do
		
	}

	/**
	 * rebuild the view, also refresh its content from the ContentProvider
	 *
	 */
	public void refresh(){
		RegistrationUIPlugin.getDefault().resetCache();
		viewer.refresh();
	}
	
	/**
	 * checks if the selection is dynamically registered or not
	 * @param selection
	 * @return
	 */
	private boolean isSelectionDynamicallyRegistered(IStructuredSelection selection){
		boolean result = true;
		
		if( selection.isEmpty() ) 
			return false;
		
		Iterator<?> iter = selection.iterator();
		while (iter.hasNext()){
			EPackage p = (EPackage) iter.next();
			if( ! EMFRegistryHelper.isDynamicallyRegistered(p.getNsURI()) ){
				return false;
			}
		}
		return result;
	}
}
