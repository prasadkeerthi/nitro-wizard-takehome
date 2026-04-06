package org.example.nitrowizard.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public final class LoggingConfig {
    private LoggingConfig() {
    }

    public static void configure(boolean verbose) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Level level = verbose ? Level.DEBUG : Level.INFO;
        ch.qos.logback.classic.Logger root = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }
}
