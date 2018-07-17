package kodkod.engine.solveengine;

public class SolvingStoppedException extends Exception{

    public SolvingStoppedException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        return "SolvingStoppedException{}";
    }
}
