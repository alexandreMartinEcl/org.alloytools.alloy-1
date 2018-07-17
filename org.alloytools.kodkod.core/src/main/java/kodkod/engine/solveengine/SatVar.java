package kodkod.engine.solveengine;

/**
 * Most basic Expr instance.
 */
public class SatVar extends Expr{
    /** Name set to define the variable */
    private String name;
    /** In case the SATModel is satisfiable, here is a potential value of the variable, according to the solver */
    private Boolean value;
    /** Id to represent the variable in a cnf file */
    private Integer id;

    public SatVar(String name, Integer id) {
        this.name = name;
        this.value = null;
        this.id = id;
    }

    @Override
    public ListExpr convertToCnf() {
        return new AND(new OR(this));
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getName() {
        return name;
    }

    public Boolean getValue() {
        return value;
    }

    public Integer getId() {
        return id;
    }

    public String getCnfStr(){
        return this.id.toString();
    }

    public String getResult(){
        if  (this.value == null){
            return this.name +
                    ": not computed";
        } else {
            return this.name +
                    ": " +
                    this.value.toString();
        }
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
