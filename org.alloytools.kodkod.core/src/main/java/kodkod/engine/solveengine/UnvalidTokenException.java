package kodkod.engine.solveengine;

public class UnvalidTokenException extends Throwable {
    public UnvalidTokenException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        return "UnvalidTokenException{}";
    }
}
