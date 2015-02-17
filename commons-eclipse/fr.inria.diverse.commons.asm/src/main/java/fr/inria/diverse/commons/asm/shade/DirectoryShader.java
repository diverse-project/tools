package fr.inria.diverse.commons.asm.shade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.IOUtil;
import org.objectweb.asm.commons.Remapper;

import fr.inria.diverse.commons.asm.shade.filter.Filter;
import fr.inria.diverse.commons.asm.shade.relocation.Relocator;

public class DirectoryShader {

	public void shade(ShadeRequest shadeRequest) throws IOException {

		Set<String> resources = new HashSet<String>();

		shadeRequest.getOutputFolder().mkdirs();

		for (File inputFolder : shadeRequest.getInputFolders()) {
			shadeFolder(shadeRequest, resources, inputFolder, inputFolder);
		}

	}

	/**
	 * 
	 * @param shadeRequest
	 * @param inputFolder
	 *            is the base folder used to compute the actual name of the
	 *            package
	 * @param folder
	 *            that must be shaded. It must be contained by the inputFolder
	 * @throws IOException
	 */
	protected void shadeFolder(ShadeRequest shadeRequest,
			Set<String> resources, File inputFolder, File folder)
			throws IOException {

		RelocatorRemapper remapper = new RelocatorRemapper(
				shadeRequest.getRelocators());

		List<Filter> folderFilters = getFilters(inputFolder,
				shadeRequest.getFilters());

		for (File file : folder.listFiles()) {
			// remove the inputFolder part of the path to get the name
			
			String name = file.getPath().substring(
					inputFolder.getPath().length()+1).replace('\\', '/');

			if (file.isDirectory()) {
				shadeFolder(shadeRequest, resources, inputFolder, file);
			} else if (!isFiltered(folderFilters, name)) {

				String mappedName = remapper.map(name);

				int idx = mappedName.lastIndexOf('/');
				if (idx != -1) {
					// make sure dirs are created
					String dir = mappedName.substring(0, idx);
					if (!resources.contains(dir)) {
						addDirectory(resources, shadeRequest.getOutputFolder(),
								dir);
					}
				}
				if (name.endsWith(".class")) {
					// duplicates.put( name, jar );
					// addRemappedClass( remapper, jos, jar, name, is );
				} else if ( /* shadeRequest.isShadeSourcesContent() && */name.endsWith(".java")) {
					// Avoid duplicates
					if (resources.contains(mappedName)) {
						continue;
					}

					addJavaSource(resources, shadeRequest.getOutputFolder(), mappedName, file,
							shadeRequest.getRelocators());
				}

			}
		}
	}

	private void addDirectory(Set<String> resources, File outputFolder,
			String name) throws IOException {
		if (name.lastIndexOf('/') > 0) {
			String parent = name.substring(0, name.lastIndexOf('/'));
			if (!resources.contains(parent)) {
				addDirectory(resources, outputFolder, parent);
			}
		}

		File folderToCreate = new File(outputFolder.getCanonicalPath() + '/' + name);

		folderToCreate.mkdirs();

		resources.add(name);
	}

	private void addJavaSource(Set<String> resources, File outputFolder,
			String name, File file, List<Relocator> relocators)
			throws IOException {
		FileInputStream is = new FileInputStream(file);
		String sourceContent = IOUtil.toString(new InputStreamReader(is,
				"UTF-8"));

		for (Relocator relocator : relocators) {
			sourceContent = relocator.applyToSourceContent(sourceContent);
		}
		FileOutputStream os = new FileOutputStream(
				outputFolder.getCanonicalPath() + '/' + name);
		OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
		IOUtil.copy(sourceContent, writer);
		writer.flush();
		is.close();
		os.close();
		resources.add(name);
	}

	private List<Filter> getFilters(File folder, List<Filter> filters) {
		List<Filter> list = new ArrayList<Filter>();

		for (Filter filter : filters) {
			if (filter.canFilter(folder)) {
				list.add(filter);
			}

		}

		return list;
	}

	private boolean isFiltered(List<Filter> filters, String name) {
		for (Filter filter : filters) {
			if (filter.isFiltered(name)) {
				return true;
			}
		}

		return false;
	}

	class RelocatorRemapper extends Remapper {

		private final Pattern classPattern = Pattern.compile("(\\[*)?L(.+);");

		List<Relocator> relocators;

		public RelocatorRemapper(List<Relocator> relocators) {
			this.relocators = relocators;
		}

		public boolean hasRelocators() {
			return !relocators.isEmpty();
		}

		public Object mapValue(Object object) {
			if (object instanceof String) {
				String name = (String) object;
				String value = name;

				String prefix = "";
				String suffix = "";

				Matcher m = classPattern.matcher(name);
				if (m.matches()) {
					prefix = m.group(1) + "L";
					suffix = ";";
					name = m.group(2);
				}

				for (Relocator r : relocators) {
					if (r.canRelocateClass(name)) {
						value = prefix + r.relocateClass(name) + suffix;
						break;
					} else if (r.canRelocatePath(name)) {
						value = prefix + r.relocatePath(name) + suffix;
						break;
					}
				}

				return value;
			}

			return super.mapValue(object);
		}

		public String map(String name) {
			String value = name;

			String prefix = "";
			String suffix = "";

			Matcher m = classPattern.matcher(name);
			if (m.matches()) {
				prefix = m.group(1) + "L";
				suffix = ";";
				name = m.group(2);
			}

			for (Relocator r : relocators) {
				if (r.canRelocatePath(name)) {
					value = prefix + r.relocatePath(name) + suffix;
					break;
				}
			}

			return value;
		}

	}

}
