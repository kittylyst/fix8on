package com.kathik.fix8on;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.SocketAcceptor;
import quickfix.SocketInitiator;

public final class DMATransformEngine {
    private static final Logger logger = LoggerFactory.getLogger(DMATransformEngine.class);
    
    private final ConcurrentMap<String, FilterChain> csFilters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, FilterChain> mktFilters = new ConcurrentHashMap<>();

    private final ScheduledExecutorService stpe = Executors.newScheduledThreadPool(2);

    private final BlockingQueue<FIX8ONMsg> msgsOnWayToMkt; // Messages from clients on way to market
    private final BlockingQueue<FIX8ONMsg> msgsFromMkt; // Messages from market on way back to client

    private final MarketsideManager msm;
    private final ClientsideManager csm;
    private SocketAcceptor acceptor; // connections from clients
    private SocketInitiator initiator; // sending stuff down to market

    
    public DMATransformEngine(MarketsideManager msm_, ClientsideManager csm_) {
        msm = msm_;
        csm = csm_;
        msgsFromMkt = msm.getHandoff();
        msgsOnWayToMkt = csm.getHandoff();
    }

    @Override
    public String toString() {
        return "DMATransformEngine{" + "csFilters=" + csFilters + ", mktFilters=" + mktFilters + ", stpe=" + stpe + ", msgsOnWayToMkt=" + msgsOnWayToMkt + ", msgsFromMkt=" + msgsFromMkt + ", msm=" + msm + ", csm=" + csm + ", acceptor=" + acceptor + ", initiator=" + initiator + '}';
    }
    
    void init(List<Map<String, String>> cfgs) throws ConfigError {
                // Configure up the acceptor - which will handle the transforms of incoming messages from clients
        MessageStoreFactory msgStoreFactory = new FileStoreFactory(csm.getSettings());
        LogFactory logFactory = new ScreenLogFactory(true, true, true);

        acceptor = new SocketAcceptor(csm, msgStoreFactory, csm.getSettings(),
                logFactory, new DefaultMessageFactory());

        msgStoreFactory = new FileStoreFactory(msm.getSettings());

        initiator = new SocketInitiator(msm, msgStoreFactory, msm.getSettings(),
                logFactory, new DefaultMessageFactory());

        
//        cfgs.forEach(j -> {
//            mktFilters.put(Utils.createUUID(j), Utils.createMarketsideFilters(j));
//        });
//        cfgs.forEach(j -> {
//            csFilters.put(Utils.createUUID(j), Utils.createClientsideFilters(j));
//        });
//		System.out.println(mktFilters);
//		System.out.println(csFilters);
    }


    /**
     * Now fully initialized, this method is used to start accepting connections
     *
     * @throws RuntimeError
     * @throws ConfigError
     */
    public void start() throws RuntimeError, ConfigError {
        initiator.start();
        acceptor.start();
    }

    /**
     * Cleanup method
     */
    public void stop() {
        acceptor.stop();
        initiator.stop();
    }

}
