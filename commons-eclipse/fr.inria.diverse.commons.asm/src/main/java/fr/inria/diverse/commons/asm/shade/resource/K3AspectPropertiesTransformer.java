package fr.inria.diverse.commons.asm.shade.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;

import fr.inria.diverse.commons.asm.shade.relocation.Relocator;

public class K3AspectPropertiesTransformer implements ResourceTransformer {

	public static final String PROP_EXTENSION = ".k3_aspect_mapping.properties";
	
	private String targetBundleSymbolicName = null;
	
	public K3AspectPropertiesTransformer(String targetBundleSymbolicName) {
		super();
		this.targetBundleSymbolicName = targetBundleSymbolicName;
	}

	@Override
	public boolean canTransformResource(String resourceName) {
		return resourceName.endsWith(PROP_EXTENSION);
	}

	@Override
	public String processResource(String resourceName, File resourceFile,
			File outputFolder, List<Relocator> relocators) throws IOException {

		FileInputStream is = new FileInputStream(resourceFile);
		String sourceContent = IOUtil.toString(new InputStreamReader(is,
				"UTF-8"));

		for (Relocator relocator : relocators) {
			sourceContent = relocator.preApplyToSourceContent(sourceContent);
		}
		for (Relocator relocator : relocators) {
			sourceContent = relocator.finalizeApplyToSourceContent(sourceContent);
		}
		
		// compute the new file name
		String targetResourceName  = resourceName;
		if(targetBundleSymbolicName != null ){
			if(resourceName.lastIndexOf("/")!=-1) {
				targetResourceName= resourceName.substring(0, resourceName.lastIndexOf("/")) + "/"+targetBundleSymbolicName+PROP_EXTENSION;
			}
			else {				
				targetResourceName = targetBundleSymbolicName+PROP_EXTENSION;
			}
		}
		
		
		FileOutputStream os = new FileOutputStream(
				outputFolder.getCanonicalPath() + '/' + targetResourceName);
		OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
		IOUtil.copy(sourceContent, writer);
		writer.flush();
		is.close();
		os.close();
		
		return targetResourceName;

	}

	public String getTargetBundleSymbolicName() {
		return targetBundleSymbolicName;
	}

	public void setTargetBundleSymbolicName(String targetBundleSymbolicName) {
		this.targetBundleSymbolicName = targetBundleSymbolicName;
	}

}
