/***********************************************************************
 * Copyright (c) 2007, 2008 INRIA and others
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    INRIA - initial API and implementation
 *
 * $Id: RegisteredPackageView.java,v 1.3 2008/05/12 21:53:04 lbigearde Exp $
 **********************************************************************/

package org.eclipse.emf.ecoretools.registration.view;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecoretools.registration.EMFRegistryHelper;
import org.eclipse.emf.ecoretools.registration.Messages;
import org.eclipse.emf.ecoretools.registration.RegistrationPlugin;
import org.eclipse.emf.ecoretools.registration.internal.RegisteredPackageComparator;
import org.eclipse.emf.ecoretools.registration.internal.RegistrationIcons;
import org.eclipse.emf.ecoretools.registration.popup.actions.CopyNSURIAction;
import org.eclipse.emf.ecoretools.registration.popup.actions.EcoreUnregisterPackageAction;
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
 * View that displays the EMF registry and allows to unregister the EPackage
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
	private Action loadLazyEPackagesAction;
	
	Clipboard clipboard;

	/**
	 * View ID
	 */
	public static final String ID = "org.eclipse.emf.ecoretools.registration.viewregisteredpackages"; //$NON-NLS-1$

	/**
	 * constructor
	 */
	public RegisteredPackageView() {
		super();
		setContentDescription(Messages.RegisteredPackageView_RegisteredPackages);
	}

	@Override
	public void createPartControl(Composite parent) {

		Display display = PlatformUI.getWorkbench().getDisplay();
		clipboard = new Clipboard(display);

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RegisteredPackagesContentProvider());
		viewer.setLabelProvider(new RegisteredPackagesLabelProvider());
		viewer.setComparator(new RegisteredPackageComparator(RegisteredPackageComparator.ASCENDING_ORDER, 
				RegisteredPackagesLabelProvider.NSURI_COLUMN));

		createActions();
		createContextMenu();

		createColumns(viewer);
		viewer.setColumnProperties(new String[] { Messages.RegisteredPackageView_RegisteredURI, Messages.RegisteredPackageView_PackageName, Messages.RegisteredPackageView_Origin, Messages.RegisteredPackageView_Status });

		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(false);

		viewer.setInput(getViewSite());

		// connect action keys
		// enable ctrl + C copy action
		IActionBars bars = getViewSite().getActionBars();
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), this.copyNSURIAction);

		contributeToActionBars();
	         
	}

	/**
	 * used by createPartControl Creates the columns in the view
	 * 
	 * @param treeViewer
	 */
	private void createColumns(TreeViewer treeViewer) {
		TreeColumn column1 = new TreeColumn(treeViewer.getTree(), SWT.LEFT);
		column1.setText(Messages.RegisteredPackageView_RegisteredURI);
		column1.setWidth(300);
		column1.setResizable(true);
		column1.addSelectionListener(new ColumnSelectionListener(treeViewer, RegisteredPackagesLabelProvider.NSURI_COLUMN));

		TreeColumn column2 = new TreeColumn(treeViewer.getTree(), SWT.LEFT);
		column2.setText(Messages.RegisteredPackageView_PackageName);
		column2.setWidth(120);
		column2.setResizable(true);
		column2.addSelectionListener(new ColumnSelectionListener(treeViewer, RegisteredPackagesLabelProvider.PACKAGE_NAME_COLUMN));

		TreeColumn column3 = new TreeColumn(treeViewer.getTree(), SWT.LEFT);
		column3.setText(Messages.RegisteredPackageView_Origin);
		column3.setWidth(350);
		column3.setResizable(true);
		column3.addSelectionListener(new ColumnSelectionListener(treeViewer, RegisteredPackagesLabelProvider.ORIGIN_COLUMN));
		
		TreeColumn column4 = new TreeColumn(viewer.getTree(), SWT.LEFT);
		column4.setText(Messages.RegisteredPackageView_Status);
		column4.setWidth(350);
		column4.setResizable(true);
		column4.addSelectionListener(new ColumnSelectionListener(treeViewer, RegisteredPackagesLabelProvider.STATUS_COLUMN));
	}

	/**
	 * used by createPartControl Create the actions
	 * 
	 */
	private void createActions() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ISharedImages platformImages = workbench.getSharedImages();
		unregisterPackageAction = new EcoreUnregisterPackageAction(this, Messages.RegisteredPackageView_Unregister);
		unregisterPackageAction.setImageDescriptor(platformImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		unregisterPackageAction.setDisabledImageDescriptor(platformImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		unregisterPackageAction.setToolTipText(Messages.RegisteredPackageView_UnregisterSelectedPackages);
		copyNSURIAction = new CopyNSURIAction(this, clipboard, Messages.RegisteredPackageView_CopyNsURI);
		copyNSURIAction.setImageDescriptor(platformImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyNSURIAction.setToolTipText(Messages.RegisteredPackageView_CopyNsURI_tooltip);
		refreshViewAction = new Action() {
			public void run() {				
				refresh();
			}
		};
		refreshViewAction.setText(Messages.RegisteredPackageView_RefreshView);
		refreshViewAction.setToolTipText(Messages.RegisteredPackageView_RefreshView_tooltip);
		refreshViewAction.setImageDescriptor(RegistrationIcons.REFRESH_ICON_DESCRIPTOR);
		loadLazyEPackagesAction = new Action() {
			public void run() {	
				Map<String, URI> map = EcorePlugin
						.getEPackageNsURIToGenModelLocationMap(true);
				map.keySet().forEach(uriKey -> {
					ResourceSetImpl resourceSet = new ResourceSetImpl();
					resourceSet.getPackageRegistry().getEPackage(uriKey);
				});
				refresh();
			}
		};
		loadLazyEPackagesAction.setText(Messages.RegisteredPackageView_LoadLazyEpackages);
		loadLazyEPackagesAction.setToolTipText(Messages.RegisteredPackageView_LoadLazyEpackages_tooltip);
		loadLazyEPackagesAction.setImageDescriptor(RegistrationIcons.LOAD_LAZY_EPACKAGES_ICON_DESCRIPTOR);
	}

	/**
	 * used by createPartControl Create the contextMenu
	 * 
	 */
	private void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager m) {
				RegisteredPackageView.this.fillContextMenu(m);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * used to create the context menu it is able to filter the actions
	 * corresponding to the selection ...
	 * 
	 * @param menuMgr
	 */
	private void fillContextMenu(IMenuManager menuMgr) {
		// filter only dynamically registered packages
		unregisterPackageAction.setEnabled(isSelectionDynamicallyRegistered((IStructuredSelection) viewer.getSelection()));
		menuMgr.add(unregisterPackageAction);
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.add(copyNSURIAction);
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(loadLazyEPackagesAction);
		manager.add(refreshViewAction);
	}
	
	/**
	 * get the selection
	 * 
	 * @return a table of the URI selected
	 */
	public String[] getSelectedPackages() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		String[] items = new String[selection.size()];
		Iterator<?> iter = selection.iterator();
		int index = 0;
		while (iter.hasNext()) {
			items[index++] = (String) iter.next();
		}
		return items;
	}

	/**
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
	public void refresh() {
		RegistrationPlugin.getDefault().resetCache();
		viewer.refresh();
	}

	/**
	 * Checks if the selection is dynamically registered or not
	 * 
	 * @param selection
	 * @return true whether the selection is dynamically registered
	 */
	private boolean isSelectionDynamicallyRegistered(IStructuredSelection selection) {
		boolean result = true;

		if (selection.isEmpty()) {
			return false;
		}

		Iterator<?> iter = selection.iterator();
		while (iter.hasNext()) {
			String nsURI = (String) iter.next();
			if (!EMFRegistryHelper.isDynamicallyRegistered(nsURI)) {
				return false;
			}
		}
		return result;
	}
	
	public static void refreshViewIfActive(){
		RegisteredPackageView view;
		view = (RegisteredPackageView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(RegisteredPackageView.ID);
		if(view != null) {
			view.refresh();
		}
	}
}
