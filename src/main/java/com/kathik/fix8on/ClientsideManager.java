package com.kathik.fix8on;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DefaultSessionFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.MemoryStoreFactory;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SLF4JLogFactory;
import quickfix.Session;
import quickfix.SessionFactory;
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
public class ClientsideManager extends AbstractConnectionManager {

    public ClientsideManager(SessionSettings settings_) {
        super(settings_);
    }

    @Override
    public void createFilters(final String sessID, final Map<String, String> m) {
        fChain = new FilterChain();

        // Set up symbology handling
        if (m.get("symbol_from") != null) {
            fChain.add(sessID, SymbolTransformer.of(m.get("symbol_from")));
        }
    }

    /**
     * This method is where new order requests, cancels etc from the clients
     * turn up.
     *
     * @param msg
     * @param sessID
     * @throws quickfix.FieldNotFound
     * @throws quickfix.IncorrectDataFormat
     * @throws quickfix.IncorrectTagValue
     * @throws quickfix.UnsupportedMessageType
     */
    @Override
    public void fromApp(final Message msg, final SessionID sessID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (liveSessions.get(sessID.toString()) == null) {
//            throw new DoNotSend();
            // FIXME Do error condition
        }
        FIX8ONMsg m = FIX8ONMsg.of(msg, sessID);

        try {
            // Put in the received-from-client queue & pass on to Marketside 
            sendToOtherSide.put(m);
        } catch (InterruptedException e) {
        }
    }


    @Override
    public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
        // NOOP for now - do we need this to send?
    }

}
