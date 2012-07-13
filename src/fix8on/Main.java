package fix8on;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
import quickfix.SocketInitiator;

public class Main {

	private SocketAcceptor acceptor; // connections from clients
	private SocketInitiator initiator; // sending stuff down to market
	private Application clientsideMgr;
	private Application marketsideMgr;
	
	/**
	 * Helper class which finds the main config file and any client configuration files
	 * 
	 * @author boxcat
	 * 
	 */
	private static class FindJsonVisitor extends SimpleFileVisitor<Path> {
		private final List<Path> files = new ArrayList<>();
		private String clientCfg;
		private String mktCfg;
		
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
	      Path fileName = file.getFileName();
	      if (fileName.endsWith("client.cfg")) {
	    	  clientCfg = file.toAbsolutePath().toString();
	      } else if (fileName.endsWith("market.cfg")) {
	    	  mktCfg = file.toAbsolutePath().toString();
	      } else if (fileName.endsWith(".json")) {
	    	  files.add(file);
	      }
	      return FileVisitResult.CONTINUE;
	    }

		public List<Path> getFiles() {
			return files;
		}

		public String getClientsideCfg() {
			return clientCfg;
		}

		public String getMarketsideCfg() {
			return mktCfg;
		}
	}
	
	private void init(String dirStr) throws ConfigError {		
		// Find all JSON objects
		FindJsonVisitor visitor = new FindJsonVisitor();
		try {
			Files.walkFileTree(Paths.get(dirStr), visitor); 
		} catch (IOException iox) {
			throw new ConfigError(iox);
		}
		
		// Handle client configs
		List<JsonNode> clientCfgs = visitor.getFiles().map(f -> createJsonObject(f)).into(new ArrayList<JsonNode>());		
		if (clientCfgs.contains(null)) throw new ConfigError("Malformed JSON file in config dir");
		
		// Handle main config
//		System.out.println("Clientside: "+ visitor.getClientsideCfg());
//		System.out.println("Marketside: "+ visitor.getMarketsideCfg());
		SessionSettings clientsideSettings = new SessionSettings(visitor.getClientsideCfg());
		SessionSettings mktsideSettings = new SessionSettings(visitor.getMarketsideCfg());
		
		// Configure up the acceptor - which will handle the transforms of incoming messages from clients
		clientsideMgr = new ClientsideManager(clientsideSettings, clientCfgs);
        MessageStoreFactory msgStoreFactory = new FileStoreFactory(clientsideSettings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true);

        acceptor = new SocketAcceptor(clientsideMgr, msgStoreFactory, clientsideSettings,
                                      logFactory, new DefaultMessageFactory());
        
        // 
		marketsideMgr = new MarketsideManager(mktsideSettings, clientCfgs);
        msgStoreFactory = new FileStoreFactory(mktsideSettings);
//        LogFactory logFactory = new ScreenLogFactory(true, true, true);

        initiator = new SocketInitiator(marketsideMgr, msgStoreFactory, mktsideSettings,
                                      logFactory, new DefaultMessageFactory());
		
	}
	
	private static JsonNode createJsonObject(Path p) {
		JsonNode cfg;
		try {
			ObjectMapper mapper = new ObjectMapper();
			cfg = mapper.readTree(p.toFile());
		} catch (IOException iox) {
			// FIXME Log a config error here...
			return null;
		}

		return cfg;
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
		initiator.start();
		acceptor.start();
	}
	
	/**
	 * Cleanup method
	 */
	private void stop() {
		acceptor.stop();
		initiator.stop();
	}
}
