package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.file.FileUtils;
import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.google.common.base.Joiner;

public class AliasFinder {
	
	public AliasInfo compute() {
		assert name != null;
		assert text != null;
		
		List<String> nameParts = Arrays.asList(name.split("\\s+"));
		//System.out.println("tokens(\"" + name + "\"): " + nameParts);
		
		List<Triple<String, Integer, Integer>> tokens = Tokenizer.getInstance().tokenize(text);
		
		Map<String, Integer> m = new HashMap<>();
		
		
		int tokNum = 0;
		List<String> buffer = new ArrayList<>();
		for (Triple<String, Integer, Integer> token : tokens) {
			String tok = token.first();
			if (nameParts.contains(tok)) {
				buffer.add(tok);
				//System.out.println("Occurrence of " + tok + " found at pos: " + tokNum + ".");
			} else {
				if (!buffer.isEmpty()) {
					String str = Joiner.on(" ").join(buffer);
					
 					if (name.startsWith(str) || name.endsWith(str)) {
						int count = m.containsKey(str) ? m.get(str) : 0;   
						m.put(str, count + 1);
					}					
					buffer.clear();
				}
			}
			tokNum++;
		}
		
		
		Set<String> keys = m.keySet();
		for (String alias : keys) {
			for (String otherAlias : keys) {
				if (!alias.equals(otherAlias) && otherAlias.contains(alias)) {
					m.put(alias, m.get(alias) + 1);
				}
			}
		}
		
		String maxFreqAlias = name;
		int max = 0;
		for (String alias : m.keySet()) {
			int freq = m.get(alias);
			//System.out.println("alias: \"" + alias + "\", freq(alias): " + freq);
			if (freq > max) {
				maxFreqAlias = alias;
				max = freq;
			}					
		}
		int counts = 0;
		for (Integer freq : m.values()) { counts += freq; }
		
		return new AliasInfo(maxFreqAlias, (double) max / counts);
	}
	
	public AliasFinder setName(String name) {
		if (name == null) throw new NullPointerException("name is null");
		
		this.name = name;
		return this;
	}
	
	public AliasFinder setText(String text) {
		if (text == null) throw new NullPointerException("text is null");
		
		this.text = text;
		return this;
	}
	
	public static void main(String[] args) { 
		test("John Lennon");
		test("Yoko Ono");
		test("Al Green");
		test("Britney Spears");
		test("Tarra White");
	}
	
	static class AliasInfo {
		
		private AliasInfo(String topCandidate, double score) {
			assert topCandidate != null;
			assert score >= .0 && score < 1;
			
			this.topCandidate = topCandidate;
			this.score = score;
		}
		
		public String getTopCandidate() {
			return topCandidate;			
		}
		
		public double getScore() {
			return score;
		}		
		
		private final String topCandidate;
		
		private final double score;
		
		
		
	}
	
	/*
	private static void testLanaDelRey() {
		String name = "Lana Del Rey";
		
		String text = "Elizabeth Woolridge Grant (born June 21, 1986), better known by her stage name Lana Del Rey, is an American singer-songwriter. Del Rey started performing in clubs in New York City at the age of 18 and she signed her first recording contract when she was 20 years old with 5 Points Records, releasing her first digital album Lana Del Ray a.k.a. Lizzy Grant in January 2010. Del Rey bought herself out of the contract with 5 Points Records in April 2010. She signed a joint contract with Interscope, Polydor, and Stranger Records in October 2011.";
		
		AliasDetector detector = new AliasDetector();
		
		String alias = detector.getAlias(name, text);
		
		System.out.println("alias: " + alias);
		
	}
	*/
	
	private static void test(String name) { 
		String path = "/home/antonio/workspace/abstracts/" + name.charAt(0) + "/" + URLEncoder.encode(name.replaceAll(" ", "_"));
		String text = FileUtils.readText(path);
		
		AliasFinder finder = new AliasFinder();
		finder.setName(name);
		finder.setText(text);
		
		AliasInfo info = finder.compute();
		System.out.format("name: \"%s\", alias: \"%s\", score: %.2f%n", name, info.getTopCandidate(), info.getScore());		
 	}
	
	private static void testLeonardoDaVinci() {
		String name = "Leonardo da Vinci";
		
		String text = "";
		text += "Leonardo di ser Piero da Vinci (Italian pronunciation: [leoˈnardo da ˈvintʃi] About this sound pronunciation (help·info)) (April 15, 1452 – May 2, 1519, Old Style) was an Italian Renaissance polymath: painter, sculptor, architect, musician, mathematician, engineer, inventor, anatomist, geologist, cartographer, botanist, and writer. His genius, perhaps more than that of any other figure, epitomized the Renaissance humanist ideal. Leonardo has often been described as the archetype of the Renaissance Man, a man of \"unquenchable curiosity\" and \"feverishly inventive imagination\".[1] He is widely considered to be one of the greatest painters of all time and perhaps the most diversely talented person ever to have lived.[2] According to art historian Helen Gardner, the scope and depth of his interests were without precedent and \"his mind and personality seem to us superhuman, the man himself mysterious and remote\".[1] Marco Rosci states that while there is much speculation about Leonardo, his vision of the world is essentially logical rather than mysterious, and that the empirical methods he employed were unusual for his time.";
		text += "Born out of wedlock to a notary, Piero da Vinci, and a peasant woman, Caterina, at Vinci in the region of Florence, Leonardo was educated in the studio of the renowned Florentine painter, Verrocchio. Much of his earlier working life was spent in the service of Ludovico il Moro in Milan. He later worked in Rome, Bologna and Venice, and he spent his last years in France at the home awarded him by Francis I.";
		text += "Leonardo was, and is, renowned[2] primarily as a painter. Among his works, the Mona Lisa is the most famous and most parodied portrait[4] and The Last Supper the most reproduced religious painting of all time, with their fame approached only by Michelangelo's The Creation of Adam.[1] Leonardo's drawing of the Vitruvian Man is also regarded as a cultural icon,[5] being reproduced on items as varied as the euro, textbooks, and T-shirts. Perhaps fifteen of his paintings survive, the small number because of his constant, and frequently disastrous, experimentation with new techniques, and his chronic procrastination.[nb 2] Nevertheless, these few works, together with his notebooks, which contain drawings, scientific diagrams, and his thoughts on the nature of painting, compose a contribution to later generations of artists rivalled only by that of his contemporary, Michelangelo.";
		text += "Leonardo is revered[2] for his technological ingenuity. He conceptualised a helicopter, a tank, concentrated solar power, a calculator,[6] and the double hull, and he outlined a rudimentary theory of plate tectonics. Relatively few of his designs were constructed or were even feasible during his lifetime,[nb 3] but some of his smaller inventions, such as an automated bobbin winder and a machine for testing the tensile strength of wire, entered the world of manufacturing unheralded.[nb 4] He made important discoveries in anatomy, civil engineering, optics, and hydrodynamics, but he did not publish his findings and they had no direct influence on later science.";
		
		AliasFinder finder = new AliasFinder();
		finder.setName(name);
		finder.setText(text);
		
		AliasInfo info = finder.compute();
		
		System.out.format("name: \"%s\", alias: \"%s\", score: %.2f%n", name, info.getTopCandidate(), info.getScore());		
		
	}
	
	private static void testAlGreen() {
		String name = "Al Green";
		
		String text = "Albert Greene (born April 13, 1946),[1] better known as Al Green or Reverend Al Green, is an American singer, better known for scoring a series of soul hit singles in the early 1970s, including \"Tired of Being Alone\", \"I'm Still In Love With You\", \"Love and Happiness\" and his signature song, \"Let's Stay Together\".[2] Inducted to the Rock and Roll Hall of Fame in 1995, Green was referred to on the museum's site as being \"one of the most gifted purveyors of soul music\".[2] Green was included in the Rolling Stone list of the 100 Greatest Artists of All Time, ranking at No. 66";
		
		AliasFinder finder = new AliasFinder();
		finder.setName(name);
		finder.setText(text);
		
		AliasInfo info = finder.compute();
		
		System.out.format("name: \"%s\", alias: \"%s\", score: %.2f%n", name, info.getTopCandidate(), info.getScore());		

		
		
	}
	
	private String name;
	
	private String text;	

}
