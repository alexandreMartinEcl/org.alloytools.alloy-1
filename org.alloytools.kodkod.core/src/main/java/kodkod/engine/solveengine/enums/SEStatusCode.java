package kodkod.engine.solveengine.enums;

public enum SEStatusCode {
    COMPLETED ("completed"),
    NOTSTARTED ("notstarted"),
    QUEUED ("queued"),
    CREATED ("created"),
    STARTED ("started"),
    STARTING ("starting"),
    STOPPED ("stopped"),
    FAILED ("failed"),
    INTERRUPTED ("interrupted"),
    TIMEOUT ("timeout"),
    UNKNOWN ("unknown")
    ;

    private final String strVal;

    private SEStatusCode(String strVal) {
        this.strVal= strVal;
    }

    public String getStrVal() {
        return strVal;
    }

    public static SEStatusCode build(String strVal) {
        for (SEStatusCode seStatusCode : SEStatusCode.values()) {
            if (strVal.equals(seStatusCode.getStrVal())) {
                return seStatusCode;
            }
        }
        return SEStatusCode.UNKNOWN;
    }
}

