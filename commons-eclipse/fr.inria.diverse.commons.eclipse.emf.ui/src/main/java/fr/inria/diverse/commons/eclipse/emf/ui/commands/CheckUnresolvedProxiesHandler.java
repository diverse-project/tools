package fr.inria.diverse.commons.eclipse.emf.ui.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class CheckUnresolvedProxiesHandler extends AbstractHandler implements
		IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		if (selection != null & selection instanceof IStructuredSelection) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			for (@SuppressWarnings("unchecked")
					Iterator<Object> iterator = strucSelection.iterator(); iterator.hasNext();) {
				Object o = iterator.next();
				if (o instanceof IFile) {
					checkUnresolvedProxies(event, (IFile) o);
					;
				}
				if (o instanceof IAdaptable) {
					IFile res = (IFile) ((IAdaptable) o)
							.getAdapter(IFile.class);
					if (res != null) {
						checkUnresolvedProxies(event, res);
						;
					}
				}
			}
		}

		return null;
	}

	protected void checkUnresolvedProxies(ExecutionEvent event, IFile iFile) {
		String uristring = iFile.getLocation().toOSString();
		URI uri = URI.createFileURI(uristring);

		ResourceSet resourceSet = new ResourceSetImpl();
		Resource res = resourceSet.getResource(uri, true);
		StringBuilder sb = new StringBuilder();
		sb.append("Checking unresolved proxies of " + uri + "\n");
		try {
			res.load(null);
		} catch (IOException e) {
			sb.append(e);
		}
		EcoreUtil.resolveAll(resourceSet);

		sb.append("Input resources:" + "\n");
		for (Resource r : resourceSet.getResources()) {
			sb.append(r.getURI().toString() + "\n");
		}

		Map<EObject, Collection<Setting>> unresolvedProxies = EcoreUtil.UnresolvedProxyCrossReferencer
				.find(resourceSet);
		if (unresolvedProxies.size() != 0) {
			sb.append("\nUNRESOLVED PROXIES FOUND ! the first is "
					+ unresolvedProxies.entrySet().toArray()[0] + "\n");
		} else {
			sb.append("\nALL PROXIES HAVE BEEN SUCCESSFULY RESOLVED");
		}

		MessageDialog.openInformation(
				HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
				"Eclipse EMF Tools Ui", sb.toString());
	}

}
