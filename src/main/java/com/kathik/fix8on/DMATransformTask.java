/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kathik.fix8on;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author boxcat
 */
public final class DMATransformTask implements Runnable {
    private final FilterChain fc;
    private final BlockingQueue<FIX8ONMsg> q;
    private volatile boolean shutdown = false;

    public DMATransformTask(FilterChain fc_, BlockingQueue<FIX8ONMsg> q_) {
        fc = fc_;
        q = q_;
    }

    public void run() {
        while (!shutdown) {
            try {
                FIX8ONMsg wu = q.poll(100, TimeUnit.MILLISECONDS);
                if (wu != null) {
                    doAction(wu);
                }
            } catch (InterruptedException e) {
                shutdown = true;
            }
        }
    }

    public void doAction(FIX8ONMsg m) {
        // Apply filter chain for this client
        //			filters.get(m.getUuid()).forEach(t -> {m.setCurrent(t.map(m.getCurrent()));});
        fc.getTransforms().forEach((t) -> {
            m.setCurrent(t.apply(m.getCurrent()));
        });
        // Update any caches / statefulness
        // Remap sender & target comp ids
        // And handoff to the otherside manager
    }
    
}
