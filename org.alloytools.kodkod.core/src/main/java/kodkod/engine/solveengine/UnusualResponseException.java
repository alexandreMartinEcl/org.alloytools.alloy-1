package kodkod.engine.solveengine;

public class UnusualResponseException extends Exception{

        public UnusualResponseException(String msg) {
            super(msg);
        }

        @Override
        public String toString() {
            return "UnusualResponseException{}";
        }
    }
