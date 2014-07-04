package org.eclipse.emf.ecoretools.registration.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RegistrationUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.emf.ecoretools.registration.ui";

	// The shared instance
	private static RegistrationUIPlugin plugin;
	
	
	private HashMap<String, String> ePackageNsURIPluginIDMapCache =  null;
	
	/**
	 * The constructor
	 */
	public RegistrationUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
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
	public static RegistrationUIPlugin getDefault() {
		return plugin;
	}

	
	private Map<String,String> getEPackageNsURIPluginIDMap(){
		if(ePackageNsURIPluginIDMapCache == null){
			ePackageNsURIPluginIDMapCache = new HashMap<String, String>();
		}
		return ePackageNsURIPluginIDMapCache;
	}
	
	public String getPluginID(String ePackageNsURI ){
		if(!getEPackageNsURIPluginIDMap().containsKey(ePackageNsURI)){
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource resource;
			URI urigenmodel = EcorePlugin.getEPackageNsURIToGenModelLocationMap().get(ePackageNsURI);
			registerExtension(ePackageNsURI,resourceSet);
			String plugin_id;
			try {
				resource = resourceSet.getResource(urigenmodel, true);
				GenModel genmodel = (GenModel) resource.getContents().get(0);				
				plugin_id = genmodel.getModelPluginID();
			} catch (Exception e) {
				plugin_id = "<genmodel = "+ urigenmodel +">";
			}
			
			
			
			getEPackageNsURIPluginIDMap().put(ePackageNsURI, plugin_id);
		}
		String res = getEPackageNsURIPluginIDMap().get(ePackageNsURI);
		if(res == null) res = "";
		return res;
	}
	private void registerExtension(String uri, ResourceSet resourceSet) {
		String[] fileNameSplit = uri.split("\\.");
		String extension = fileNameSplit[fileNameSplit.length - 1];
		if (extension.compareTo("uml") != 0) {
			resourceSet.getResourceFactoryRegistry()
					.getExtensionToFactoryMap().put(extension,
							new XMIResourceFactoryImpl());
		}

	}

	public void resetCache() {
		ePackageNsURIPluginIDMapCache = null;
	}
}
