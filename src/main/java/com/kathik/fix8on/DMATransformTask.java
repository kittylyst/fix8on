/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kathik.fix8on;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.SessionID;

/**
 *
 * @author boxcat
 */
public final class DMATransformTask implements Runnable {
    private final BlockingQueue<FIX8ONMsg> q;
    private final Application app;
    private volatile boolean shutdown = false;

    public DMATransformTask(BlockingQueue<FIX8ONMsg> q_, Application sender) {
        q = q_;
        app = sender;
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
        try {
            app.toApp(m.getCurrent(), new SessionID(m.getUuid()));
        } catch (DoNotSend ex) {
            Logger.getLogger(DMATransformTask.class.getName()).log(Level.INFO, null, ex);
        }
    }
    
}
