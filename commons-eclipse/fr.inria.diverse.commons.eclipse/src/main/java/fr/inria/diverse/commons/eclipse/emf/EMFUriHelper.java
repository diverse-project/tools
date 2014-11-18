/*$Id:  $
* License : EPL
* Copyright : IRISA / INRIA 
* ----------------------------------------------------------------------------
* Creation date : 27 sept. 2011
* Authors : 
*      Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.eclipse.emf;

import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

public class EMFUriHelper {

		public static org.eclipse.emf.common.util.URI convertToEMFUri(java.net.URI javaUri){
			// current implementation is trivial, but may be enhanced if necessary
			return org.eclipse.emf.common.util.URI.createURI(javaUri.toString());
		}
		public static java.net.URI convertToJavaUri(org.eclipse.emf.common.util.URI emfUri){
			// current implementation is trivial, but may be enhanced if necessary
			return java.net.URI.create(emfUri.toString());
		}
		
		
		public static org.eclipse.emf.common.util.URI normalize(org.eclipse.emf.common.util.URI emfUri){
			
			org.eclipse.emf.common.util.URI normalizedUri = CommonPlugin.resolve(new ExtensibleURIConverterImpl().normalize(emfUri));
			if(emfUri.isPlatformResource() && normalizedUri.isPlatformResource()){
				// still a platform resource, try with EcorePlugin
				//System.out.println("[EMFUriHelper] platform:/resource not normalized, trying to resolve");
				normalizedUri = EcorePlugin.resolvePlatformResourcePath(emfUri.toString().replaceFirst("platform:/resource", "")); 
				//System.out.println("[EMFUriHelper] resolve result ="+normalizedUri);
			}
			return normalizedUri;
		}
}
