

/*$Id: ConsoleIO.java 13571 2010-11-22 16:33:54Z hrambelo $
* Project : fr.irisa.triskell.kermeta.kpm
* File : 	IOConsole.java
* License : EPL
* Copyright : IRISA / INRIA / Universite de Rennes 1
* ----------------------------------------------------------------------------
* Creation date : Feb 20, 2007
* Authors : ftanguy
*/
package fr.inria.diverse.commons.eclipse.messagingsystem.ui.internal.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IOConsoleOutputStream;

import fr.inria.diverse.commons.eclipse.messagingsystem.ui.internal.console.message.ConsoleMessage;
import fr.inria.diverse.commons.eclipse.messagingsystem.ui.internal.console.message.InfoMessage;


/**
 * 
 * This console encapsulates an eclipse IOConsole instance and provides an
 * high level writing / reading protocol.
 * 
 * As a fact you can use this class either to manage output and input
 * string and display it in the console view.
 * 
 * @author ftanguy
 *
 */
abstract public class ConsoleIO {
	
	/**
	 * Considering the IOConsole protocol, several output stream can be created.
	 * For the moment, we just need one. To get access to it, please use the accessor method.
	 */
	protected OutputStream outputStream = null;
	
	/**
	 * The reader is used to read strings from the keyboard. To get access to it, please use the accessor method.
	 */
	protected BufferedReader reader;

	//////////////////////////
	//////////////////////////
	//		Constructor		//
	//////////////////////////
	//////////////////////////
	/**
	 * This constructor is IMPORTANT for the subclasses.
	 */
	//protected ConsoleIO() {}

	//////////////////////////////////
	//////////////////////////////////
	//		End of Constructor		//
	//////////////////////////////////
	//////////////////////////////////
	

	//////////////////////////
	//////////////////////////
	//		Accessors		//
	//////////////////////////
	//////////////////////////
	/**
	 * This is a lazy initialization.
	 * @return
	 */
	abstract public OutputStream getOutputStream();
	
	/**
	 * This is a lazy initialization.
	 * @return
	 */
	abstract public BufferedReader getReader();
	
	//////////////////////////////////
	//////////////////////////////////
	//		End of Accessors		//
	//////////////////////////////////
	//////////////////////////////////

	
	
	
	//////////////////////////////
	//////////////////////////////
	//		Writing Methods		//
	///////////////////////////////////////////////////////////////////////////////////////
	// The writing protocol provides some methods to print out some String or messages . //
	// Any object can be printed for the moment it declares a toString method.			 //
	///////////////////////////////////////////////////////////////////////////////////////
	public void print(Object o) {
		if ( o != null ) {
			print( new InfoMessage(o.toString()) );
		}
	}
	
	public void println(Object o) {
		if ( o != null ){
			println( new InfoMessage(o.toString()) );
		}
	}
	
	abstract public void print(ConsoleMessage message);
	
	/**
	 * this methods allow to change the color of futur message
	 * (this is because a simple change of current stream color, change the color for all messages, even previous ones ...) 
	 * @param c
	 */
	abstract public void changeColor(Color c);
	
	abstract public void println(ConsoleMessage message);
	//////////////////////////////////////
	//////////////////////////////////////
	//		End of Writing Methods		//
	//////////////////////////////////////
	//////////////////////////////////////
	
	
	
	//////////////////////////////
	//////////////////////////////
	//		Reading Methods		//
	//////////////////////////////
	//////////////////////////////
	public String read() {
		String line = "";
		BufferedReader reader = getReader();
		try {
			line = reader.readLine();
			if ( line == null ){
				line = "";
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return line;
	}
	
	public String promptAndRead(String prompt) {
		print(prompt);
		return read();
	}
	//////////////////////////////////////
	//////////////////////////////////////
	//		End of Reading Methods		//
	//////////////////////////////////////
	//////////////////////////////////////
	
	
	public void clear() {
	}
	
	
	public void dispose() {
		try {
			if ( outputStream != null ){
				outputStream.close();
			}
			if ( reader != null ){
				reader.close();
			//ConsolePlugin.getDefault().getConsoleManager().removeConsoles( new IConsole[]{console} );
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public void finalize() throws Throwable {
		super.finalize();
		if ( (outputStream != null) && ! ((IOConsoleOutputStream) getOutputStream()).isClosed() ){
			outputStream.close();
		}
	}
	
	
}
