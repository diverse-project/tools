package fr.inria.diverse.commons.eclipse.emf.ui.popup.actions;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class CheckUnresolvedProxiesAction implements IObjectActionDelegate {

	private Shell shell;
	
	public IFile selectedEMFModelIFile = null; 
	
	/**
	 * Constructor for Action1.
	 */
	public CheckUnresolvedProxiesAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		String uristring = selectedEMFModelIFile.getLocation().toOSString();
	    URI uri = URI.createFileURI(uristring);
	    
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource res = resourceSet.getResource(uri, true);
		StringBuilder sb = new StringBuilder();
		sb.append("Checking unresolved proxies of "+uri+"\n");
		try {
			res.load(null);
		} catch (IOException e) {
			sb.append(e);
		}
		EcoreUtil.resolveAll(resourceSet);

		
		sb.append("Input resources:"+"\n");
		for(Resource r : resourceSet.getResources()) 
		{
			sb.append(r.getURI().toString()+"\n");
		}
		
		Map<EObject, Collection<Setting>>  unresolvedProxies = EcoreUtil.UnresolvedProxyCrossReferencer.find(resourceSet);
		if(unresolvedProxies.size() != 0){
			sb.append("UNRESOLVED PROXIES FOUND ! the first is "+unresolvedProxies.entrySet().toArray()[0]+"\n");
		}
		else{
			sb.append("no unresolved proxies");
		}
		
		MessageDialog.openInformation(
			shell,
			"Eclipse EMF Tools Ui",
			sb.toString());
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection != null & selection instanceof IStructuredSelection) {
			StructuredSelection ss = (StructuredSelection) selection;
			Object o = ss.getFirstElement();
			if (o instanceof IFile) {
				selectedEMFModelIFile = (IFile) o;	
			}
			if (o instanceof IAdaptable) {
				IFile res = (IFile) ((IAdaptable) o)
						.getAdapter(IFile.class);
				if (res != null) {
					selectedEMFModelIFile = res;
				}
			}
		}
	}

}
