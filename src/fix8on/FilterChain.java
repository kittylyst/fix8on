package fix8on;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.functions.Mapper;

import quickfix.Message;

public class FilterChain {

	private List<Mapper<Message, Message>> filters = new ArrayList<>();
	
	private final Lock lock = new ReentrantLock();

	public void add(Mapper<Message, Message> m) {
		filters.add(m);
	}
	
	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	public List<Mapper<Message, Message>> getTransforms() {
		return filters;
	}

	
}
