package com.kathik.fix8on;

import java.io.IOException;
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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.RuntimeError;
import quickfix.SessionSettings;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private DMATransformEngine transformEngine;

    private List<Map<String, String>> clientCfgs;

    private volatile boolean shutdown = false;

    public void shutdown() {
        shutdown = true;
    }

    @Override
    public String toString() {
        return "Main{" + "transformEngine=" + transformEngine + ", clientCfgs=" + clientCfgs + ", shutdown=" + shutdown + '}';
    }

    private Main() {
    }

    /**
     * Helper class which finds the main configuration file and any client
     * configuration files
     *
     * @author boxcat
     */
    public static class FindJsonVisitor extends SimpleFileVisitor<Path> {

        private final List<Path> files = new ArrayList<>();
        private String clientCfg;
        private String mktCfg;

        private static final PathMatcher jsonMatcher = FileSystems.getDefault()
                .getPathMatcher("glob:*.json");

        @Override
        public @Nonnull
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            Path fileName = file.getFileName();
            if (fileName.endsWith("client.cfg")) {
                clientCfg = file.toAbsolutePath().toString();
                if (logger.isInfoEnabled()) {
                    logger.info("Found client config: " + clientCfg);
                }
            } else if (fileName.endsWith("market.cfg")) {
                mktCfg = file.toAbsolutePath().toString();
                if (logger.isInfoEnabled()) {
                    logger.info("Found market config: " + mktCfg);
                }
            } else if (jsonMatcher.matches(fileName)) {
                // Now path match horribleness
                if (logger.isDebugEnabled()) {
                    logger.debug("Found json file: " + fileName);
                }
                files.add(file);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring file: " + fileName);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        List<Map<String, String>> handleClientConfiguration() throws ConfigError {
            final List<Map<String, String>> clientConfigs
                    = getFiles().stream()
                    .map(f -> createConfig(f)).collect(Collectors.toList());
            if (clientConfigs.contains(null)) {
                // Work around difficulties with exceptions being thrown from lambdas
                throw new ConfigError("Malformed JSON file in config dir");
            }
            return clientConfigs;
        }

        private static Map<String, String> createConfig(Path p) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<HashMap<String, String>> typeRef
                        = new TypeReference<HashMap<String, String>>() {
                        };

                return mapper.readValue(p.toFile(), typeRef);
            } catch (IOException iox) {
                logger.warn("File " + p.getFileName() + " contains bad config");
                return null;
            }
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
        // Find all JSON objects and client config
        FindJsonVisitor visitor = findAllJsonFiles(dirStr);

        // Handle client configs and set clientCfgs
        clientCfgs = visitor.handleClientConfiguration();

        // Handle main config
        final ClientsideManager clientsideMgr = new ClientsideManager(new SessionSettings(visitor.getClientsideCfg()));
        final MarketsideManager marketsideMgr = new MarketsideManager(new SessionSettings(visitor.getMarketsideCfg()));
        transformEngine = new DMATransformEngine(marketsideMgr, clientsideMgr);
    }

    private @Nonnull
    FindJsonVisitor findAllJsonFiles(String dirStr) throws ConfigError {
        final FindJsonVisitor visitor = new FindJsonVisitor();
        try {
            Files.walkFileTree(Paths.get(dirStr), visitor);
        } catch (IOException iox) {
            throw new ConfigError(iox);
        }
        return visitor;
    }

    private void run(boolean pauseMode) {
        while (!shutdown) {
            try {
                Thread.sleep(5000);
                if (pauseMode) {
                    shutdown = true;
                }
            } catch (InterruptedException e) {
            }
        }
        logger.info(toString());
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
        m.run(true);
        m.stop();
    }

    /**
     * Now fully initialized, this method is used to start accepting connections
     *
     * @throws RuntimeError
     * @throws ConfigError
     */
    public void start() throws RuntimeError, ConfigError {
        transformEngine.start();
    }

    /**
     * Cleanup method
     */
    private void stop() {
        transformEngine.stop();
    }
}
