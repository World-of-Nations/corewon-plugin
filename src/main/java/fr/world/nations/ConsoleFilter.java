package fr.world.nations;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ConsoleFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        return !record.getMessage().contains("[Server Thread/INFO] [co.ma.fa.FactionsPlugin/]: [SaberFactions] Cannot find valid material for:");
    }
}