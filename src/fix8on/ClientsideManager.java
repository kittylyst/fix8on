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
public class ClientsideManager implements Application {

    private DefaultMessageFactory messageFactory = new DefaultMessageFactory();
	
    private ConcurrentMap<String, List<Mapper<Message, Message>>> filters;
    
	public ClientsideManager(SessionSettings settings, List<Map<String, String>> clientCfgs) {
		initFilters(clientCfgs);
	}

	private void initFilters(List<Map<String,String>> clientCfgs) {
		filters = new ConcurrentHashMap<>();
		clientCfgs.forEach(j -> {filters.put(Utils.createUUID(j), Utils.createFilters(j));});
		System.out.println(filters);
	}

	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method is where new order requests, cancels etc from the clients turn up.
	 */
	@Override
	public void fromApp(Message msg, SessionID id) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		
		FIX8ONMsg m = FIX8ONMsg.of(msg);
		// FIXME Sanity check m against session ID
		
		// Lookup filter chain for this client
	
	}

	@Override
	public void onCreate(SessionID arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLogon(SessionID arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLogout(SessionID arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toAdmin(Message arg0, SessionID arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
		// TODO Auto-generated method stub
		
	}

}
