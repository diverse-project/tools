package fr.inria.diverse.commons.asm.tests.shade;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

import junit.framework.TestCase;



import fr.inria.diverse.commons.asm.shade.DirectoryShader;
import fr.inria.diverse.commons.asm.shade.ShadeRequest;
import fr.inria.diverse.commons.asm.shade.filter.Filter;
import fr.inria.diverse.commons.asm.shade.filter.SimpleFilter;
import fr.inria.diverse.commons.asm.shade.relocation.Relocator;
import fr.inria.diverse.commons.asm.shade.relocation.SimpleRelocator;

/**
 * @author Jason van Zyl
 * @author Mauro Talevi
 */
public class DirectoryShaderTest
    extends TestCase
{
    private static final String[] EXCLUDES = new String[] { "org/gemoc/sigpml/util/SigpmlSwitch",
        "org/gemoc/sigpml/impl.*" };

    public void testShaderWithDefaultShadedPatternWithExclude()
        throws Exception
    {
        shaderWithPattern( null, new File( "target/gemoc_shaded_default_with_excludes" ), EXCLUDES );
        assertTrue(new File( "target/gemoc_shaded_default_with_excludes/hidden/org/gemoc/sigpml" ).exists());
        assertTrue(new File( "target/gemoc_shaded_default_with_excludes/hidden/org/gemoc/sigpml/util" ).exists());
        assertFalse(new File( "target/gemoc_shaded_default_with_excludes/hidden/org/gemoc/sigpml/impl" ).exists());
        assertTrue(new File( "target/gemoc_shaded_default_with_excludes/org/gemoc/sigpml/impl" ).exists());
        // TODO check that all ref to org/gemoc/sigpml/impl are correct in the files
        assertTrue(FileUtils.fileRead("target/gemoc_shaded_default_with_excludes/hidden/org/gemoc/sigpml/SigpmlFactory.java").contains(" org.gemoc.sigpml.impl.SigpmlFactoryImpl"));
    }

 /*   public void testShaderWithStaticInitializedClass()
        throws Exception
    {
        Shader s = newShader();

        Set<File> set = new LinkedHashSet<File>();

        set.add( new File( "src/test/jars/test-artifact-1.0-SNAPSHOT.jar" ) );

        List<Relocator> relocators = new ArrayList<Relocator>();

        relocators.add( new SimpleRelocator( "org.apache.maven.plugins.shade", null, null, null ) );

        List<ResourceTransformer> resourceTransformers = new ArrayList<ResourceTransformer>();

        List<Filter> filters = new ArrayList<Filter>();

        File file = new File( "target/testShaderWithStaticInitializedClass.jar" );

        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars( set );
        shadeRequest.setUberJar( file );
        shadeRequest.setFilters( filters );
        shadeRequest.setRelocators( relocators );
        shadeRequest.setResourceTransformers( resourceTransformers );

        s.shade( shadeRequest );

        URLClassLoader cl = new URLClassLoader( new URL[] { file.toURI().toURL() } );
        Class<?> c = cl.loadClass( "hidden.org.apache.maven.plugins.shade.Lib" );
        Object o = c.newInstance();
        assertEquals( "foo.bar/baz", c.getDeclaredField( "CONSTANT" ).get( o ) );
    }
*/
    public void testShaderWithCustomShadedPatternWithExclude()
        throws Exception
    {
        shaderWithPattern( "org/gemoc/sigpml/extended", new File( "target/gemoc_shaded_extended_with_excludes" ), EXCLUDES );
        assertTrue(new File( "target/gemoc_shaded_extended_with_excludes/org/gemoc/sigpml/extended" ).exists());
        assertTrue(new File( "target/gemoc_shaded_extended_with_excludes/org/gemoc/sigpml/extended/util" ).exists());
        assertFalse(new File( "target/gemoc_shaded_extended_with_excludes/org/gemoc/sigpml/extended/impl" ).exists());
        assertTrue(new File( "target/gemoc_shaded_extended_with_excludes/org/gemoc/sigpml/impl" ).exists());
        // check that all ref to org/gemoc/sigpml/impl are correct in the files
        assertTrue(FileUtils.fileRead("target/gemoc_shaded_extended_with_excludes/org/gemoc/sigpml/extended/SigpmlFactory.java").contains(" org.gemoc.sigpml.impl.SigpmlFactoryImpl"));
    }

    public void testShaderWithCustomShadedPattern()
        throws Exception
    {
        // FIXME: shaded jar should not include references to org/codehaus/* (empty dirs) or org.codehaus.* META-INF
        // files.
        shaderWithPattern( "org/gemoc/sigpml/extended", new File( "target/gemoc_shaded_extended" ),
                           new String[] {} );

        assertTrue(new File( "target/gemoc_shaded_extended/org/gemoc/sigpml/extended" ).exists());
        assertTrue(new File( "target/gemoc_shaded_extended/org/gemoc/sigpml/extended/util" ).exists());
        assertTrue(new File( "target/gemoc_shaded_extended/org/gemoc/sigpml/extended/impl" ).exists());
        assertTrue(FileUtils.fileRead("target/gemoc_shaded_extended_with_excludes/org/gemoc/sigpml/extended/SigpmlFactory.java").contains(" org.gemoc.sigpml.extended.impl.SigpmlFactoryImpl"));
    }
    
    public void testShaderWithCustomShadedPatternWithExcludeFilter()
            throws Exception {
            // FIXME: shaded jar should not include references to org/codehaus/* (empty dirs) or org.codehaus.* META-INF
            // files.
    	shaderWithPatternAndExcludeFilter( "org/gemoc/sigpml/extended", "org/gemoc/sigpml/util/*", new File( "target/gemoc_shaded_extended_with_excludefilter" ),
                               new String[] {} );

        assertTrue(new File( "target/gemoc_shaded_extended_with_excludefilter/org/gemoc/sigpml/extended" ).exists());
        assertFalse(new File( "target/gemoc_shaded_extended_with_excludefilter/org/gemoc/sigpml/extended/util" ).exists());
        assertTrue(new File( "target/gemoc_shaded_extended_with_excludefilter/org/gemoc/sigpml/extended/impl" ).exists());
        assertTrue(FileUtils.fileRead("target/gemoc_shaded_extended_with_excludefilter/org/gemoc/sigpml/extended/SigpmlFactory.java").contains(" org.gemoc.sigpml.extended.impl.SigpmlFactoryImpl"));
    }

   

    private void shaderWithPattern( String shadedPattern, File outputFolder, String[] excludes )
        throws Exception
    {
    	DirectoryShader s = newShader();

        Set<File> set = new LinkedHashSet<File>();

        set.add( new File( "test/resources/src_1" ) );

        //set.add( new File( "src/test/jars/plexus-utils-1.4.1.jar" ) );

        List<Relocator> relocators = new ArrayList<Relocator>();

        relocators.add( new SimpleRelocator( "org/gemoc/sigpml", shadedPattern, null, Arrays.asList( excludes ) ) );

       // List<ResourceTransformer> resourceTransformers = new ArrayList<ResourceTransformer>();

        //resourceTransformers.add( new ComponentsXmlResourceTransformer() );

        List<Filter> filters = new ArrayList<Filter>();

        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setInputFolders( set );
        shadeRequest.setOutputFolder( outputFolder );
        shadeRequest.setFilters( filters );
        shadeRequest.setRelocators( relocators );
       // shadeRequest.setResourceTransformers( resourceTransformers );

        s.shade( shadeRequest );
    }
    
    private void shaderWithPatternAndExcludeFilter( String shadedPattern, String excludeFilter,  File outputFolder, String[] excludes )
            throws Exception
        {
        	DirectoryShader s = newShader();

            Set<File> set = new LinkedHashSet<File>();

            set.add( new File( "test/resources/src_1" ) );

            //set.add( new File( "src/test/jars/plexus-utils-1.4.1.jar" ) );

            List<Relocator> relocators = new ArrayList<Relocator>();

            relocators.add( new SimpleRelocator( "org/gemoc/sigpml", shadedPattern, null, Arrays.asList( excludes ) ) );

           // List<ResourceTransformer> resourceTransformers = new ArrayList<ResourceTransformer>();

            //resourceTransformers.add( new ComponentsXmlResourceTransformer() );

            List<Filter> filters = new ArrayList<Filter>();
            
            filters.add( new SimpleFilter(set, Collections.<String> emptySet(), Collections.singleton( excludeFilter )));

            ShadeRequest shadeRequest = new ShadeRequest();
            shadeRequest.setInputFolders( set );
            shadeRequest.setOutputFolder( outputFolder );
            shadeRequest.setFilters( filters );
            shadeRequest.setRelocators( relocators );
           // shadeRequest.setResourceTransformers( resourceTransformers );

            s.shade( shadeRequest );
        }

    private static DirectoryShader newShader()
    {
    	DirectoryShader s = new DirectoryShader();

        //s.enableLogging( new ConsoleLogger( Logger.LEVEL_INFO, "TEST" ) );

        return s;
    }
    
    
    private boolean contains(File file, String searchedString) throws IOException{
    	FileUtils.fileRead(file).contains(searchedString);
    	return FileUtils.fileRead(file).contains(searchedString);
    }

}
