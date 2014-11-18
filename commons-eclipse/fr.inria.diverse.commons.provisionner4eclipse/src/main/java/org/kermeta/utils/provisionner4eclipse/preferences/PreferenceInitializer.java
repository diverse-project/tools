/*$Id$
* Project : org.kermeta.utils.provisionner.eclipse
* License : EPL
* Copyright : IRISA / INRIA / Universite de Rennes 1
* ----------------------------------------------------------------------------
* Creation date : 2010
* Authors : 
*			Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package org.kermeta.utils.provisionner4eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.kermeta.utils.provisionner4eclipse.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(PreferenceConstants.P_MVN_AETHER_OFFLINE,false);
		store.setDefault(PreferenceConstants.P_REPO_URL_LIST,
				"# Add here the Urls of the repository where maven should look for artifacts. \n"+				
				"http://maven.inria.fr/artifactory/public-release\n" +
				"http://maven.inria.fr/artifactory/public-snapshot\n"+
				"\n\n\n\n");
		store.setDefault(PreferenceConstants.P_BUNDLE_URI_LIST,
				"# Add here the URIs to the bundle \n"+
				"# you want to dynamically add to this platform\n"+
				"# It supports standard URI and maven PAX URL \n"+
				"# (see http://wiki.ops4j.org/display/paxurl/Documentation)\n" +
				"# Example URIs:\n" +
				"# mvn:org.kermeta.language/language.model\n"+
				"# file:///c:/users/yourlogin/your.jar\n"+
				"\n\n\n\n\n\n\n\n\n\n");
		
		
	}

}
