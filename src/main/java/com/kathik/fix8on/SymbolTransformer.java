package com.kathik.fix8on;

import java.util.function.Function;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.Symbol;

/**
 * A class to house symbol transformers
 * 
 * @author boxcat
 * 
 */
public abstract class SymbolTransformer implements Function<Message, Message> {

    private static final class MSGSymbolTransformer extends SymbolTransformer {

        /**
         * Simple transformation of the symbol - just reverses the symbol
         */
        @Override
        public Message map(Message t) {
            String orig;
            try {
                orig = t.getHeader().getString(Symbol.FIELD);
            } catch (FieldNotFound e) {
                return t;
            }
            if (orig == null)
                return t;

            StringBuilder sb = new StringBuilder(orig);
            t.getHeader().setString(Symbol.FIELD, sb.reverse().toString());
            return t;
        }

        @Override
        public Message apply(Message t) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static SymbolTransformer of(String from) {
        switch (from) {
        case "MSG":
            return new MSGSymbolTransformer();
        default:
            return null;
        }
    }

    public abstract Message map(Message t);

}
