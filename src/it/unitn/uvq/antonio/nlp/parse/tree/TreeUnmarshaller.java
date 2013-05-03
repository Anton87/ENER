package it.unitn.uvq.antonio.nlp.parse.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tree unmarshaller class. It perfoms unmarshalling of 
 *  a tree by a file.
 *  
 * @author Antonio Uva 145683
 *
 */
public class TreeUnmarshaller {
	
	public static TreeBuilder unmarshal(String inFile) throws FileNotFoundException, TreeUnmarshalException {
		if (inFile == null) throw new NullPointerException("inFile: null");
		if (!isDirectory(inFile)) throw new FileNotFoundException("File not found: \"" + inFile + "\".");
		List<TreeBuilder> nodes = readTrees(inFile);
		Collections.sort(nodes, treeNodeNumCmp);
		TreeBuilder tree = restoreTreeStruct(nodes);
		return tree;
	}
	
	private static TreeBuilder restoreTreeStruct(List<TreeBuilder> trees) {
		Map<Integer, TreeBuilder> map = new HashMap<>();		
		for (TreeBuilder tree : trees) { 
			map.put(tree.getNodeNum(), tree);
			TreeBuilder parent = map.get(tree.parentNodeNum);
			if (parent != null) { 
				parent.addChild(tree);
			}
		}
		return map.get(1);
	}
	
	private static List<TreeBuilder> readTrees(final String inFile) throws TreeUnmarshalException {
		assert inFile != null;
		List<TreeBuilder> trees = new ArrayList<>();
		File dir = new File(inFile);
		for (File file : dir.listFiles()) { 
			String fp = file.getPath();
			ObjectInput in = null;
			try {
				in = 
						new ObjectInputStream(
								new FileInputStream(fp));
				TreeBuilder tree = (TreeBuilder) in.readObject();
				trees.add(tree);
			} catch (FileNotFoundException e) {
				throw new TreeUnmarshalException("File not found: \"" + fp + "\"", e);
			} catch (ClassNotFoundException e) { 
				throw new TreeUnmarshalException("Class not found: \"" + TreeBuilder.class.getName() + "\"", e);	
			} catch (IOException e) {
				throw new TreeUnmarshalException("File reading error: \"" + fp + "\"", e);
			}  finally {
				try {
					in.close();
				} catch (IOException e) { }
			}
		}
		return trees;
	}
	
	private static boolean isDirectory(final String filepath) {
		return new File(filepath).isDirectory();
	}
	
	private final static Comparator<TreeBuilder> treeNodeNumCmp = new Comparator<TreeBuilder>() {
		@Override
		public int compare(TreeBuilder t1, TreeBuilder t2) {
			return t1.getNodeNum() - t2.getNodeNum();
		} 
		
	};

}
