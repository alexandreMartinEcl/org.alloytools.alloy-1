package kodkod.engine.solveengine;

public class ExistingVariableException extends Exception{

    public ExistingVariableException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        return "ExistingVariableError{}";
    }
}
