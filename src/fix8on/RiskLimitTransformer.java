package fix8on;

import java.util.functions.Mapper;

import quickfix.Message;

public final class RiskLimitTransformer implements Mapper<Message, Message> {

	public RiskLimitTransformer(long limit) {
		// TODO Auto-generated constructor stub
	}

	public RiskLimitTransformer() {
		this(10_000_000);
	}

	
	@Override
	public Message map(Message t) {
		// TODO Auto-generated method stub
		return null;
	}

}
