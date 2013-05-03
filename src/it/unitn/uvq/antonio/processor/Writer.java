package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.freebase.db.NotableType;

import java.util.HashMap;
import java.util.Map;

public class Writer {
	
	public static void main(String[] args) {
		NotableType ntype = NotableType.EN_MUSICIAN;
		
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("ntypeMax", 5);
		ctx.put("ntypeID", ntype.getId());
		ctx.put("outFile", "/home/antonio/Scrivania");
				
		AbstractProcessorBuilder midProcBuilder = new AbstractProcessorBuilder(MidProcessor.class, ctx);
		AbstractProcessorBuilder pageAbstractBuilder = new AbstractProcessorBuilder(ParagraphRetriever.class);
		AbstractProcessorBuilder sentProcBuilder = new AbstractProcessorBuilder(SentProcessor.class);
		AbstractProcessorBuilder nerProcBuilder = new AbstractProcessorBuilder(NERProcessor.class);
		AbstractProcessorBuilder posProcBuilder = new AbstractProcessorBuilder(POSTaggerProcessor.class);
		AbstractProcessorBuilder treeProcBuilder = new AbstractProcessorBuilder(TreeProcessor.class);
		AbstractProcessorBuilder vecProcBuilder = new AbstractProcessorBuilder(VecProcessor.class);
		// AbstractProcessorBuilder erProcBuilder = new AbstractProcessorBuilder(ERProcessor.class);
		
		midProcBuilder.addNextProcType(pageAbstractBuilder);
		pageAbstractBuilder.addNextProcType(sentProcBuilder);
		sentProcBuilder.addNextProcType(nerProcBuilder);
		sentProcBuilder.addNextProcType(posProcBuilder);
		sentProcBuilder.addNextProcType(treeProcBuilder);
		sentProcBuilder.addNextProcType(vecProcBuilder);
		// treeProcBuilder.addNextProcType(erProcBuilder);
		
		AbstractProcessor midProcessor = midProcBuilder.build();
		
		
		midProcessor.run();
		
	}

}
