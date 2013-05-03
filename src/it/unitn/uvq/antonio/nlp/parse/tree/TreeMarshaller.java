package it.unitn.uvq.antonio.nlp.parse.tree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * A tree marsheller class. It performs marshalling of 
 *  a tree on a file.
 *  for later reuse.
 * 
 * @author Antonio Uva 145683
 *
 */
public class TreeMarshaller {
	
	/**
	 * Saves a tree on a file.
	 * 
	 * @param tree The tree to save 
	 * @param outFile The path where to save the tree
	 * @throws NullPointerException if (tree == null || outFile == null)
	 * @throws TreeMarshalException if an occurs while saving the file
	 */
	public static void marshal(TreeBuilder tree, String outFile) throws TreeMarshalException {
		if (tree == null) throw new NullPointerException("tree: null");
		if (outFile == null) throw new NullPointerException("outFile: null");
		List<TreeBuilder> nodes = tree.getNodes();
		writeTrees(nodes, outFile);
	}
	
	
	private static void writeTrees(List<TreeBuilder> trees, String outFile) throws TreeMarshalException{ 
		assert trees != null;
		assert outFile != null;
		
		deleteDirectoryIfExists(outFile);
		mkdirs(outFile);
		for (TreeBuilder tree : trees) { 
			String fp = new File(outFile, tree.hashCode() + ".out").toString();
			ObjectOutput out = null;
			try {
				out = new ObjectOutputStream(
						new FileOutputStream(fp));
				out.writeObject(tree);
			} catch (IOException e) { 
				throw new TreeMarshalException("Tree marshalling error: \"" + outFile + "\"", e);
			} finally {
				try {
					out.close();
				} catch (IOException e) { }
			}			
		}
	}
	
	private static void deleteDirectoryIfExists(String pathname) { 
		assert pathname != null;
		if (existsDirectory(pathname)) {
			deleteDirectory(pathname);
		}
	}
	
	private static boolean existsDirectory(String pathname) { 
		assert pathname != null;
		return new File(pathname).exists();
	}
	
	private static void deleteDirectory(String dirpath) {
		assert dirpath != null;
		deleteFiles(dirpath);
		new File(dirpath).delete();
	}
	
	private static void deleteFiles(String dirpath) {
		assert dirpath != null;
		File dir = new File(dirpath);
		for (File file : dir.listFiles()) { 
			file.delete();
		}
	}
	
	private static boolean mkdirs(String pathname) {
		assert pathname != null;
		return new File(pathname).mkdirs();
	}

}
