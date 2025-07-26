package de.kevin_stefan.virtualChests.utils;

import java.util.logging.Logger;

public final class PluginLogger {

    private final Logger logger;
    private boolean debug = false;

    public PluginLogger(Logger pluginLogger) {
        this.logger = pluginLogger;
    }

    public PluginLogger(Logger pluginLogger, boolean debug) {
        this.logger = pluginLogger;
        setDebug(debug);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }

    public void severe(String msg) {
        logger.severe(msg);
    }

    public void debug(String msg) {
        var frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1).findFirst().orElse(null));
        if (frame == null) {
            logger.info("DEBUG: " + msg);
            return;
        }
        String caller = String.format("L%s %s#%s: ", frame.getLineNumber(), frame.getDeclaringClass().getSimpleName(), frame.getMethodName());
        logger.info(caller + msg);
    }

}
