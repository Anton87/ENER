package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.file.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractProcessor implements Processor, CtxIterator {
	

	@SuppressWarnings("unchecked")
	public AbstractProcessor run() { 
		process();
		Map<String, Object> ctx_m = (Map<String, Object>) ctx;
		for (AbstractProcessorBuilder type : procTypes) {
			for (Object ctxValue : iterate(ctx)) {
				
				/* Create a new map. */
				Map<String, Object> newCtx_m = new HashMap<String, Object>(ctx_m);
				
				newCtx_m.putAll((Map<String, Object>) ctxValue);
				
				AbstractProcessor proc = type.build();
				proc.prev = this;
				nextProcessors.add(proc);
				
				proc.setCtx(newCtx_m);
				proc.run();					
				
			}
		}
		return this;
	}
	
	public AbstractProcessor setCtx(Object ctx) {
		if (ctx == null) throw new NullPointerException("ctx: null");
		
		this.ctx = ctx;
		return this;
	}
	
	public AbstractProcessor addProcessorType(AbstractProcessorBuilder procType) { 
		if (procType == null) throw new NullPointerException("procType: null");
		
		procTypes.add(procType);		
		return this;
	}
	
	void writeText(String text, String outFile) { 
		assert text != null;
		assert outFile != null;
		
		getParentFile(outFile).mkdir();
		FileUtils.writeText(text, outFile);		
	}
	
	void writeObject(Object o, String outFile) { 
		assert o != null;
		assert outFile != null;
		
		getParentFile(outFile).mkdirs();
		FileUtils.writeObject(o, outFile);
	}
	
	private File getParentFile(String pathname) { 
		assert pathname != null;
		
		return new File(pathname).getParentFile();
	}
	
	protected final List<AbstractProcessorBuilder> procTypes = new ArrayList<>();
	
	protected final List<AbstractProcessor> nextProcessors = new ArrayList<>();
	
	protected Object ctx = null;
	
	protected AbstractProcessor prev = null;

}
