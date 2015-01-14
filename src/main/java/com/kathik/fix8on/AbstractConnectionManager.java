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
public abstract class AbstractConnectionManager implements Application {

    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

    protected final SessionSettings settings;
    protected final Map<String, Session> liveSessions = new HashMap<>();
    protected final BlockingQueue<FIX8ONMsg> sendToOtherSide = new LinkedBlockingQueue<>();
    protected final BlockingQueue<FIX8ONMsg> sendToExternal = new LinkedBlockingQueue<>();
    protected final SessionFactory sessionFactory;
    protected FilterChain fChain;

    public AbstractConnectionManager(SessionSettings settings_) {
        settings = settings_;
        sessionFactory = new DefaultSessionFactory(this, new MemoryStoreFactory(), new SLF4JLogFactory(settings));
    }

    public void init() {
        DMATransformTask task = new DMATransformTask(sendToExternal, this);
        pool.execute(task);
    }

    public void shutdown() {
        pool.shutdown();
    }

    public SessionSettings getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return "ClientsideManager{" + "messageFactory=" + messageFactory + ", settings=" + settings + ", liveSessions=" + liveSessions + ", sendToMktside=" + sendToOtherSide + ", sendToClient=" + sendToExternal + ", sessionFactory=" + sessionFactory + '}';
    }

    public abstract void createFilters(final String sessID, final Map<String, String> m);

    @Override
    public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // NOOP for now      
    }

    @Override
    public void toAdmin(Message arg0, SessionID arg1) {
        // Mostly NOOP
    }

    @Override
    public void onCreate(SessionID arg0) {
        // NOOP for now
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
        // FIXME Should we do something else here...?
        liveSessions.remove(sessID.toString());
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
    public abstract void fromApp(final Message msg, final SessionID sessID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType;

    @Override
    public abstract void toApp(Message arg0, SessionID arg1) throws DoNotSend;

    /**
     * The receive handoff is the messages that are being _received_ by the CSM
     * and being sent back to the client
     *
     * @return
     */
    public BlockingQueue<FIX8ONMsg> getRecvHandoff() {
        return sendToExternal;
    }

    /**
     * The send handoff is the messages that are being _sent_ by the CSM on
     * behalf of the client and passed on to the Marketside engine
     *
     * @return
     */
    public BlockingQueue<FIX8ONMsg> getSendHandoff() {
        return sendToOtherSide;
    }

}
