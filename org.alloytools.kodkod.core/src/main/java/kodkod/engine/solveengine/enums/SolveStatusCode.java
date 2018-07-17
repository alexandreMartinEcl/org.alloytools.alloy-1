package kodkod.engine.solveengine.enums;

public enum SolveStatusCode {
    INTERRUPTED     ("interrupted"),
    NOTSTARTED      ("notstarted"),
    NOTSOLVED       ("notsolved"),
    OPTIMAL         ("optimal"),
    INFEASIBLE      ("infeasible"),
    UNBOUNDED       ("unbounded"),
    SATISFIABLE     ("satisfiable"),
    UNSATISFIABLE   ("unsatisfiable"),
    UNKNOWN         ("unknown")
    ;

    private final String strVal;

    private SolveStatusCode(String strVal) {
        this.strVal= strVal;
    }

    public String getStrVal() {
        return strVal;
    }

    public static SolveStatusCode build(String strVal) {
        for (SolveStatusCode solveStatusCode : SolveStatusCode.values()) {
            if (strVal.equals(solveStatusCode.getStrVal())) {
                return solveStatusCode;
            }
        }
        return SolveStatusCode.UNKNOWN;
    }
}

