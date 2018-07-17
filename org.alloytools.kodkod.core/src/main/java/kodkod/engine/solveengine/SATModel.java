package kodkod.engine.solveengine;

import kodkod.engine.solveengine.enums.SEStatusCode;
import kodkod.engine.solveengine.enums.SolveStatusCode;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.abs;


public class SATModel extends Model {
    /** HashMap of the variables, id -> name */
    private HashMap<Integer, String> variableNames;

    /** HashMap of the variables, name -> id */
    private HashMap<String, Integer> variableIds;

    /** HashMap of the variables, name -> variable instance */
    private HashMap<String, SatVar> variables;

    /** List of all the constraints, as added*/
    private ArrayList<Expr> constraints;

    public HashMap<String, SatVar> getVariables() { return variables; }

    public HashMap<String, Integer> getVariableIds() {return variableIds;}

    public ArrayList<Expr> getConstraints() {return constraints;}

    public SolveStatusCode getSolveStatus() { return solveStatus; }

    public SEStatusCode getSeStatus() { return seStatus; }

    public String getFileName() {return fileName;}

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    /**
     * If the name does not finish by ".cnf", will add it to the fileName
     * @param modelName
     */
    public void setFileName(String modelName) {
        if (!modelName.endsWith(".cnf")) {
            this.fileName = new StringBuffer()
                    .append(modelName)
                    .append(".cnf")
                    .toString();
        } else {
            this.fileName = modelName;
        }
    }

    public HashMap<Integer, String> getVariableNames() { return variableNames; }

    public Integer getSleepTime() { return client.getSleepTime(); }
    public void setSleepTime(Integer sleepTime) { this.client.setSleepTime(sleepTime); }

    /**
     * Splits/translates the constraints into cnf clauses
     * @return the ArrayList of reducted Expr instances, each Expr is a row of the cnf file to be built
     * @throws NotImplementedException if the code has been modified not properly
     */
    private ArrayList<Expr> constraintsToClauses() throws NotImplementedException {
        ArrayList<Expr> clauses = new ArrayList<>();
        ArrayList<Expr> temContent;

        for (Expr constraint : constraints) {
            temContent= constraint.convertToCnf().getContent();
            clauses.addAll(temContent);
        }

        return clauses;
    }

    public SATModel(String token, String fileName, Integer sleepTime, Boolean interactiveMode) {
        super(token, fileName, sleepTime, interactiveMode);
        this.variableIds = new HashMap<>();
        this.variableNames = new HashMap<>();
        this.variables = new HashMap<>();
        this.constraints = new ArrayList<>();
    }

    public SATModel(String token, String fileName, Integer sleepTime) { this(token, "myModel", sleepTime, false); }
    public SATModel(String token, String fileName, Boolean interactivemode) { this(token, "myModel", 2, interactivemode); }
    public SATModel(String token, String fileName) { this(token, fileName, 2, false); }
    public SATModel(String token) { this(token, "myModel", 2, false); }

    /**
     * Creates a SatVar instance and adds it to the three hashmaps of the SATModel.
     * @param name of the variable
     * @param id the integer that will be used for the cnf model
     * @return the SatVar instance
     * @throws ExistingVariableException
     */
    public SatVar addVariable(String name, Integer id) throws ExistingVariableException {
        if(this.variableIds.containsKey(name)) {
            throw new ExistingVariableException("This name is already used by another variable");
        }

        this.variableIds.put(name, id);
        this.variableNames.put(id, name);

        SatVar var = new SatVar(name, id);
        this.variables.put(name, var);

        return var;
    }

    /**
     * Will add a variable with a specified name. The id used by default is the smallest one not being used yet
     * @param name of the variable
     * @return the SatVar instance
     * @throws ExistingVariableException if the name chosen has already been used
     */
    public SatVar addVariable(String name) throws ExistingVariableException {
        int id;

        if(this.variableNames.containsKey(this.variables.size() + 1)) {
            id = 1;
            while(this.variableNames.containsKey(id)) {
                id++;
            }
        } else {
            id = this.variables.size() + 1;
        }

        return this.addVariable(name, id);
    }

    /**
     * Gets a variable from the model using the id, or creates a new one
     * @return the SatVar instance with the asked id
     */
    private SatVar addOrGetVariable(Integer id) {
        String varName;
        String finalVarName;
        Integer i = 2;
        Integer actualId = abs(id);
        if (this.variableNames.containsKey(actualId)) {
            varName = this.variableNames.get(actualId);
            return this.variables.get(varName);
        } else {
            varName = "var" + id.toString();
            finalVarName = varName;

            // prevent ExistingVaraibleException if the user already
            // added the automatically generated varName for another variable
            // instead of var1, could be set to var1_2
            while(this.variableIds.containsKey(finalVarName)) {
                finalVarName = varName + "_" + i.toString();
            }

            try {
                return this.addVariable(finalVarName, id);
            } catch (ExistingVariableException e) { // should not happen
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Add an Expr instance, which makes a constraint, to the list of constraint of the model
     */
    public void addConstraint(Expr expr) {
        this.constraints.add(expr);
    }

    /**
     * Add a new constraint, by translating a list of integers, as you could read in a cnf-modeled file (without 0)
     */
    public void addCnfClause(ArrayList<Integer> arrInt) throws UnvalidClauseException {
        Expr expr = null;
        Expr var = null;

        for (Integer id : arrInt) {

            if (id == 0) {
                throw new UnvalidClauseException("Cannot have variable 0 in a cnf clause.");
            }

            var = this.addOrGetVariable(id);

            if (id < 0) {
                var = var.negate();
            }

            if (expr == null) {
                expr = var;
            } else {
                expr = expr.or(var);
            }
        }

        this.constraints.add(expr);
    }

    /**
     * Add a new constraint, by translating an array of integers, as you could read in a cnf-modeled file (without 0)
     */
    public void addCnfClause(int[] arrInt) throws UnvalidClauseException {
        ArrayList<Integer> arr = new ArrayList<>();
        for (int i : arrInt) {
            arr.add(i);
        }

        addCnfClause(arr);
    }

    /**
     * Add new constraints, by translating each list of integers from an array,
     * as you could read in a cnf-modeled file (without 0)
     */
    public void addListCnfClauses(ArrayList<ArrayList<Integer>> arrClauses) throws UnvalidClauseException {
        for (ArrayList<Integer> clause : arrClauses) {
            this.addCnfClause(clause);
        }
    }

    /**
     * Add new constraints, by translating each list of integers from an array,
     * as you could read in a cnf-modeled file (without 0)
     */
    public void addListCnfClauses(int[][] arrClauses) throws UnvalidClauseException {
        Expr expr = null;
        Expr var = null;

        for (int[] clause : arrClauses) {
            this.addCnfClause(clause);
        }

        this.constraints.add(expr);
    }

    /**
     * Builds the str file of the problem, written in the CNF format
     * Ready to be saved or sent as .cnf file
     * @return returns the str value of the text
     * @throws NotImplementedException
     */
    public String buildStrModel() throws NotImplementedException {

        ArrayList<Expr> clauses = constraintsToClauses();

        // writes the first row
        StringBuilder strBuff = new StringBuilder()
                .append("p cnf ")
                .append(Integer.toString(variables.size())).append(" ")
                .append(Integer.toString(clauses.size())).append("\n");

        // writes the row for each clause
        for (Expr reductedClause : clauses) {
            strBuff.append(reductedClause.getCnfStr()).append("\n");
        }

        return strBuff.toString();
    }

    /**
     * Analyse the SEResults, containing the results returned from teh solver, to complete the model
     * @param result
     */
    protected void processResults(Helper.SEResults result) {
        solveStatus = SolveStatusCode.build(result.status);

        for (Helper.SEVariable variable : result.variables) {
            int id = Integer.valueOf(variable.name);

            if (variableNames.containsKey(id)){
                String varName = variableNames.get(id);
                SatVar var = variables.get(varName);
                var.setValue((variable.value == 1));
            }
        }
    }

    /**
     * Prints a summary of the results
     */
    public void printResults() {
        ArrayList<String> arrLines = new ArrayList<>();
        arrLines.add("Status : " + this.solveStatus.getStrVal());

        for (SatVar var : this.variables.values()) {
            arrLines.add(var.getResult());
        }

        System.out.println(String.join("\n", arrLines));
    }

    /**
     * Remove a variable of the three hashmpas of the model, using its name
     * @param name
     */
    private void removeVariable(String name) {
        if (variableIds.containsKey(name)) {
            int id = variableIds.get(name);
            variableIds.remove(name);
            variableNames.remove(id);
            variables.remove(name);
        }
    }

    /**
     * Remove a variable of the three hashmpas of the model, using its id
     * @param id
     */
    public void removeVariable(int id) {
        if (variableNames.containsKey(id)) {
            removeVariable(variableNames.get(id));
        }
    }

}


/**
 * An Expr is a either an alone variable,
 * a list of Variables linked by an operator,
 * or a list of Expr linked by an operator
 */
class Expr {
    String OPERATOR = "ERROR";

    @Override
    public String toString() {
        try {
            throw new NotImplementedException();
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ListExpr convertToCnf() throws NotImplementedException {
        try {
            throw new NotImplementedException();
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The next methods produce a particular ListEpxr linking this instance and the input
     * @param other expression to be linked with
     * @return ListExpr of this and other
     */
    public OR or(Expr other){return new OR(this, other);}

    public AND and(Expr other){return new AND(this, other);}

    public XOR xor(Expr other){return new XOR(this, other);}

    public IMP implies(Expr other){return new IMP(this, other);}

    public EQ eq(Expr other){return new EQ(this, other);}

    public NE ne(Expr other){return new NE(this, other);}


    /**
     * Returns the negation of this
     */
    public Expr negate(){return new NEG(this);}

    /**
     * Subclass must have this method to make a cnf-modeled string of the expression
     * A variable will just return the string of its id, for example
     */
    public String getCnfStr() throws NotImplementedException {
        throw new NotImplementedException();
    }

    public String getOPERATOR(){return OPERATOR;}
}


class NEG extends Expr {
    private String OPERATOR = "-";

    private Expr inner;

    public NEG(Expr inner) {
        assert !(inner instanceof NEG) : "Input for NEG expression is of wrong instance";
        this.inner = inner;
    }

    @Override
    public String toString() {
        return this.OPERATOR +
                this.inner;
    }

    public Expr negate(){
        return this.inner;
    }

    public String getCnfStr() {
        // get the id of the variable or negation of a variable
        assert (this.inner instanceof SatVar) : "Input for NEG expression is of wrong instance 952";
        SatVar inner1 = (SatVar) this.inner;
        return this.OPERATOR +
                inner1.getId();
    }

    private Expr getInner() {
        return inner;
    }

    public ListExpr convertToCnf() throws NotImplementedException {
        Expr expr = this.getInner();

        if (expr instanceof SatVar) {
            return new AND(new OR(this));
        }
        if (expr instanceof XOR) {
            expr = ((XOR) expr).getEquivalentExpr();
        } else if (expr instanceof IMP) {
            expr = ((IMP) expr).getEquivalentExpr();
        } else if (expr instanceof EQ) {
            expr = ((EQ) expr).getEquivalentExpr();
        } else if (expr instanceof NE) {
            expr = ((NE) expr).getEquivalentExpr();
        }

        if (expr instanceof AND) {
            ArrayList<Expr> l = new ArrayList<>();

            for (Expr expr1 : ((ListExpr) expr).getContent()) {
                l.add(expr1.negate());
            }

            return new OR(l).convertToCnf();
        }

        if (expr instanceof OR) {
            ArrayList<Expr> l = new ArrayList<>();

            for (Expr expr1 : ((ListExpr) expr).getContent()) {
                l.add(expr1.negate());
            }

            return new AND(l).convertToCnf();
        }

        throw new ValueException("found not supported inner type " +
                this.inner);
    }
}


/**
 * An Expr is a set of one or several boolean variables being linked to eahch other
 * through different operators that can link booleans
 */
class ListExpr extends Expr {
    // a dummy expr class for expression which can contain multiple expressions
    private ArrayList<Expr> content;

    ListExpr(ArrayList<Expr> content) {
        this.content = new ArrayList<>();
        this.addOther(content);
    }

    ListExpr(Expr one, Expr other) {
        this(makeArray(one, other));
    }

    ListExpr(Expr content) {
        this(makeArray(content));
    }

    private static ArrayList<Expr> makeArray(Expr one, Expr other){
        ArrayList<Expr> arr = new ArrayList<>();
        arr.add(one);
        arr.add(other);
        return arr;
    }

    private static ArrayList<Expr> makeArray(Expr elem){
        ArrayList<Expr> arr = new ArrayList<>();
        arr.add(elem);
        return arr;
    }

    private void addOther(ArrayList<Expr> content) {
        for (Expr expr : content) {
            if (expr.getClass() == this.getClass()) {
                ListExpr e = (ListExpr) expr;
                this.getContent().addAll(e.getContent());
            } else {
                this.getContent().add(expr);
            }
        }
    }

    @Override
    public ListExpr convertToCnf() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer()
                .append("(");
        for (Expr expr : this.getContent()) {
            buff.append(expr.toString());
            buff.append(" ");
            buff.append(this.getOPERATOR());
            buff.append(" ");
        }
        int toRemove = 1 + this.getOPERATOR().length() + 1;
        buff.delete(buff.length() - toRemove, buff.length())
                .append(")");
        return buff.toString();
    }

    public ArrayList<Expr> getContent() {
        return content;
    }

}


class AND extends ListExpr {
    // and expression
    String OPERATOR = "&";

    public AND(ArrayList<Expr> content) {
        super(content);
    }

    public AND(Expr one, Expr other) {
        super(one, other);
    }

    public AND(Expr elem) {
        super(elem);
    }

    @Override
    public ListExpr convertToCnf() throws NotImplementedException {
        ArrayList<Expr> l = new ArrayList<>();
        for (Expr expr : this.getContent()) {
            for (Expr expr1 : expr.convertToCnf().getContent()) {
                l.add(expr1);
            }
        }
        return new AND(l);
    }

    @Override
    public String getOPERATOR(){return OPERATOR;}
}


class OR extends ListExpr {
    String OPERATOR ="|";

    public OR(ArrayList<Expr> content) {
        super(content);
    }

    public OR(Expr one, Expr other) {
        super(one, other);
    }

    public OR(Expr elem) {
        super(elem);
    }

    @Override
    public AND convertToCnf() throws NotImplementedException {
        ArrayList<ArrayList<Expr>> l = new ArrayList<>();
        for (Expr expr : this.getContent()) {
            l.add(expr.convertToCnf().getContent());
        }


        ArrayList<ArrayList<Expr>> l1 = tools.iterProduct(l);

        ArrayList<Expr> l2 = new ArrayList<>();
        for (ArrayList<Expr> expr : l1) {
            l2.add(new OR(expr));
        }

        return new AND(l2);
    }

    @Override
    public String getCnfStr() throws NotImplementedException {
        StringBuffer buff = new StringBuffer();
        for (Expr expr : this.getContent()) {
            buff.append(expr.getCnfStr());
            buff.append(" ");
        }
        buff.append("0");
        return buff.toString();
    }

    @Override
    public String getOPERATOR(){return OPERATOR;}
}

/**
 * When the operator cannot link more than 2 Expressions, we use BinaryExpr instead of ListExpr
 */
class BinaryExpr extends Expr {
    /* Left expression */
    private Expr lhs;
    /* Right expression */
    private Expr rhs;

    public BinaryExpr(Expr lhs, Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Expr getLhs() {
        return lhs;
    }

    public Expr getRhs() {
        return rhs;
    }

    @Override
    public String toString() {
        return new StringBuffer()
                .append("(")
                .append(lhs)
                .append(" ")
                .append(this.getOPERATOR())
                .append(" ")
                .append(rhs)
                .append(")")
                .toString();
    }

    /**
     * get expression which only uses AND, OR and NEG and is equivalent
     */
    public ListExpr getEquivalentExpr() throws NotImplementedException {
        throw new NotImplementedException();

    }

    @Override
    public String getOPERATOR(){return OPERATOR;}
}


class XOR extends BinaryExpr {
    // xor expression
    String OPERATOR = "^";

    public XOR(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    @Override
    public ListExpr convertToCnf() throws NotImplementedException {
        return this.getEquivalentExpr().convertToCnf();
    }

    /**
     * get expression which only uses AND, OR and NEG and is equivalent
     */
    public ListExpr getEquivalentExpr(){
        // (this.lhs & -this.rhs) | (-this.lhs & this.rhs)
        return this.getLhs().and(this.getRhs().negate()).or(this.getLhs().negate().and(this.getRhs()));
    }

    @Override
    public String getOPERATOR(){return OPERATOR;}

}


class EQ extends BinaryExpr {
    // equivalence expression
    String OPERATOR = "==";

    public EQ(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    @Override
    public ListExpr convertToCnf() throws NotImplementedException {
        return this.getEquivalentExpr().convertToCnf();
    }

    /**
     * get expression which only uses AND, OR and NEG and is equivalent
     */
    public ListExpr getEquivalentExpr(){
        // (this.lhs & this.rhs) | (-this.lhs & -this.rhs)
        return this.getLhs().and(this.getRhs())
                .or(this.getLhs().negate().and(this.getRhs().negate()));
    }

    @Override
    public String getOPERATOR(){return OPERATOR;}

}


class NE extends BinaryExpr {
    // non equal expression
    String OPERATOR = "!=";

    public NE(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    @Override
    public ListExpr convertToCnf() throws NotImplementedException {
        return this.getEquivalentExpr().convertToCnf();
    }

    /**
     * get expression which only uses AND, OR and NEG and is equivalent
     */
    public ListExpr getEquivalentExpr(){
        // (-this.lhs | -this.rhs) & (this.lhs | this.rhs)
        return (ListExpr) this.getLhs().negate().or(this.getRhs().negate())
                .and(this.getLhs().or(this.getRhs()));
    }

    @Override
    public String getOPERATOR(){return OPERATOR;}

}


class IMP extends BinaryExpr {
    // implication expression
    String OPERATOR = "=>";

    public IMP(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    @Override
    public ListExpr convertToCnf() throws NotImplementedException {
        return this.getEquivalentExpr().convertToCnf();
    }

    /**
     * get expression which only uses AND, OR and NEG and is equivalent
     */
    public ListExpr getEquivalentExpr(){
        // -this.lhs | this.rhs
        return (ListExpr) this.getLhs().negate().or(this.getRhs());
    }

    @Override
    public String getOPERATOR(){return OPERATOR;}
}


/**
 * Do the same thing as we expect from itertools.product in python
 * [[A, B], [C, D], [E]] will return [[A, C, E], [A, D, E], [B, C, E], [B, D, E]]
 */
class tools {
    public static ArrayList<ArrayList<Expr>> iterProduct(ArrayList<ArrayList<Expr>> input){
        ArrayList<ArrayList<Expr>> res = new ArrayList<>();
        ArrayList<Expr> temArray;
        ArrayList<Expr> temArray2;

        if (input.size() > 1) {
            ArrayList<Expr> firstArray = input.get(0);
            input.remove(0);

            ArrayList<ArrayList<Expr>> nextInput = iterProduct(input);

            for (Expr expr : firstArray) {
                temArray = new ArrayList<>();
                temArray.add(expr);

                for (ArrayList<Expr> exprs : nextInput) {
                    temArray2 = (ArrayList<Expr>) temArray.clone();
                    temArray2.addAll(exprs);
                    res.add(temArray2);
                }
            }
        } else {
            for (Expr expr : input.get(0)) {
                temArray = new ArrayList<>();
                temArray.add(expr);
                res.add(temArray);
            }
        }

        return res;
    }
}

