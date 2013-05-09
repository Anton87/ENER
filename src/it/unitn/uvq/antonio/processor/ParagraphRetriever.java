package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.entity.TopicsRepository;
import it.unitn.uvq.antonio.file.FileUtils;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPIException;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParagraphRetriever extends AbstractProcessor {

	@Override
	public Object process() {
		@SuppressWarnings("unchecked")		
		Map<String, Object> m = (Map<String, Object>) ctx;
		String mid = (String) m.get("mid");		
		outFile = (String) m.get("outFile");
		
		paragraph = getWikiAbstract(mid);
		outFile += "/abstract";
		
		if (paragraph != null && !paragraph.isEmpty()) {
			write(paragraph, outFile + "/" + paragraph.hashCode());
		}
		
		return m;		
	}
	
	private String getWikiAbstract(String mid) {
		assert mid != null;
		
		String paragraph = null;		
		try {
			paragraph = TopicsRepository.getWikiAbstract(mid);
		} catch (TopicAPIException e) {
			System.err.println("Paragraph Retrieving error.");
		}
		return paragraph;		
	}

	@Override
	public List<Object> iterate(Object ctx) {
		if (ctx == null) throw new NullPointerException("ctx: null");
		
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("abstract", paragraph);
		m.put("outFile", outFile);
		return Arrays.asList((Object) m);
	}
	
	private void write(String text, String outFile) {
		assert text != null;
		assert outFile != null;
		
		getParentFile(outFile).mkdirs();
		FileUtils.writeText(text, outFile);
	}
	
	private File getParentFile(String pathname) { 
		assert pathname != null;
		
		return new File(pathname).getParentFile();
	}
	
	private String paragraph;
	
	private String outFile;
	
}
