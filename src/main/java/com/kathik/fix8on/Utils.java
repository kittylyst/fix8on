package com.kathik.fix8on;

import java.util.Map;

public final class Utils {

    public static String createUUID(final Map<String, String> map) {
        return map.get("SenderCompID");
    }

    public static FilterChain createClientsideFilters(final Map<String, String> m) {
        FilterChain out = new FilterChain();

        // Set up symbology handling
        if (m.get("symbol_from") != null) {
            out.add(SymbolTransformer.of(m.get("symbol_from")));
        }

        return out;
    }

    public static FilterChain createMarketsideFilters(final Map<String, String> m) {
        FilterChain out = new FilterChain();

        // Set up symbology handling
        if (m.get("symbol_from") != null) {
            out.add(SymbolTransformer.of(m.get("symbol_from")));
        }

        // Risk limit handling - only done marketside
        if (m.get("risk_limit") == null) {
            out.add(new RiskLimitTransformer());
        } else if (!m.get("risk_limit").equals("OFF")) {
            try {
                long limit = Long.parseLong(m.get("risk_limit"));
                out.add(new RiskLimitTransformer(limit));
            } catch (NumberFormatException nmx) {
                // Warn about misconfigured risk limit and carry on
            }
        }

        return out;
    }

}
