/**
* Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.gnu.org/licenses/lgpl-3.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/**
* Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.gnu.org/licenses/lgpl-3.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/**
* Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.gnu.org/licenses/lgpl-3.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.kermeta.utils.aether

import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.impl.internal.{SimpleLocalRepositoryManagerFactory, EnhancedLocalRepositoryManagerFactory}
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.apache.maven.repository.internal.{MavenRepositorySystemSession, DefaultServiceLocator}
import org.sonatype.aether.transfer.{TransferEvent, TransferListener}
import org.sonatype.aether.repository.{LocalRepository, RepositoryPolicy}
import java.io.File
import org.slf4j.LoggerFactory
import org.sonatype.aether.{RepositoryCache, ConfigurationProperties, RepositorySystem}
import java.util

/**
* Created with IntelliJ IDEA.
* User: duke
* Date: 25/04/12
* Time: 22:44
*/

object AetherRepositoryStandalone {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def newRepositorySystem: RepositorySystem = {
    val locator = new DefaultServiceLocator()
  //  locator.setServices(classOf[Logger], new AetherLogger) // Doesn't work to JdkAsyncHttpProvider because this class uses its own logger and not the one provided by plexus and set with this line
   // locator.setService(classOf[RepositoryCache], classOf[NoopCache])
    // locator.setService(classOf[LocalRepositoryManagerFactory], classOf[EnhancedLocalRepositoryManagerFactory])
    locator.setService(classOf[LocalRepositoryManagerFactory], classOf[SimpleLocalRepositoryManagerFactory])



    locator.setService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.setService(classOf[RepositoryConnectorFactory], classOf[AsyncRepositoryConnectorFactory])
    locator.getService(classOf[RepositorySystem])
  }


  def getConfigURLS: java.util.List[String] = {
    if (defaultRepo == null) {
      defaultRepo = new java.util.HashSet[String]()
      try {
        val configFile = new File(System.getProperty("user.home").toString + File.separator + ".m2" + File.separator + "settings.xml")
        if (configFile.exists()) {
          val configRoot = scala.xml.XML.loadFile(configFile)
          val nodes = configRoot \\ "repositories" \ "repository" \ "url"
          nodes.foreach {
            url =>
              defaultRepo.add(url.text)
          }
        } else {
          logger.debug("settings.xml not found")
        }
      } catch {
        case _ @ e => logger.debug("Error while reading m2 config",e)
      }
    }
    import scala.collection.JavaConversions._
    defaultRepo.toList
  }

  var defaultRepo: util.HashSet[String] = null


  def newRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    session.setTransferListener(new TransferListener() {
      def transferInitiated(p1: TransferEvent) {
        logger.debug("Transfert init for Artifact " + p1.getResource.getResourceName)
      }

      def transferStarted(p1: TransferEvent) {
        logger.debug("Transfert begin for Artifact " + p1.getResource.getResourceName)
      }

      def transferProgressed(p1: TransferEvent) {
        logger.debug("Transfert in progress for Artifact " + p1.getResource.getResourceName)
      }

      def transferCorrupted(p1: TransferEvent) {
        logger.error("TransfertCorrupted : " + p1.getResource.getResourceName)
      }

      def transferSucceeded(p1: TransferEvent) {
        logger.debug("Transfert succeeded for Artifact " + p1.getResource.getResourceName)
      }

      def transferFailed(p1: TransferEvent) {
        logger.debug("TransferFailed : " + p1.getResource.getResourceName)
      }
    })
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
    session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN)
    //session.setConfigProperty("aether.connector.ahc.provider", "jdk")
    //DEFAULT VALUE
    session.setLocalRepositoryManager(newRepositorySystem.newLocalRepositoryManager(new LocalRepository(System.getProperty("user.home").toString + "/.m2/repository")))
    //TRY TO FOUND MAVEN CONFIGURATION
    val configFile = new File(System.getProperty("user.home").toString + File.separator + ".m2" + File.separator + "settings.xml")
    if (configFile.exists()) {
      val configRoot = scala.xml.XML.loadFile(configFile)
      configRoot.child.find(c => c.label == "localRepository").map {
        localRepo =>
          logger.info("Found localRepository value from settings.xml in user path => " + localRepo.text)
          session.setLocalRepositoryManager(newRepositorySystem.newLocalRepositoryManager(new LocalRepository(localRepo.text)))
      }
      val nodes = configRoot \\ "repositories" \ "repository" \ "url"
      
      // make sure it has be initiated 
      getConfigURLS
      nodes.foreach {
        url =>
          defaultRepo.add(url.text)
      }
      configRoot.child.find(c => c.label == "offline").map {
        offline =>
          logger.debug("Found offline value from settings.xml in user path => \"" + offline.text + "\"")
          offline.text match {
            case "true" => session.setOffline(true)
            case "yes" => session.setOffline(true)
            case _ =>
          }
      }
    } else {
      logger.debug("settings.xml not found")
    }
    session.getConfigProperties.put(ConfigurationProperties.REQUEST_TIMEOUT, 20000.asInstanceOf[java.lang.Integer])
    session.getConfigProperties.put(ConfigurationProperties.CONNECT_TIMEOUT, 1000.asInstanceOf[java.lang.Integer])
    session
  }
  
  /*
   val newRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    session.setTransferListener(new TransferListener() {
      def transferInitiated(p1: TransferEvent) {
        messagingSystem.initProgress(msgGroup + "." + session.hashCode(), "Transfert init    : " + p1.getResource.getResourceName + " from " + p1.getResource().getRepositoryUrl(), msgGroup, 1)
        //messagingSystem.debug("Transfert init for Artifact "+p1.getResource.getResourceName+" from "+p1.getResource().getRepositoryUrl(), msgGroup)
      }

      def transferStarted(p1: TransferEvent) {
        messagingSystem.log(MessagingSystem.Kind.DevDEBUG, "Transfert begin    : " + p1.getResource.getResourceName + " from " + p1.getResource().getRepositoryUrl(), msgGroup)
      }

      def transferProgressed(p1: TransferEvent) {
        messagingSystem.debug("Transfert in progress for Artifact " + p1.getResource.getResourceName + "(" + p1.getTransferredBytes() + "/" + p1.getResource().getContentLength() + ") from " + p1.getResource().getRepositoryUrl(), msgGroup)
      }

      def transferCorrupted(p1: TransferEvent) {
        messagingSystem.doneProgress(msgGroup + session.hashCode(), "Transfert Corrupted: " + p1.getResource.getResourceName + " from " + p1.getResource().getRepositoryUrl(), msgGroup)
        //messagingSystem.debug("TransfertCorrupted : "+p1.getResource.getResourceName+" from "+p1.getResource().getRepositoryUrl(), msgGroup)
      }

      def transferSucceeded(p1: TransferEvent) {
        messagingSystem.doneProgress(msgGroup + "." + session.hashCode(), "Transfert success : " + p1.getResource.getResourceName + " from " + p1.getResource().getRepositoryUrl(), msgGroup)
        //messagingSystem.debug("Transfert succeeded for Artifact "+p1.getResource.getResourceName+" from "+p1.getResource().getRepositoryUrl(), msgGroup)
      }

      def transferFailed(p1: TransferEvent) {
        messagingSystem.doneProgress(msgGroup + "." + session.hashCode(), "Artifact not found on repository  : " + p1.getResource.getResourceName + " from " + p1.getResource().getRepositoryUrl(), msgGroup)
        //messagingSystem.debug("TransferFailed : "+p1.getResource.getResourceName+" from "+p1.getResource().getRepositoryUrl(), msgGroup)
      }

    })
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
    session.setConfigProperty("aether.connector.ahc.provider", "jdk")

    session.setLocalRepositoryManager(getRepositorySystem.newLocalRepositoryManager(new LocalRepository(System.getProperty("user.home").toString + "/.m2/repository")))
    //TRY TO FIND MAVEN CONFIGURATION
    val configFile = new File(System.getProperty("user.home").toString + File.separator + ".m2" + File.separator + "settings.xml")
    var foundUserSpecificLocalRepositorySettings: Boolean = false
    if (configFile.exists()) {
      val configRoot = scala.xml.XML.loadFile(configFile)

      configRoot.child.find(c => c.label == "localRepository").map {
        localRepo =>
          messagingSystem.debug("Found localRepository value from settings.xml in user path => \"" + localRepo.text + "\"", msgGroup)
          session.setLocalRepositoryManager(getRepositorySystem.newLocalRepositoryManager(new LocalRepository(localRepo.text)))

      }
      configRoot.child.find(c => c.label == "offline").map {
        offline =>
          messagingSystem.debug("Found offline value from settings.xml in user path => \"" + offline.text + "\"", msgGroup)
          offline.text match {
            case "true" => session.setOffline(true)
            case "yes" => session.setOffline(true)
            case _ =>
          }
      }

    }
    if (!foundUserSpecificLocalRepositorySettings) {
      val localRepoString = System.getProperty("user.home").toString + File.separator + ".m2" + File.separator + "repository"
      messagingSystem.debug("localRepository not found in settings.xml, using " + localRepoString, msgGroup)
      session.setLocalRepositoryManager(getRepositorySystem.newLocalRepositoryManager(new LocalRepository(localRepoString)))
    }
    session.getConfigProperties.put(ConfigurationProperties.REQUEST_TIMEOUT, 2000.asInstanceOf[java.lang.Integer])
    session.getConfigProperties.put(ConfigurationProperties.CONNECT_TIMEOUT, 1000.asInstanceOf[java.lang.Integer])
    session
  }
   */

}