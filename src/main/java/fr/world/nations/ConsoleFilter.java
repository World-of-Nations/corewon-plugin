package fr.world.nations;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ConsoleFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        return !record.getMessage().toLowerCase()
                .contains("Cannot find valid material for".toLowerCase());
    }
}