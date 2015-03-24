package org.eclipse.emf.ecoretools.registration.ui;

import org.eclipse.ui.IStartup;

public class EarlyStartUp implements IStartup {

	public void earlyStartup() {
		RegistrationUIPlugin.getDefault();
	}

}
