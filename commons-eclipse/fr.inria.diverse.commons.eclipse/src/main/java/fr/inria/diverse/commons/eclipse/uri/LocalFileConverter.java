/*$Id:  $
* License : EPL
* Copyright : IRISA / INRIA 
* ----------------------------------------------------------------------------
* Creation date : 26 sept. 2011
* Authors : 
*      Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.eclipse.uri;

import java.io.File;

import org.eclipse.emf.common.util.URI;

import fr.inria.diverse.commons.eclipse.emf.EMFUriHelper;


/**
 * Abstract service : retrieve a local file (or uri of file) from various sources
 */
public abstract class LocalFileConverter {
	/**
	 * Returns the URI as a local file, or null if the given URI does not represent a local file.
	 * @param uri
	 * @return
	 */
	public File convertSpecialURItoFile(java.net.URI uri){
		URI fileUri = EMFUriHelper.convertToEMFUri(convertSpecialURItoFileURI( uri));
		if(fileUri == null) return null;
		
		return new File(fileUri.toFileString());
	}
	
	public abstract java.net.URI convertSpecialURItoFileURI(java.net.URI uri);
}
