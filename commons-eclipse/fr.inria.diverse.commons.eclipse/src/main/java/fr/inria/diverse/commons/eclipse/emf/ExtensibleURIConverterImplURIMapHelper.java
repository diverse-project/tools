/*$Id:  $
* License : EPL
* Copyright : IRISA / INRIA 
* ----------------------------------------------------------------------------
* Creation date : 30 sept. 2011
* Authors : 
*      Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.eclipse.emf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

public class ExtensibleURIConverterImplURIMapHelper {
	
	public static String URIMAP_LOCATION_SYSTEM_PROPERTY = "urimap.file.location";
	
	/**
	 * fill the ExtensibleURIConverterImpl.URI_MAP with the file referenced by the system property "urimap.file.location"
	 * @param ignorePlatformResource if true, will not add in the map an entry key platform:/resource (useful if this entry is used by EcorePlugin.java instead)
	 */
	public static void fillMapFromSystemPropertyFile(boolean ignorePlatformResource){
		Properties props;
		if(ignorePlatformResource) {
			props = filterPlatformResource(getMapFromSystemProperty());
		}
		else{
			props = getMapFromSystemProperty();
		}
		for(Entry<Object, Object> entry : props.entrySet()){
			// TODO make sure to not add the same URI twice
			URI keyURI = URI.createURI(entry.getKey().toString());
			URI valueURI = URI.createURI(entry.getValue().toString());
			if(keyURI.isPlatformResource()){
				valueURI = URI.createURI(entry.getValue().toString()+"/");
				EcorePlugin.getPlatformResourceMap().put(keyURI.lastSegment(),valueURI);
				//System.out.println("PlatformResourceMap().put " + keyURI.lastSegment()+"  =>  " +valueURI);
			}else{
				ExtensibleURIConverterImpl.URI_MAP.put(keyURI,valueURI);
			}
		}
	}
	
	public static Properties getMapFromSystemProperty(){
		String fileLocation = System.getProperties().getProperty(URIMAP_LOCATION_SYSTEM_PROPERTY);
		Properties props = new Properties();
		if (fileLocation == null) return props;
		
		try {
			FileInputStream fis = new FileInputStream(fileLocation);
			props.load(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}
	
	public static Properties filterPlatformResource(Properties originalProperties){
		if(originalProperties.containsKey("platform:/resource")){
			Properties props = new Properties();
			for(Entry<Object, Object> entry : props.entrySet()){
				if(!entry.getKey().equals("platform:/resource")  )
					props.put(entry.getKey(), entry.getValue());
			}
			return props;
		}
		else
			return originalProperties;
	}
	
	public static String getPlatformResource(Properties props){
		return props.get("platform:/resource").toString();
	}

}
