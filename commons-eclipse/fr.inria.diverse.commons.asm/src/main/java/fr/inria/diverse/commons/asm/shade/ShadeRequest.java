package fr.inria.diverse.commons.asm.shade;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//import org.apache.maven.plugins.shade.filter.Filter;
//import org.apache.maven.plugins.shade.resource.ResourceTransformer;

import fr.inria.diverse.commons.asm.shade.filter.Filter;
import fr.inria.diverse.commons.asm.shade.relocation.Relocator;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Parameter object used to pass  args to DirectoryShader.shade()
 * @since 2.0
 */
public class ShadeRequest
{

    private Set<File> inputFolders;

    private File outputFolder;

    private List<Filter> filters;

    private List<Relocator> relocators;
    
    private boolean mustSortRelocators = true;

   // private List<ResourceTransformer> resourceTransformers;

    public boolean isMustSortRelocators() {
		return mustSortRelocators;
	}

	public void setMustSortRelocators(boolean mustSortRelocators) {
		this.mustSortRelocators = mustSortRelocators;
	}

	private boolean shadeSourcesContent;

    public Set<File> getInputFolders()
    {
        return inputFolders;
    }

    /**
     * Which source folder to shade.
     * this must be the base folder containing the sources, not the actual package, use filter to limit the shading within a source folder 
     * @param inputFolders
     */
    public void setInputFolders( Set<File> inputFolders )
    {
        this.inputFolders = inputFolders;
    }

    public File getOutputFolder()
    {
        return outputFolder;
    }

    /**
     * Output folder.
     *
     * @param outputFolder
     */
    public void setOutputFolder( File outputFolder )
    {
        this.outputFolder = outputFolder;
    }

    public List<Filter> getFilters()
    {
        return filters;
    }

    /**
     * The filters.
     *
     * @param filters
     */
    public void setFilters( List<Filter> filters )
    {
        this.filters = filters;
    }

    /**
     * try to reorder the relocators in a way that even if their pattern are included in one another, 
     *  only the stronger pattern will be applied 
     */
    public void sortRelocators(){
    	Collections.sort(relocators);
    }
    
    public List<Relocator> getRelocators()
    {
        return relocators;
    }

    /**
     * The relocators.
     *
     * @param relocators
     */
    public void setRelocators( List<Relocator> relocators )
    {
        this.relocators = relocators;
    }

  /*  public List<ResourceTransformer> getResourceTransformers()
    {
        return resourceTransformers;
    }
*/
    /**
     * The transformers.
     *
     * @param resourceTransformers
     */
  /*  public void setResourceTransformers( List<ResourceTransformer> resourceTransformers )
    {
        this.resourceTransformers = resourceTransformers;
    }*/

    public boolean isShadeSourcesContent()
    {
        return shadeSourcesContent;
    }

    /**
     * When true, it will attempt to shade the contents of the java source files when creating the sources jar.
     * When false, it will just relocate the java source files to the shaded paths, but will not modify the
     * actual contents of the java source files.
     *
     * @param shadeSourcesContent
     */
    public void setShadeSourcesContent( boolean shadeSourcesContent )
    {
        this.shadeSourcesContent = shadeSourcesContent;
    }
}
