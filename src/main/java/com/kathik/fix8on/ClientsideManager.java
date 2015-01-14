package com.kathik.fix8on;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
 * This application only deals with a single version of FIX - FIX 4.4 for
 * simplicity.
 *
 * @author boxcat
 *
 */
public class ClientsideManager implements Application {

    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private final SessionSettings settings;
    private final BlockingQueue<FIX8ONMsg> handoff = new LinkedBlockingQueue<>();

    public ClientsideManager(SessionSettings settings_) {
        settings = settings_;
    }

    public SessionSettings getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return "ClientsideManager{" + "messageFactory=" + messageFactory + ", settings=" + settings + ", handoff=" + handoff + '}';
    }
    
    @Override
    public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // NOOP		
    }

    /**
     * This method is where new order requests, cancels etc from the clients
     * turn up.
     * @param msg
     * @param id
     * @throws quickfix.FieldNotFound
     * @throws quickfix.IncorrectDataFormat
     * @throws quickfix.IncorrectTagValue
     * @throws quickfix.UnsupportedMessageType
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
