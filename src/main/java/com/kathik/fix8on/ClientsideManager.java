package com.kathik.fix8on;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
public class ClientsideManager implements Application {

    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private final SessionSettings settings;
    private final Map<String, Session> liveSessions = new HashMap<>();
    private final BlockingQueue<FIX8ONMsg> recvfromClient = new LinkedBlockingQueue<>();
    private final BlockingQueue<FIX8ONMsg> sendToClient = new LinkedBlockingQueue<>();
    private final SessionFactory sessionFactory;

    public ClientsideManager(SessionSettings settings_) {
        settings = settings_;
        sessionFactory = new DefaultSessionFactory(this, new MemoryStoreFactory(), new SLF4JLogFactory(settings));
    }

    public SessionSettings getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return "ClientsideManager{" + "messageFactory=" + messageFactory + ", settings=" + settings + ", recvfromClient=" + recvfromClient + ", sendToClient=" + sendToClient + ", sessionFactory=" + sessionFactory + '}';
    }

    @Override
    public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // NOOP		
    }

    /**
     * This method is where new order requests, cancels etc from the clients
     * turn up.
     *
     * @param msg
     * @param sessID
     * @param id
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
            // Put in the received-from-client queue & pass on to DMA Engine
            recvfromClient.put(m);
        } catch (InterruptedException e) {
        }
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

    /**
     * The receive handoff is the messages that are being received _by_ the CSM
     * and being sent back to the client
     *
     * @return
     */
    public BlockingQueue<FIX8ONMsg> getRecvHandoff() {
        return sendToClient;
    }

    /**
     * The send handoff is the messages that are being sent _by_ the CSM on
     * behalf of the client and passed on to the DMA engine
     *
     * @return
     */
    public BlockingQueue<FIX8ONMsg> getSendHandoff() {
        return recvfromClient;
    }

}
