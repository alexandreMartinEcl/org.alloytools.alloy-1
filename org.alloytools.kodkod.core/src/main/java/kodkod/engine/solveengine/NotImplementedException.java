package kodkod.engine.solveengine;

public class NotImplementedException extends Exception {

    public NotImplementedException(String msg) {
        super(msg);
    }

    public NotImplementedException() {
        super();
    }

    @Override
    public String toString() {
        return "NotImplementedException{}";
    }
}
