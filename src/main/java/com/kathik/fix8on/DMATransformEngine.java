package com.kathik.fix8on;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

    private final BlockingQueue<FIX8ONMsg> msgsOnWayToClient; // Messages from clients on way to market
    private final BlockingQueue<FIX8ONMsg> msgsFromClient; // Messages from market on way back to client

    private final MarketsideManager msm;
    private final ClientsideManager csm;

    public DMATransformEngine(MarketsideManager msm_, ClientsideManager csm_) {
        msm = msm_;
        csm = csm_;
        msgsFromMkt = msm.getSendHandoff();
        msgsOnWayToMkt = msm.getRecvHandoff();

        msgsFromClient = csm.getSendHandoff();
        msgsOnWayToClient = csm.getRecvHandoff();
    }



}
