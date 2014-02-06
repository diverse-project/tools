/* $Id:$ 
 * Licence  : EPL 
 * Copyright: IRISA/INRIA
 * Authors  : 
 *            Francois Fouquet
 *            Didier Vojtisek
 * 
 */
package fr.inria.diverse.image_tools.mavenplugin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * This class implement a maven plugin that convert an image file into another format
 *  
 * @goal convert
 * @phase generate-sources
 * @author <a href="mailto:didier.vojtisek@inria.fr">Didier Vojtisek</a>
 * @version $Id$
 *
 */
public class ImageConverterMojo extends AbstractMojo {

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected java.util.List remoteRepos;
    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected org.apache.maven.artifact.repository.ArtifactRepository local;
    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;
    /**
     * Input image file
     *
     * @parameter
     * @required
     */
    private File input;
    /**
     * Output image file
     *
     * @parameter
     * @required
     */
    private File output;


    /**
     * format of the outputfile
     *
     * @parameter 
     * @required
     */
    private String outputFormat;
    
    
   
    public void execute() throws MojoExecutionException, MojoFailureException {

        //org.apache.log4j.BasicConfigurator.configure();
        //System.out.println("K2CompilerMojo.execute");
        this.getLog().info("convert "+input.getAbsolutePath()+" to "+output.getAbsolutePath()+" as a "+outputFormat+" file");
        /* CHECK IF GENERATION IF OK */
        
        checkFile(input.getAbsolutePath().toString());
        try {
	        //Read the file to a BufferedImage
	        BufferedImage image = ImageIO.read(input);
	
	        //Write the image to the destination in the correct format
	        
	        if(outputFormat.toLowerCase().equals("bmp") || outputFormat.toLowerCase().equals("jpg")|| outputFormat.toLowerCase().equals("jpeg")){
	        	// remove possible transparent channel
	        	image = fillTransparentPixels(image, Color.black);
	        }
	        output.getParentFile().mkdirs();
			boolean result = ImageIO.write(image, outputFormat, output);
			if(!result){
				this.getLog().error("No appropriate writer found for format "+outputFormat);
				StringBuilder sb = new StringBuilder();
				for(String s : ImageIO.getWriterFormatNames()){
					sb.append(s+", ");
				}
				this.getLog().info("Accepted formats: "+sb.toString());
				
			}
		} catch (IOException e) {
			this.getLog().error(e.getMessage(),e);
		}
        
    }

    protected boolean checkFile(String filePath) throws MojoExecutionException {
        File file = new File(filePath);
        if (!file.exists()) {
            this.getLog().error("File not found : " + filePath);
            throw new MojoExecutionException("File not found : " + filePath);

        }
        return true;
    }
    
    public static BufferedImage fillTransparentPixels( BufferedImage image, Color fillColor ) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image2.createGraphics();
		g.setColor(fillColor);
		g.fillRect(0,0,w,h);
		g.drawRenderedImage(image, null);
		g.dispose();
		return image2;
    }
        
}
