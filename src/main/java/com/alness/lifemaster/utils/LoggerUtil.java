package com.alness.lifemaster.utils;

import com.alness.lifemaster.common.messages.Messages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerUtil {
    private LoggerUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void logError(Exception e) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2]; // método que llamó a logError()
        log.error(Messages.LOG_ERROR_API,
                caller.getClassName(),
                caller.getMethodName(),
                e.getMessage(), e);
    }
}
