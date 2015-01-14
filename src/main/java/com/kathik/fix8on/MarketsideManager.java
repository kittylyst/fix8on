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
public class MarketsideManager implements Application {

    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private final BlockingQueue<FIX8ONMsg> recvfromEngine = new LinkedBlockingQueue<>();
    private final BlockingQueue<FIX8ONMsg> sendToEngine = new LinkedBlockingQueue<>();
    private final SessionSettings settings;
    private final Map<String, Session> liveSessions = new HashMap<>();
    private Map<String, List<Function<Message, Message>>> filters;
    private final SessionFactory sessionFactory;

    public MarketsideManager(SessionSettings settings_) {
        settings = settings_;
        sessionFactory = new DefaultSessionFactory(this, new MemoryStoreFactory(), new SLF4JLogFactory(settings));
    }

    public SessionSettings getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return "MarketsideManager{" + "messageFactory=" + messageFactory + ", handoff=" + recvfromEngine + ", settings=" + settings + ", liveSessions=" + liveSessions + '}';
    }

    @Override
    public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // NOOP
    }

    @Override
    public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        // App logic goes here...
        // Fills etc coming back from market

    }

    @Override
    public void onCreate(SessionID arg0) {
        // TODO Auto-generated method stub
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
    public void onLogout(SessionID sessID) {
        // The FIX session has gone away, we should do something about this...
        liveSessions.remove(sessID.toString());
    }

    @Override
    public void toAdmin(Message arg0, SessionID arg1) {
        // Mostly NOOP
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
        filters.get(m.getUuid()).stream().forEach(t -> m.setCurrent(t.apply(m.getCurrent())));
        liveSessions.get(m.getUuid()).send(m.getCurrent());

    }

    public BlockingQueue<FIX8ONMsg> getRecvHandoff() {
        return recvfromEngine;
    }
    public BlockingQueue<FIX8ONMsg> getSendHandoff() {
        return sendToEngine;
    }

}
