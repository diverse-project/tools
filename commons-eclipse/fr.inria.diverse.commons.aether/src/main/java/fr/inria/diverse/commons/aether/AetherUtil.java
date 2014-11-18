package fr.inria.diverse.commons.aether;

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import fr.inria.diverse.commons.messagingsystem.api.impl.StdioSimpleMessagingSystem;
import fr.inria.diverse.commons.messagingsystem.api.MessagingSystem;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;


/**
 * User: ffouquet Date: 25/07/11 Time: 15:06
 */

public class AetherUtil extends AetherRepositoryHandler {

	protected MessagingSystem messagingSystem;
	protected String baseMsgGroup;

	public AetherUtil(MessagingSystem messagingSystem, String baseMsgGroup) {
		this.messagingSystem = messagingSystem;
		this.baseMsgGroup = baseMsgGroup;
	}

	public AetherUtil() {
		this(new StdioSimpleMessagingSystem(), "");
	}

	protected static AetherUtil instance = null;

	public AetherUtil getInstance() {
		if (instance == null) {
			instance = new AetherUtil();
		}
		return instance;
	}

	/**
	 * setOffline mode if this is a maven repository system
	 */
	public void setOffline(boolean b) {
		if (getRepositorySystemSession() instanceof MavenRepositorySystemSession) {
			((MavenRepositorySystemSession) getRepositorySystemSession())
					.setOffline(b);
		}
	}

	public File resolveMavenArtifact(String mavenurl,
			java.util.List<String> repositoriesUrl)
			throws MalformedURLException, ArtifactResolutionException {

		File file = null;
		if (mavenurl.startsWith("mvn:")) {
			String url = mavenurl.substring(4);
			if (url.contains("!")) {
				String repourl = url.substring(0, url.indexOf("!"));
				String urlids = url.substring(url.indexOf("!") + 1);
				String[] part = urlids.split("/");
				List<String> fixedrepositoriesUrl = new ArrayList<String>();
				fixedrepositoriesUrl.add(repourl);
				if (part.length == 3) {
					file = resolveMavenArtifact(part[0], part[1], part[2],
							fixedrepositoriesUrl);
				} else if (part.length == 2) {
					file = resolveMavenArtifact(part[0], part[1], null,
							fixedrepositoriesUrl);
				} else {
					throw new MalformedURLException(
							"Bad MVN URL <mvn:[repourl!]groupID/artefactID[/version]> "
									+ mavenurl);
				}
			}
			if (file == null) {
				String[] part = url.split("/");
				if (part.length == 3) {
					file = resolveMavenArtifact(part[0], part[1], part[2],
							repositoriesUrl);
				} else if (part.length == 2) {
					file = resolveMavenArtifact(part[0], part[1], null,
							repositoriesUrl);
				} else {
					throw new MalformedURLException(
							"Bad MVN URL <mvn:[repourl!]groupID/artefactID[/version]> "
									+ mavenurl);
				}
			}
		} else {
			throw new MalformedURLException(
					"Bad MVN URL <mvn:[repourl!]groupID/artefactID[/version]> "
							+ mavenurl);
		}
		return file;
	}

	public File resolveMavenArtifact(String mavenurl, String repoURL)
			throws MalformedURLException, ArtifactResolutionException {
		List<String> repositoriesUrl = new ArrayList<String>();
		repositoriesUrl.add(repoURL);
		// try{
		return resolveMavenArtifact(mavenurl, repositoriesUrl);
		/*
		 * } catch { case _@e => { e.printStackTrace() null } }
		 */
	}

	public File resolveMavenArtifact4J(String groupID, String artifactID,
			String version, List<String> repositoriesUrl)
			throws ArtifactResolutionException {
		return resolveMavenArtifact(groupID, artifactID, version,
				repositoriesUrl);
	}

	public File resolveMavenArtifact4J(String mavenurl,
			List<String> repositoriesUrl) throws MalformedURLException,
			ArtifactResolutionException {
		return resolveMavenArtifact(mavenurl, repositoriesUrl);
	}

	public File resolveMavenArtifact(String groupID, String artifactID,
			String version, String repoURL) throws ArtifactResolutionException {
		List<String> repositoriesUrl = new ArrayList<String>();
		repositoriesUrl.add(repoURL);
		return resolveMavenArtifact(groupID, artifactID, version,
				repositoriesUrl);
	}

	public File resolveMavenArtifact(String groupID, String artifactID,
			String version, List<String> repositoriesUrl)
			throws ArtifactResolutionException {
		Artifact artifact = new DefaultArtifact(groupID + ":" + artifactID
				+ ":" + version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);

		java.util.List<RemoteRepository> repositories = new java.util.ArrayList<RemoteRepository>();

		for (String repoURL : repositoriesUrl) {
			RemoteRepository repo = new RemoteRepository();
			repo.setId(repoURL.trim().replace("http://", "").replace(':', '_')
					.replace('/', '_').replace('\\', '_'));
			// repo.setUrl(repoURL.trim.replace(':', '_').replace('/',
			// '_').replace('\\', '_'))
			repo.setUrl(repoURL);
			repo.setContentType("default");
			RepositoryPolicy repositoryPolicy = new RepositoryPolicy();
			repositoryPolicy
					.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);
			repositoryPolicy
					.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
			repo.setPolicy(true, repositoryPolicy);
			repositories.add(repo);
		}

		// add default central repo
		RemoteRepository centralRepo = new RemoteRepository();
		centralRepo.setId("central");
		centralRepo.setUrl("http://repo1.maven.org/maven2");
		centralRepo.setContentType("default");
		repositories.add(centralRepo);

		artifactRequest.setRepositories(repositories);
		ArtifactResult artefactResult = null;

		artefactResult = getRepositorySystem().resolveArtifact(
				getRepositorySystemSession(), artifactRequest);
		return artefactResult.getArtifact().getFile();
	}

	public String msgGroup() {
		if (baseMsgGroup.equals(""))
			return this.getClass().getName() + hashCode();
		else
			return baseMsgGroup + "." + this.getClass().getName() + hashCode();
	}

}
