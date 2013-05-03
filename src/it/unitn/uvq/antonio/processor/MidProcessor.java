package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.entity.TopicsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MidProcessor extends AbstractProcessor {

	@Override
	public Object process() {		
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) ctx;
		ntypeID = (String) m.get("ntypeID");
		outFile = (String) m.get("outFile");
		limit = (Integer) m.get("ntypeMax"); 
		mids = TopicsRepository.getMidsByNotableTypeId(ntypeID, limit);	
		return m;
	}

	@Override
	public List<Object> iterate(Object ctx) {
		if (ctx == null) throw new NullPointerException("ctx: null");
		
		List<Object> retValues = new ArrayList<>();
		for (String mid : mids) {
			System.out.println(mid);
			Map<String, Object> n = new HashMap<>();
			n.put("mid", mid);
			n.put("outFile", outFile + ntypeID + mid);
			retValues.add(n);
		}
		return retValues;
	}
	
	private int limit;
	
	private String outFile;

	private String ntypeID;
	
	private List<String> mids;
	
}
