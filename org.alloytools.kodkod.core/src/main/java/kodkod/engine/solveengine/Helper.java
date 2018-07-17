package kodkod.engine.solveengine;

import java.util.ArrayList;

public class Helper {

    public static class SEProblem {
        public String name = "";
        public String data = "";

        public SEProblem(String name, String data) {
            this.name = name;
            this.data = data;
        }
    }

    public static class SEResults {
        public String status = "";
        public String objective_value = "no objective value";
        public ArrayList<SEVariable> variables = new ArrayList<>();
    }

    public static class SEVariable {
        public String name = "";
        public double value = 0.0;
    }
    public static class ProblemsToSend {
        public ArrayList<SEProblem> problems = new ArrayList<>();

        public ProblemsToSend(ArrayList<SEProblem> problems) {
            this.problems = problems;
        }
    }

    public static class SimpleResponse {
        public String code = "";
        public String message = "";

        public String buildErrorMessage() {
            return new StringBuffer().append("Error type : ")
                    .append(code).append("\nMessage returned by the server : ")
                    .append(message).toString();
        }
    }

    public static class JobCreateResponse extends SimpleResponse {
        public String id = "";
    }

    public static class JobStatusResponse extends SimpleResponse {
        public String status = "";
    }

    public static class JobResultsResponse extends SimpleResponse {
        public String job_id = "";
        public SEResults result = new SEResults();
    }
}
