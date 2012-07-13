package fix8on;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.SenderCompID;

public final class FIX8ONMsg {

	private final Message initial;
	private final Message current;
	
	private final String uuid;
	
	// FIXME Really want the initial and final String representations of
	// these messages for logging & forensics - but looks like no easy way
	// to do this in QF/J
//	private final String inStr;
//	private final String outStr;
	
	private FIX8ONMsg(Message m) {
		current = m;
		// FIXME Need a better way to take a snapshot of the original form of this message
		initial = (Message) m.clone();
		
		uuid = makeUUID(m);
	}
	
	/**
	 * This method is anticipated to become more sophisticated over time - to allow 
	 * different routing rules / limits for a subset of client orders. For now, it's just
	 * quick and dirty
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

	public static FIX8ONMsg of(Message m) {
		return new FIX8ONMsg(m);
	}
}
