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
 * $Id: RegistrationPlugin.java,v 1.2 2008/04/28 15:47:42 jlescot Exp $
 **********************************************************************/
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
package org.eclipse.emf.ecoretools.registration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecoretools.registration.builder.EPackageAutoRegistrationBuilder;
import org.eclipse.emf.ecoretools.registration.builder.EPackageAutoRegistrationNature;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RegistrationPlugin extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.emf.ecoretools.registration"; //$NON-NLS-1$

	// The shared instance
	private static RegistrationPlugin plugin;

	/**
	 * The constructor
	 */
	public RegistrationPlugin() {
		// Do nothing
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		try{
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			for(IProject proj : workspace.getRoot().getProjects()){
				if(proj.isAccessible() && proj.getNature(EPackageAutoRegistrationNature.NATURE_ID) != null){
					proj.build(IncrementalProjectBuilder.FULL_BUILD, EPackageAutoRegistrationBuilder.BUILDER_ID, null, null);
				}
			}
		}catch(Exception e){}
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RegistrationPlugin getDefault() {
		return plugin;
	}
	

	private HashMap<String, String> ePackageNsURIPluginIDMapCache =  null;
	
	private Map<String,String> getEPackageNsURIPluginIDMap(){
		if(ePackageNsURIPluginIDMapCache == null){
			ePackageNsURIPluginIDMapCache = new HashMap<String, String>();
		}
		return ePackageNsURIPluginIDMapCache;
	}
	
	/**
	 * Search the pluginId of the plugin that deploys this NsUri
	 * Note: As this search implies to open and read the genmodel file, it uses a cache to speed up the queries 
	 * @param ePackageNsURI
	 * @return the pluginId of the plugin that deploys this NsUri
	 */
	public String getPluginID(String ePackageNsURI ){
		if(!getEPackageNsURIPluginIDMap().containsKey(ePackageNsURI)){
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(true));
			Resource resource;
			URI urigenmodel = EcorePlugin.getEPackageNsURIToGenModelLocationMap(false).get(ePackageNsURI);
			String plugin_id;
			try {
				resource = resourceSet.getResource(urigenmodel, true);
				GenModel genmodel = (GenModel) resource.getContents().get(0);				
				plugin_id = genmodel.getModelPluginID();
			} catch (Exception e) {
				plugin_id = NLS.bind(Messages.RegisteredPackageView_MissingInvalidGenmodel, urigenmodel);
			}
			
			getEPackageNsURIPluginIDMap().put(ePackageNsURI, plugin_id);
		}
		String res = getEPackageNsURIPluginIDMap().get(ePackageNsURI);
		if(res == null) res = "";
		return res;
	}

	/**
	 * Reset the cache used by getPluginID()
	 */
	public void resetCache() {
		ePackageNsURIPluginIDMapCache = null;
	}
}
