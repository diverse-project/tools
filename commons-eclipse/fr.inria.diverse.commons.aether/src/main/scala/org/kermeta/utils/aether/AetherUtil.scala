package org.kermeta.utils.aether

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

import org.sonatype.aether.{ ConfigurationProperties, RepositorySystem }
import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManagerFactory
import org.sonatype.aether.impl.internal.EnhancedLocalRepositoryManagerFactory
import org.sonatype.aether.resolution.ArtifactRequest
import org.sonatype.aether.resolution.ArtifactResult
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import java.io.File
import org.sonatype.aether.repository.{ RepositoryPolicy, RemoteRepository, LocalRepository }
import org.sonatype.aether.artifact.Artifact
import scala.collection.JavaConversions._
import org.sonatype.aether.transfer.{ TransferEvent, TransferListener }
import java.net.MalformedURLException
import org.kermeta.utils.systemservices.api.messaging.MessagingSystem
import org.kermeta.utils.systemservices.api.impl.StdioSimpleMessagingSystem

/**
 * User: ffouquet
 * Date: 25/07/11
 * Time: 15:06
 */

class AetherUtil(val messagingSystem: MessagingSystem, val baseMsgGroup: String) extends AetherRepositoryHandler {

  def this() = this(new StdioSimpleMessagingSystem(), "")
  
  /**
   * setOffline mode if this is a maven repository system
   */
  def setOffline(b : Boolean ) = {
    if(getRepositorySystemSession.isInstanceOf[MavenRepositorySystemSession]){
      getRepositorySystemSession.asInstanceOf[MavenRepositorySystemSession].setOffline(b)
    }
  }

  def resolveMavenArtifact(mavenurl: String, repositoriesUrl: List[String]): File = {

    var file: File = null
    if (mavenurl.startsWith("mvn:")) {
      val url = mavenurl.substring(4)
      if (url.contains("!")) {
        val repourl = url.substring(0, url.indexOf("!"))
        val urlids = url.substring(url.indexOf("!") + 1)
        val part = urlids.split("/")
        if (part.size == 3) {
          file = resolveMavenArtifact(part(0), part(1), part(2), List(repourl))
        } else if (part.size == 2) {
          file = resolveMavenArtifact(part(0), part(1), null, List(repourl))
        } else {
          throw new MalformedURLException("Bad MVN URL <mvn:[repourl!]groupID/artefactID[/version]> " + mavenurl)
        }
      }
      if (file == null) {
        val part = url.split("/")
        if (part.size == 3) {
          file = resolveMavenArtifact(part(0), part(1), part(2), repositoriesUrl)
        } else if (part.size == 2) {
          file = resolveMavenArtifact(part(0), part(1), null, repositoriesUrl)
        } else {
          throw new MalformedURLException("Bad MVN URL <mvn:[repourl!]groupID/artefactID[/version]> " + mavenurl)
        }
      }
    } else {
      throw new MalformedURLException("Bad MVN URL <mvn:[repourl!]groupID/artefactID[/version]> " + mavenurl)
    }
    file
  }

  def resolveMavenArtifact(mavenurl: String, repoURL: String): File = {
    val repositoriesUrl: List[String] = List(repoURL)
    // try{
    resolveMavenArtifact(mavenurl, repositoriesUrl)
    /*} catch {
        case _@e => {
          e.printStackTrace()
          null
        }
      }*/
  }
  def resolveMavenArtifact4J(groupID: String, artifactID: String, version: String, repositoriesUrl: java.util.List[String]): File =
    resolveMavenArtifact(groupID, artifactID, version, repositoriesUrl.toList)
  def resolveMavenArtifact4J(mavenurl: String, repositoriesUrl: java.util.List[String]): File =
    resolveMavenArtifact(mavenurl, repositoriesUrl.toList)

  def resolveMavenArtifact(groupID: String, artifactID: String, version: String, repoURL: String): File = {
    val repositoriesUrl: List[String] = List(repoURL)
    resolveMavenArtifact(groupID, artifactID, version, repositoriesUrl)
  }

  def resolveMavenArtifact(groupID: String, artifactID: String, version: String, repositoriesUrl: List[String]): File = {
    val artifact: Artifact = new DefaultArtifact(List(groupID, artifactID, version).mkString(":"))
    val artifactRequest = new ArtifactRequest
    artifactRequest.setArtifact(artifact)

    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList();

    repositoriesUrl.foreach {
      repoURL =>
        val repo = new RemoteRepository
        repo.setId(repoURL.trim.replace("http://", "").replace(':', '_').replace('/', '_').replace('\\', '_'))
        //repo.setUrl(repoURL.trim.replace(':', '_').replace('/', '_').replace('\\', '_'))
        repo.setUrl(repoURL)
        repo.setContentType("default")
        val repositoryPolicy = new RepositoryPolicy()
        repositoryPolicy.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN)
        repositoryPolicy.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
        repo.setPolicy(true, repositoryPolicy)
        repositories.add(repo)
    }

    // add default central repo
    val centralRepo = new RemoteRepository
    centralRepo.setId("central")
    centralRepo.setUrl("http://repo1.maven.org/maven2")
    centralRepo.setContentType("default")
    repositories.add(centralRepo)

    artifactRequest.setRepositories(repositories)
    var artefactResult: ArtifactResult = null;

    artefactResult = getRepositorySystem.resolveArtifact(getRepositorySystemSession, artifactRequest)
    artefactResult.getArtifact.getFile
  }

  

  val msgGroup: String = {
    if (baseMsgGroup.equals(""))
      this.getClass().getName() + hashCode()
    else
      baseMsgGroup + "." + this.getClass().getName() + hashCode()
  }

}

object AetherUtil {}