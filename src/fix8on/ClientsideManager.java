package fix8on;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
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
 * This appliaction only deals with a single version of FIX - FIX 4.4 for
 * simplicity.
 * 
 * @author boxcat
 * 
 */
public class ClientsideManager implements Application {

    private DefaultMessageFactory messageFactory = new DefaultMessageFactory();
	private final SessionSettings settings;
	private final BlockingQueue<FIX8ONMsg> handoff = new LinkedBlockingQueue<>();
	
	public ClientsideManager(SessionSettings settings_) {
		settings = settings_;
	}

	
	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		// NOOP		
	}

    /**
     * This method is where new order requests, cancels etc from the clients
     * turn up.
     */
    @Override
	public void fromApp(Message msg, SessionID id) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		
		FIX8ONMsg m = FIX8ONMsg.of(msg, id);
		// FIXME Sanity check m against session ID
		
		try {
			handoff.put(m);
		} catch (InterruptedException e) {
		}	
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


	public BlockingQueue<FIX8ONMsg> getHandoff() {
		return handoff;
	}

}
