package it.unitn.uvq.antonio.nlp.parse;

import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeTransformer;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;

/**
 * Parses a sentence and returns its parse tree.
 * 
 * @author Antonio Uva 145683
 *
 */
public class Parser {
	
	/**
	 * Returns this singleton parser instance.
	 * 
	 * @return This parser instance
	 */
	public static Parser getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Return a String representing the parse tree of the sentence as
	 *  a bracketed list.
	 *  
	 * @param str The string to parse
	 * @return The string pare tree
	 * @throws NullPointerException if (str == null)
	 */
	public Tree parse(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		List<CoreLabel> tokens = tokenize(str);
		edu.stanford.nlp.trees.Tree tree = parser.apply(tokens);
		return TreeTransformer.transform(tree).build();
	}
	
	private List<CoreLabel> tokenize(final String str) {
		assert str != null;
		
		Tokenizer<CoreLabel> tokenizer =
				tokenizerFactory.getTokenizer(
						new StringReader(str));
		return tokenizer.tokenize();
	}
	
	private final static Parser INSTANCE = new Parser();
	
	private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";	
		
	private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");
	
	// private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(false, true);
		
	private final LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);
	
	
	public static void main(String[] args) { 
		String str = "Andy Warhol (August 6, 1928 – February 22, 1987) was an American artist who was a leading figure in the visual art movement known as pop art.";
		
		Tree tree = Parser.getInstance().parse(str);
		
		System.out.println(tree);
	}

}
