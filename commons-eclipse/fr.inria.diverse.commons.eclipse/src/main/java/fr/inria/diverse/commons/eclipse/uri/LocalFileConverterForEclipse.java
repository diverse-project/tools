/*$Id:  $
* License : EPL
* Copyright : IRISA / INRIA 
* ----------------------------------------------------------------------------
* Creation date : 26 sept. 2011
* Authors : 
*      Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.eclipse.uri;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;

import fr.inria.diverse.commons.eclipse.emf.EMFUriHelper;

public class LocalFileConverterForEclipse extends LocalFileConverter {

	public LocalFileConverterForEclipse(){
		
	}
	
	/**
	 * be careful java.net.URI uses RFC 2396 compliant string, which means that " " are enforced into "%20" for example
	 * return null if it cannot be converted
	 */
	@Override
	public java.net.URI convertSpecialURItoFileURI(java.net.URI javaUri) {
		org.eclipse.emf.common.util.URI emfUri = EMFUriHelper.convertToEMFUri(javaUri);
		if(emfUri.isFile()){
			// already a file, nothing to do
			return javaUri;
		}
		if (emfUri.isPlatformResource()) {
			String platformString = emfUri.toPlatformString(true);
			IResource res =ResourcesPlugin.getWorkspace().getRoot().findMember(platformString); 
			if(res != null){
				if(res.getRawLocationURI()!=null)
					return java.net.URI.create(encodeSpecialChars(res.getRawLocationURI().toString()));
				else if(res.getLocationURI()!=null)						
					return java.net.URI.create(encodeSpecialChars(res.getLocationURI().toString()));
				
			}
		}
		// deal with platformPlugin
		if(emfUri.isPlatformPlugin()){
			URL resolvedURL;
			try {
				resolvedURL = FileLocator.resolve(javaUri.toURL()); 
				return java.net.URI.create(encodeSpecialChars(resolvedURL.toString()));
			} catch (MalformedURLException e) {
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		return null;
	}
	public String encodeSpecialChars(String s){
		// java.net.URLEncoder.encode(resolvedURL.toString(),"UTF-8") doesn't work properly because it also encodes the : or ! of the jar:file:/...!   urls
		// let's go back to simpler encoding 
		return s.toString().replaceAll(" ","%20");
	}
}
