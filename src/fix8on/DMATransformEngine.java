package fix8on;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.functions.Mapper;

import quickfix.Message;

public final class DMATransformEngine {

    private final ConcurrentMap<String, FilterChain> csFilters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, FilterChain> mktFilters = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService stpe = Executors.newScheduledThreadPool(2); 
    
    private final BlockingQueue<FIX8ONMsg> clbq; // Messages from clients on way to market
    private final BlockingQueue<FIX8ONMsg> mlbq; // Messages from market on way back to client

	private final MarketsideManager msm;
	private final ClientsideManager csm;

	public DMATransformEngine(MarketsideManager msm_, ClientsideManager csm_) {
		msm = msm_;
		csm = csm_;
		mlbq = msm.getHandoff();
		clbq = csm.getHandoff();
	}
	
	void init(List<Map<String,String>> cfgs) {
		cfgs.forEach(j -> {mktFilters.put(Utils.createUUID(j), Utils.createMarketsideFilters(j));});
		cfgs.forEach(j -> {csFilters.put(Utils.createUUID(j), Utils.createClientsideFilters(j));});
		System.out.println(mktFilters);
		System.out.println(csFilters);
	}

	
	private static class DMATransformTask {
		
		private final FilterChain fc;
		
		public DMATransformTask(FilterChain fc_) {
			fc = fc_;
		}
		
		public void doAction(FIX8ONMsg m) {
			// Apply filter chain for this client
//			filters.get(m.getUuid()).forEach(t -> {m.setCurrent(t.map(m.getCurrent()));});
			fc.getTransforms().forEach(t -> {m.setCurrent(t.map(m.getCurrent()));});
			
			// Update any caches / statefulness
			
			// And handoff to the marketside manager
		}
		
	}


	public void start() {
		// TODO Auto-generated method stub
		
	}

	
}
