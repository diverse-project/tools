/*$Id:  $
* License : EPL
* Copyright : IRISA / INRIA 
* ----------------------------------------------------------------------------
* Creation date : 26 sept. 2011
* Authors : 
*      Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.eclipse.uri;

import fr.inria.diverse.commons.eclipse.emf.EMFUriHelper;



/**
 * Simple URI to Physical converter : no conversion if unsure
 *
 */
public class SimpleLocalFileConverter extends LocalFileConverter {


	@Override
	public java.net.URI  convertSpecialURItoFileURI(java.net.URI javaUri) {
		if( EMFUriHelper.convertToEMFUri(javaUri).isFile())
			return javaUri;
		else
			return null;
	}

}
