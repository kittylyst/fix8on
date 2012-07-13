package fix8on;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

public class Main {

	private SocketAcceptor acceptor;
	private Application app;
	
	private void init(String dirStr) throws ConfigError {
		// Convert to a Path
		
		// Find all JSON objects
		List<File> clientFiles = new ArrayList<>();
		
		// Find main config
		JsonNode primaryCfg = createJsonObject(null);
		
		// Handle client configs
		List<JsonNode> clientCfgs = clientFiles.map(f -> createJsonObject(f)).into(new ArrayList<JsonNode>());
				
		// Configure up the acceptor
		SessionSettings settings = createSessionSettings(primaryCfg);
		app = new DMAManager(settings, clientCfgs);
        MessageStoreFactory msgStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        DefaultMessageFactory msgFactory = new DefaultMessageFactory();

        acceptor = new SocketAcceptor(app, msgStoreFactory, settings,
                                      logFactory, msgFactory);
		
	}
	
	private static JsonNode createJsonObject(File f) throws ConfigError {
		String mainCfgStr = "";
		
		JsonNode primaryCfg;
		try {
			ObjectMapper mapper = new ObjectMapper();
			primaryCfg = mapper.readTree(mainCfgStr);
		} catch (IOException iox) {
			throw new ConfigError(iox);
		}

		return primaryCfg;
	}
	
	private SessionSettings createSessionSettings(JsonNode primaryCfg) {
		// TODO Auto-generated method stub
		return null;
	}

	private void run() {
		
	}
	
	/**
	 * @param args
	 * @throws ConfigError 
	 * @throws RuntimeError 
	 */
	public static void main(String[] args) throws RuntimeError, ConfigError {
		Main m = new Main();
		m.init(args[0]);
		m.start();
		m.run();
		m.stop();
	}

	/**
	 * Now fully initialized, this method is used to start accepting connections
	 * 
	 * @throws RuntimeError
	 * @throws ConfigError
	 */
	private void start() throws RuntimeError, ConfigError {
		acceptor.start();
	}
	
	/**
	 * Cleanup method
	 */
	private void stop() {
		acceptor.stop();
	}

}
