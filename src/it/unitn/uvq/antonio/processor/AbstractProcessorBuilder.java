package it.unitn.uvq.antonio.processor;

import java.util.ArrayList;
import java.util.List;

public class AbstractProcessorBuilder {
	
	AbstractProcessorBuilder(Class<? extends AbstractProcessor> procType) {
		if (procType == null) throw new NullPointerException("proc: null");
		
		this.procType = procType;		
	}
	
	AbstractProcessorBuilder(Class<? extends AbstractProcessor> procType, Object ctx) {
		if (procType == null) throw new NullPointerException("proc: null");
		if (ctx == null) throw new NullPointerException("ctx: null");
		
		this.procType = procType;
		this.ctx = ctx;
	}
	
	AbstractProcessorBuilder addNextProcType(AbstractProcessorBuilder builder) {
		if (builder == null) throw new NullPointerException("procType: null");
		
		nextBuilders.add(builder);
		return this;
	}
	
	AbstractProcessor build() { 
		AbstractProcessor proc = null;		
			try {
				proc = procType.newInstance();
				if (ctx != null) {
					proc.setCtx(ctx);					
				}
				for (AbstractProcessorBuilder builder : nextBuilders) {
					proc.addProcessorType(builder);
				}
			} catch (InstantiationException e) {
				System.err.println(e.getMessage());
			} catch (IllegalAccessException e) {
				System.err.println(e.getMessage());
			}
		return proc;
	}

	private Object ctx = null;
	
	final Class<? extends AbstractProcessor> procType;	
	
	private List<AbstractProcessorBuilder> nextBuilders = new ArrayList<>();

}
