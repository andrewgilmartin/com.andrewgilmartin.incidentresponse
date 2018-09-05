package com.andrewgilmartin.util;

import java.text.MessageFormat;
import java.util.logging.Level;

public class Logger {

    private final java.util.logging.Logger logger;

    public static Logger getLogger(Class c) {
        return new Logger(java.util.logging.Logger.getLogger(c.getName()));
    }

    private Logger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    public void debug(Throwable t, String message, Object... parameters) {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, MessageFormat.format(message, parameters), t);
        }
    }

    public void debug(String message, Object... parameters) {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, MessageFormat.format(message, parameters));
        }
    }

    public void info(Throwable t, String message, Object... parameters) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, MessageFormat.format(message, parameters), t);
        }
    }

    public void info(String message, Object... parameters) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, MessageFormat.format(message, parameters));
        }
    }

    public void warn(Throwable t, String message, Object... parameters) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.log(Level.WARNING, MessageFormat.format(message, parameters), t);
        }
    }

    public void warn(String message, Object... parameters) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.log(Level.WARNING, MessageFormat.format(message, parameters));
        }
    }

    public void error(Throwable t, String message, Object... parameters) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, MessageFormat.format(message, parameters), t);
        }
    }

    public void error(String message, Object... parameters) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, MessageFormat.format(message, parameters));
        }
    }

}

// END

