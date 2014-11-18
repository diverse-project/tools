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
package fr.inria.diverse.commons.aether;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.slf4j.LoggerFactory;



public class AetherRepositoryHandler {

  private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  protected RepositorySystem repositorySystem = null;

  public void setRepositorySystem(RepositorySystem rs) {
    repositorySystem = rs;
  }

  public RepositorySystem getRepositorySystem() {
    if (repositorySystem == null) {
      return AetherRepositoryStandalone.getInstance().newRepositorySystem();
    } else {
      return repositorySystem;
    }
  }


  protected RepositorySystemSession repositorySession = null;

  public void setRepositorySystemSession(RepositorySystemSession rs) {
    repositorySession = rs;
  }

  public RepositorySystemSession getRepositorySystemSession() {
    if (repositorySession == null) {
      return AetherRepositoryStandalone.getInstance().newRepositorySystemSession();
    } else {
      return repositorySession;
    }
  }

  java.util.List<String> getDefaultURLS = AetherRepositoryStandalone.getInstance().getConfigURLS();


}