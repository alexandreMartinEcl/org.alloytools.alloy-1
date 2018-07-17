package kodkod.engine.solveengine;

public class UnvalidClauseException extends Exception {

    public UnvalidClauseException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        return "UnvalidClauseException{}";
    }
}
