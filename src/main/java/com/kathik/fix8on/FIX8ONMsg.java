package com.kathik.fix8on;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.SenderCompID;

public final class FIX8ONMsg {

    private final Message initial;
    private Message current;

    private final String uuid;
    private final String sessionID;

	// FIXME Really want the initial and final String representations of
    // these messages for logging & forensics - but looks like no easy way
    // to do this in QF/J
//	private final String inStr;
//	private final String outStr;
    private FIX8ONMsg(Message m, String sessID) {
        current = m;
        // FIXME Need a better way to take a snapshot of the original form of this message
        initial = (Message) m.clone();
        sessionID = sessID;
        uuid = makeUUID(m);
    }

    @Override
    public String toString() {
        return "FIX8ONMsg{" + "initial=" + initial + ", current=" + current + ", uuid=" + uuid + ", sessionID=" + sessionID + '}';
    }
    
    /**
     * This method is anticipated to become more sophisticated over time - to
     * allow different routing rules / limits for a subset of client orders. For
     * now, it's just quick and dirty
     *
     * @param m
     * @return
     */
    private String makeUUID(Message m) {
        try {
            return m.getHeader().getString(SenderCompID.FIELD).toUpperCase();
        } catch (FieldNotFound e) {
        }
        return "XXX";
    }

    public static FIX8ONMsg of(Message m, SessionID id) {
        return new FIX8ONMsg(m, id.toString());
    }

    public Message getCurrent() {
        return current;
    }

    public String getUuid() {
        return uuid;
    }

    public void setCurrent(Message current) {
        this.current = current;
    }
}
