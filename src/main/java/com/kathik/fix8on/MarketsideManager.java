package com.kathik.fix8on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
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
import quickfix.MessageStore;
import quickfix.MessageStoreFactory;
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
public class MarketsideManager extends AbstractConnectionManager {

    public MarketsideManager(SessionSettings settings_) {
        super(settings_);
    }

    @Override
    public void createFilters(final String sessID, final Map<String, String> m) {
        fChain = new FilterChain();

        // Set up symbology handling
        if (m.get("symbol_from") != null) {
            fChain.add(sessID, SymbolTransformer.of(m.get("symbol_from")));
        }

        // Risk limit handling - only done marketside
        if (m.get("risk_limit") == null) {
            fChain.add(sessID, new RiskLimitTransformer());
        } else if (!m.get("risk_limit").equals("OFF")) {
            try {
                long limit = Long.parseLong(m.get("risk_limit"));
                fChain.add(sessID, new RiskLimitTransformer(limit));
            } catch (NumberFormatException nmx) {
                // Warn about misconfigured risk limit and carry on
            }
        }
    }

    @Override
    public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        // App logic goes here...
        // Fills etc coming back from market

    }

    @Override
    public void onLogon(SessionID sessID) {
        try {
            // A wild FIX session appears!
            Session sess = sessionFactory.create(sessID, settings);
            // Save the ID (& use it as a key)
            liveSessions.put(sessID.toString(), sess);

        } catch (ConfigError ex) {
            Logger.getLogger(MarketsideManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public void toApp(Message msg, SessionID sessID) throws DoNotSend {
        // This is where the 
        // Sanity check m against session ID
        if (liveSessions.get(sessID.toString()) == null) {
            throw new DoNotSend();
        }
        FIX8ONMsg m = FIX8ONMsg.of(msg, sessID);

        // Apply filter chain for this client
        // For marketside this is things like risk checks
        fChain.apply(m);

        liveSessions.get(m.getUuid()).send(m.getCurrent());
    }

}
