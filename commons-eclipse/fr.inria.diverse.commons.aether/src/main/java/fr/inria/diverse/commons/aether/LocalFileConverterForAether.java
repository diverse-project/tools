/*$Id:  $
* License : EPL
* Copyright : IRISA / INRIA 
* ----------------------------------------------------------------------------
* Creation date : 26 sept. 2011
* Authors : 
*      Didier Vojtisek <didier.vojtisek@inria.fr>
*/
package fr.inria.diverse.commons.aether;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import fr.inria.diverse.commons.messagingsystem.api.MessagingSystem;

import org.sonatype.aether.resolution.ArtifactResolutionException;

import fr.inria.diverse.commons.aether.AetherUtil;
import fr.inria.diverse.commons.eclipse.uri.LocalFileConverter;

public class LocalFileConverterForAether extends LocalFileConverter {

	protected MessagingSystem logger;
	protected String baseMsgGroup;
	protected java.util.List<String> repositoriesUrl;
	
	public LocalFileConverterForAether(MessagingSystem messagingSystem , String baseMsgGroup, java.util.List<String> repositoriesUrl){
		this.baseMsgGroup = baseMsgGroup;
		this.logger = messagingSystem;
		this.repositoriesUrl = repositoriesUrl;
	}
	public LocalFileConverterForAether(MessagingSystem messagingSystem , String baseMsgGroup, String repositoryUrl){
		this.baseMsgGroup = baseMsgGroup;
		this.logger = messagingSystem;
		this.repositoriesUrl = Arrays.asList(repositoryUrl);
	}
	
	@Override
	public java.net.URI convertSpecialURItoFileURI(java.net.URI javaUri) {
		try {
			URL javaUrl = javaUri.toURL();
			if(javaUrl.getProtocol().equals("file")){
				// already a file, nothing to do
				return javaUri;
			}
			if(javaUrl.getProtocol().equals("mvn")){
				AetherUtil aetherUtil = new AetherUtil(logger,baseMsgGroup);
				File theFile = aetherUtil.resolveMavenArtifact4J(javaUrl.toString(), repositoriesUrl);
				if(theFile.exists())
					return theFile.toURI();
			}
			
			
		} catch (MalformedURLException e) {
			return null;
		} catch (ArtifactResolutionException e) {
			return null;
		}
		
		return null;
	}

}
