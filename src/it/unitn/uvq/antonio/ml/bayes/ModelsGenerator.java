package it.unitn.uvq.antonio.ml.bayes;

import it.unitn.uvq.antonio.strings.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ModelsGenerator {
	
	private static void codegen(String model_file, String dest_file) {
		assert model_file != null;
		assert dest_file != null;
		
		PrintWriter printer = null;		
		try {
			printer = new PrintWriter(
					new FileOutputStream(dest_file));
			File model = new File(model_file);
			
			printer.println("package it.unitn.uvq.antonio.ml.bayes;");
			printer.println();
			printer.println("import java.io.BufferedReader;");
			printer.println("import java.io.FileNotFoundException;");
			printer.println("import java.io.FileReader;");
			printer.println("import java.io.IOException;");
			printer.println("import java.util.Collections;");
			printer.println("import java.util.HashMap;");
			printer.println("import java.util.Map;");
			printer.println();
			printer.println("public enum Models implements Model {");
			printer.println();
			
			File[] files = model.listFiles();
			for (int i = 0; i < files.length; i++) { 
				File file = files[i];
				System.out.println("topic: " + file.getName());
				String filename = file.getName();
				String fn = filename.substring(0, filename.lastIndexOf("."));
				String topicName = title(fn.replace("_", " "));
				String topicFilepath = new File(MODEL_FILEPATH, filename).toString();
				printer.print("\t" + fn.toUpperCase() + "(\"" + fn + "\", \"" + topicName + "\", \"" + topicFilepath + "\")");
				printer.println(i < files.length - 1 ? "," : ";");
			}
			printer.println();
			printer.println("\tModels(String modelId, String modelName, String modelFile) {");
			printer.println("\t\tif (modelId == null) throw new NullPointerException(\"modelId: null\");");
			printer.println("\t\tif (modelName == null) throw new NullPointerException(\"modelName: null\");");
			printer.println("\t\tif (modelFile == null) throw new NullPointerException(\"modelFile: null\");");
			printer.println();
			printer.println("\t\tthis.modelId = modelId;");
			printer.println("\t\tthis.modelName = modelName;");
			printer.println("\t\tthis.modelFile = modelFile;");
			printer.println("\t\tthis.prob = readProb(modelFile);");
			printer.println("\t\tword2ProbMap = readProbs(modelFile);");
			printer.println("\t}");
			printer.println();
			
			printer.println("\t@Override");
			printer.println("\tpublic String modelName() {");
			printer.println("\t\treturn modelName;");
			printer.println("\t}");
			printer.println();
			
			printer.println("\t@Override");
			printer.println("\tpublic double prob() {");
			printer.println("\t\treturn prob;");
			printer.println("\t}");
			printer.println();
			
			printer.println("\t@Override");
			printer.println("\tpublic double prob(String word) {");
			printer.println("\t\tif (word2ProbMap.containsKey(word)) {");
			printer.println("\t\t\treturn word2ProbMap.get(word);");
			printer.println("\t\t}");
			printer.println("\t\treturn .0;");
			printer.println("\t}");
			printer.println();
			
			printer.println("\tprivate double readProb(String modelFile) {");
			printer.println("\t\tassert modelFile != null;");
			printer.println();
			printer.println("\t\tBufferedReader in = null;");
			printer.println("\t\tdouble prob = .0;");
			printer.println("\t\ttry {");
			printer.println("\t\t\tin = new BufferedReader(");
			printer.println("\t\t\t\tnew FileReader(modelFile));");
			printer.println("\t\t\tString line = in.readLine();");
			printer.println("\t\t\tif (line != null) { prob = Double.parseDouble(line); }");
			printer.println("\t\t} catch (FileNotFoundException e) { ");
			printer.println("\t\t\te.printStackTrace();");
			printer.println("\t\t} catch(IOException e) {");
			printer.println("\t\t\te.printStackTrace();");
			printer.println("\t\t} finally {");
			printer.println("\t\t\ttry {");
			printer.println("\t\t\t\tin.close();");
			printer.println("\t\t\t}catch (IOException e) { }");
			printer.println("\t\t}");
			printer.println("\t\treturn prob;");
			printer.println("}");
			printer.println();
			
			printer.println("\tprivate Map<String, Double> readProbs(String modelFile) { ");
			printer.println("\t\tassert modelFile != null;");
			printer.println();
			printer.println("\t\tMap<String, Double> probs = new HashMap<>();");
			printer.println("\t\tBufferedReader in = null;");
			printer.println("\t\ttry {");
			printer.println("\t\t\tin = new BufferedReader(");
			printer.println("\t\t\t\tnew FileReader(modelFile));");
			printer.println("\t\t\tString line = in.readLine();");
			printer.println("\t\t\tif (line != null) {");
			printer.println("\t\t\t\twhile ((line = in.readLine()) != null) {");
			printer.println("\t\t\t\t\tString[] values = line.split(SEPARATOR);");
			printer.println("\t\t\t\t\tString word = values[0];");
			printer.println("\t\t\t\t\tdouble prob = Double.parseDouble(values[1]);");
			printer.println("\t\t\t\t\tprobs.put(word, prob);");
			printer.println("\t\t\t\t}");
			printer.println("\t\t\t}");
			printer.println("\t\t} catch (FileNotFoundException e) {");
			printer.println("\t\t\te.printStackTrace();");
			printer.println("\t\t} catch (IOException e) {");
			printer.println("\t\t\te.printStackTrace();");
			printer.println("\t\t} finally {");
			printer.println("\t\t\ttry {");
			printer.println("\t\t\t\tin.close();");
			printer.println("\t\t\t} catch (IOException e) { }");
			printer.println("\t\t}");
			printer.println("\t\treturn Collections.unmodifiableMap(probs);");
			printer.println("\t}");
			printer.println();
			
			printer.println("\tprivate final static String SEPARATOR = \"\\t\";");
			printer.println();
			
			printer.println("\tprivate final String modelId;");
			printer.println();
			
			printer.println("\tprivate final String modelName;");
			printer.println();
			
			printer.println("\tprivate final String modelFile;");
			printer.println();
			
			printer.println("\tprivate final double prob;");
			printer.println();
			
			printer.println("\tprivate final Map<String, Double> word2ProbMap;");
			printer.println();
			printer.println("}");			
		} catch (IOException e) {
			
		} finally { 
			printer.close();
		}
	}
	
	private static String title(String str) { 
		assert str != null;
		
		return Strings.upperCaseFirstLetters(str, "\\s+");
	}
	
	private final static String MODEL_FILEPATH = "nb-models";
	
	public static void main(String[] args) { 
		String model_file = args[0];
		String dest_file = args[1];
		
		System.out.println("model_file: \"" + model_file + "\"");
		System.out.println("dest_file: \"" + dest_file + "\"");
		
		codegen(model_file, dest_file);
		
	}

}
