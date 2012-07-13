package fix8on;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.functions.Mapper;

import quickfix.Message;

public final class Utils {

	public static String createUUID(Map<String, String> map) {
		return map.get("SenderCompID");
	}

	public static List<Mapper<Message, Message>> createFilters(Map<String, String> map) {
		return new ArrayList<>();
	}
	
}
