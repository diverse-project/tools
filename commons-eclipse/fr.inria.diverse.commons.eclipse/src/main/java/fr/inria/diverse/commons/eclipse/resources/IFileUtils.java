package fr.inria.diverse.commons.eclipse.resources;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;


public class IFileUtils {
	
	static String lineSeparator = System.getProperty("line.separator");
	
	
	public static void writeInFileIfDifferent(IFile file, String contents, IProgressMonitor monitor) throws CoreException, IOException{		
		InputStream stream =  new ByteArrayInputStream(contents.getBytes(("UTF-8")));
		try{
			if (file.exists()) {
				if(!IOUtils.contentEquals(stream, file.getContents(true))){
					stream =  new ByteArrayInputStream(contents.getBytes(("UTF-8")));
					file.setContents(stream, true, true, monitor);	
				}
			} else {
				file.create(stream, true, monitor);
			}
		}
		finally{
			IOUtils.closeQuietly(stream);
		}
	}
	
	
	public static void writeInFile(IFile file, String contents, IProgressMonitor monitor) throws CoreException, IOException{
		InputStream stream =  new ByteArrayInputStream(contents.getBytes(("UTF-8")));
		if (file.exists()) {
			file.setContents(stream, true, true, monitor);
		} else {
			file.create(stream, true, monitor);
		}
		stream.close();
	}
	
	public static void copy(final InputStream inStream, final OutputStream outStream, final int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];
		int nbRead;
		while ((nbRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, nbRead);
		}
	}
	   
	public static void copyDirectory(final File from, final File to) throws IOException {
		if (! to.exists()) {
			to.mkdir();
		}
		final File[] inDir = from.listFiles();
		for (int i = 0; i < inDir.length; i++) {
			final File file = inDir[i];
			copy(file, new File(to, file.getName()));
		}
	}
	
	public static void copyFile(final File from, final File to) throws IOException {
		final InputStream inStream = new FileInputStream(from);
		final OutputStream outStream = new FileOutputStream(to);
		copy(inStream, outStream, (int) Math.min(from.length(), 4*1024));
		inStream.close();
		outStream.close();
	}
	
	public static void copy(final File from, final File to) throws IOException {
		if (from.isFile()) {
			copyFile(from, to);
		} else if (from.isDirectory()){
			copyDirectory(from, to);
		} else {
			throw new FileNotFoundException(from.toString() + " does not exist" );
		}
	}
	
	/**
	 * Unzip the content of an archive contained in a plugin into a given IProject
	 * @param project destination of the unzip
	 * @param projectDesc description of the source bundle that contains the zip file and zip location in this bundle
	 * @throws IOException
	 */
	public static void unZip(IProject project, ProjectDescriptor projectDesc) throws IOException {

		URL interpreterZipUrl = FileLocator.find(Platform.getBundle(projectDesc.getBundleName()), new Path(projectDesc.getZipLocation()), null);
		// We make sure that the project is created from this point forward.
		
		ZipInputStream zipFileStream = new ZipInputStream(interpreterZipUrl.openStream());
		ZipEntry zipEntry = zipFileStream.getNextEntry();
		
		// We derive a regexedProjectName so that the dots don't end up being
		//  interpreted as the dot operator in the regular expression language.
		//String regexedProjectName = projectName.replaceAll("\\.", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
		
		while (zipEntry != null) {
			// We will construct the new file but we will strip off the project
			//  directory from the beginning of the path because we have already
			//  created the destination project for this zip.
			File file = new File(project.getLocation().toString(), zipEntry.getName());

			if (false == zipEntry.isDirectory()) {

				/*
				 * Copy files (and make sure parent directory exist)
				 */
				File parentFile = file.getParentFile();
				if (null != parentFile && false == parentFile.exists()) {
					parentFile.mkdirs();
				}
					OutputStream os = null;

					try {
						os = new FileOutputStream(file);

						byte[] buffer = new byte[102400];
						while (true) {
							int len = zipFileStream.read(buffer);
							if (zipFileStream.available() == 0)
								break;
							os.write(buffer, 0, len);
						}
					} finally {
						if (null != os) {
							os.close();
						}
					}
			}
			
			zipFileStream.closeEntry();
			zipEntry = zipFileStream.getNextEntry();
		}
		
	}

}
	