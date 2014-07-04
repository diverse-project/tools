package org.eclipse.emf.ecoretools.registration;


import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecoretools.registration.exceptions.NotValidEPackageURIException;
//import org.eclipse.emf.ecoretools.registration.popup.actions.UMLRegisteringJob;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Package;



public class UMLRegistering {

	
	/**
	 * Register the contained EPackages by the given IFile
	 * Ignores EPackages for which the NsURI is already present in the registry
	 * @param umlFile
	 * @throws NotValidEPackageURIException 
	 */
	public static void registerPackages(IFile umlFile) {
		String strURI = "platform:/resource" + umlFile.getFullPath().toString(); 
		URI mmURI = URI.createURI(strURI);
		registerPackages(mmURI);
	}
	
	/**
	 * Registers the given uml file and all its contained EPackages
	 * Ignores EPackages for which the NsURI is already present in the registry
	 * @param pack
	 * @throws NotValidEPackageURIException 
	 */
	public static void registerPackages(URI umlFileUri) {
	
		System.out.println("Trying to load " + umlFileUri.toString());
		
    	// Create a resource set.
		ResourceSet resourceSet = new ResourceSetImpl();
		
		// Register the default resource factory -- only needed for
		// stand-alone!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap() 
			.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
					new XMIResourceFactoryImpl());
		// Register the package to ensure it is available during loading.
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI,UMLPackage.eINSTANCE);
		
		// Demand load the resource for this file.
		Resource resource = resourceSet.getResource(umlFileUri, true);
		// Process resources

		System.out.println("Resource " + resource.getURI().toString() + " loaded ...");
		
		//Browse the file content
		for ( EObject o : resource.getContents()){
		
			if(o instanceof org.eclipse.uml2.uml.Package){
				processPackage((Package)o);
			}
			
			if (o instanceof EAnnotation) {
				processEAnnotation((EAnnotation) o);
			}
			
		}
		

	}
	
	private static void processEAnnotation(EAnnotation annotation) {
		
		for (EObject annObj : annotation.eContents() ) {
			
			if (annObj instanceof EPackage) {
				EPackage pack = (EPackage)annObj;
				System.out.println("Registering package " + pack.getNsURI());
				try {
					EcoreRegistering.registerPackages(pack);
				} catch (NotValidEPackageURIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void processPackage(org.eclipse.uml2.uml.Package pack) {
		
		for (EObject obj : pack.eContents()) {
			
			if (obj instanceof org.eclipse.uml2.uml.Package) {
				processPackage((Package) obj);
			}
			
			if (obj instanceof EAnnotation) {
				processEAnnotation((EAnnotation) obj);
			}
			
		}
		
	}
}
