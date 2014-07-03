package fr.inria.diverse.commons.eclipse.messagingsystem.api;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import fr.inria.diverse.commons.messagingsystem.api.MessagingSystem;
import fr.inria.diverse.commons.messagingsystem.api.impl.StdioSimpleMessagingSystem;

public class MessagingSystemManager {

	public static final String MESSAGINGSYSTEM_EXTENSION_POINT_NAME = "fr.inria.diverse.commons.eclipse.messagingsystem";
	public static final String MESSAGINGSYSTEM_EXTENSION_POINT_CONTRIB = "MessagingSystem_Contribution";
	public static final String MESSAGINGSYSTEM_EXTENSION_POINT_CONTRIB_NAME_ATT = "name_string";
	public static final String MESSAGINGSYSTEM_EXTENSION_POINT_CONTRIB_MESSAGINGSYSTEM_ATT = "MessagingSystem_class";
	
	// best platform shared messaging system
	protected MessagingSystem bestPlatformMessaggingSystem= null;
	
	/**
	 * 
	 * @return the current shared instance of the best implementation of MessagingSystem for the current platform
	 */
	public MessagingSystem getBestPlatformSharedMessaggingSystem(){
	
		if(bestPlatformMessaggingSystem == null){
			bestPlatformMessaggingSystem = createBestPlatformMessagingSystem();
		}
		return bestPlatformMessaggingSystem;
	}
	
	
	/**
	 * 
	 * @return a new MessagingSystem that is supposed to be the best implementation for the current platform
	 */
	public MessagingSystem createBestPlatformMessagingSystem(){
		MessagingSystem result = null;
		IConfigurationElement[] confElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(MESSAGINGSYSTEM_EXTENSION_POINT_NAME);
		for (int i = 0; i < confElements.length; i++) {
			// get first working contribution
			// TODO find some criterion or properties allowing to have better selection in case of multiple definitions
			//String name = confElements[i].getAttribute(MESSAGINGSYSTEM_EXTENSION_POINT_CONTRIB_NAME_ATT);
			try {
				result = (MessagingSystem) confElements[i].createExecutableExtension(MESSAGINGSYSTEM_EXTENSION_POINT_CONTRIB_MESSAGINGSYSTEM_ATT);
				if(result != null)	break;
			} catch (CoreException e) {;
			}
		}
		if (result == null){
			// still not created, either due to exception or to missing extension contributor
			// fallback to default constructor
			result = new StdioSimpleMessagingSystem();
		}
		
		return result;
	}
}
