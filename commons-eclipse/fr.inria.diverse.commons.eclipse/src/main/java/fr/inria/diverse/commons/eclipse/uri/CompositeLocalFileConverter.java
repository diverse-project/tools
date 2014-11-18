/*$Id:  $
* License : EPL
* Copyright : IRISA / INRIA 
* ----------------------------------------------------------------------------
* Creation date : 26 sept. 2011
* Authors : 
*      Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.eclipse.uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * allow to combine the use of several LocalFileConverter 
 *
 */
public class CompositeLocalFileConverter extends LocalFileConverter {

	protected java.util.List<LocalFileConverter> innerLocalFileConverters;
	
	public CompositeLocalFileConverter(java.util.List<LocalFileConverter> innerLocalFileConverters){
		this.innerLocalFileConverters = new java.util.ArrayList<LocalFileConverter>();
		this.innerLocalFileConverters.addAll(innerLocalFileConverters);
	}
	public void add(LocalFileConverter innerLocalFileConverter){
		innerLocalFileConverters.add(innerLocalFileConverter);
	}
	
	@Override
	public java.net.URI convertSpecialURItoFileURI(java.net.URI javaUri) {
		try {
			URL javaUrl = javaUri.toURL();
			if(javaUrl.getProtocol().equals("file")){
				// already a file, nothing to do
				return javaUri;
			}
			for(LocalFileConverter innerLocalFileConverter: innerLocalFileConverters){
				URI result = innerLocalFileConverter.convertSpecialURItoFileURI(javaUri);
				if(result != null) return result;
			}
			
			
		} catch (MalformedURLException e) {
			return null;
		}
		
		return null;
	}

}
