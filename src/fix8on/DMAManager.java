package fix8on;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.functions.Mapper;

import quickfix.Message;

public abstract class DMAManager {

    protected ConcurrentMap<String, List<Mapper<Message, Message>>> filters;

	protected abstract void initFilters(List<Map<String,String>> clientCfgs);

}
