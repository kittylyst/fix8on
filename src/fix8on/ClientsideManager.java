package fix8on;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.functions.Mapper;

import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;

/**
 * This appliaction only deals with a single version of FIX - FIX 4.4 for simplicity.
 * 
 * @author boxcat
 *
 */
public class ClientsideManager extends DMAManager implements Application {

    private DefaultMessageFactory messageFactory = new DefaultMessageFactory();
	private MarketsideManager handoff;
	
	public ClientsideManager(SessionSettings settings, List<Map<String, String>> clientCfgs) {
		initFilters(clientCfgs);
	}

	public void setOtherside(MarketsideManager mgr) {
		handoff = mgr;
	}
	
	protected void initFilters(List<Map<String,String>> clientCfgs) {
		filters = new ConcurrentHashMap<>();
		clientCfgs.forEach(j -> {filters.put(Utils.createUUID(j), Utils.createClientsideFilters(j));});
//		System.out.println(filters);
	}
	
	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		// NOOP		
	}

	/**
	 * This method is where new order requests, cancels etc from the clients turn up.
	 */
	@Override
	public void fromApp(Message msg, SessionID id) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		
		FIX8ONMsg m = FIX8ONMsg.of(msg);
		// FIXME Sanity check m against session ID
		
		// Apply filter chain for this client
		filters.get(m.getUuid()).forEach(t -> {m.setCurrent(t.map(m.getCurrent()));});
		
		// Update any caches / statefulness
		
		// And handoff to the marketside manager
		// FIXME this has to be done with an LBQ and a proper threadpool handoff
//		handoff.toApp(m.getCurrent(), id);	
	}

	@Override
	public void onCreate(SessionID arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLogon(SessionID arg0) {
		// A wild FIX session appears!
	}

	@Override
	public void onLogout(SessionID arg0) {
		// The FIX session has gone away, we should do something about this...
	}

	@Override
	public void toAdmin(Message arg0, SessionID arg1) {
		// Mostly NOOP
	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
		// TODO Auto-generated method stub
		
	}

}
