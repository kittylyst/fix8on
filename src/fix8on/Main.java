package fix8on;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

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
	private ClientsideManager clientsideMgr;
	private MarketsideManager marketsideMgr;
	
	/**
	 * Helper class which finds the main config file and any client configuration files
	 * 
	 * @author boxcat
	 * 
	 */
	static class FindJsonVisitor extends SimpleFileVisitor<Path> {
		private final List<Path> files = new ArrayList<>();
		private String clientCfg;
		private String mktCfg;
		
		private static final PathMatcher jsonMatcher = FileSystems.getDefault().getPathMatcher("glob:*.json");
		
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
	      Path fileName = file.getFileName();
	      if (fileName.endsWith("client.cfg")) {
	    	  clientCfg = file.toAbsolutePath().toString();
	    	  // FIXME Convert to logging
//	  		System.out.println("Found client config: "+ clientCfg);
	      } else if (fileName.endsWith("market.cfg")) {
	    	  mktCfg = file.toAbsolutePath().toString();
//	    	  System.out.println("Found market config: "+ mktCfg);
	      } else if (jsonMatcher.matches(fileName)) {
	    	  // Now path match horribleness
//	    	  System.out.println("Found json file: "+ fileName);
	    	  files.add(file);
	      } else {
//	    	  System.out.println("Ignoring file: "+ fileName);
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
		List<Map<String, String>> clientCfgs = visitor.getFiles().map(f -> createConfig(f)).into(new ArrayList<Map<String, String>>());		
		if (clientCfgs.contains(null)) throw new ConfigError("Malformed JSON file in config dir");
		
		// Handle main config
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
		marketsideMgr.setOtherside(clientsideMgr);
		clientsideMgr.setOtherside(marketsideMgr);
        msgStoreFactory = new FileStoreFactory(mktsideSettings);
//        LogFactory logFactory = new ScreenLogFactory(true, true, true);

        initiator = new SocketInitiator(marketsideMgr, msgStoreFactory, mktsideSettings,
                                      logFactory, new DefaultMessageFactory());
		
	}
	
	private static Map<String, String> createConfig(Path p) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>(){}; 
	    
			return mapper.readValue(p.toFile(), typeRef);
		} catch (IOException iox) {
			System.out.println("File "+ p.getFileName() +" contains bad config");
			return null;
		}
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
