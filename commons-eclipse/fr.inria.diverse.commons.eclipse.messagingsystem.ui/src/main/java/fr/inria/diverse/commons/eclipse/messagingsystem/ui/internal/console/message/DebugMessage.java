/*$Id: DebugMessage.java 12250 2010-08-20 11:20:47Z dvojtise $
* Project : fr.irisa.triskell.eclipse.util
* File : 	InfoMessage.java
* License : EPL
* Copyright : IRISA / INRIA / Universite de Rennes 1
* ----------------------------------------------------------------------------
* Creation date : Feb 20, 2007
* Authors : ftanguy
*/
package fr.inria.diverse.commons.eclipse.messagingsystem.ui.internal.console.message;

public class DebugMessage extends ConsoleMessage {

	public DebugMessage(String content) {
		super(content, ConsoleMessage.DEBUG);
	}
	
}
