/*$Id$
* Project : org.kermeta.utils.provisionner.eclipse
* License : EPL
* Copyright : IRISA / INRIA / Universite de Rennes 1
* ----------------------------------------------------------------------------
* Creation date : 2010
* Authors : 
*			Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package org.kermeta.utils.provisionner4eclipse;

import org.eclipse.ui.IStartup;

public class ProvisionnerEarlyStartup implements IStartup{

	
	public void earlyStartup() {
		// simply start this bundle
		Activator.getDefault();		
	}

}
