/*
 * Kodkod -- Copyright (c) 2005-present, Emina Torlak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package kodkod.engine.satlab;

import kodkod.engine.solveengine.*;
import kodkod.engine.solveengine.enums.SolveStatusCode;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.rmi.ServerException;

/**
 * Java wrapper for Satalia's SolveEngine solver
 *
 * @author Alexandre Martin
 */
final class SolveEngine implements SATSolver {
    private SATModel				solver;
    private Boolean					sat;
    private int						vars, clauses;

    /**
     * Constructs a wrapper for the given instance of ISolver.
     *
     * @throws NullPointerException solver = null
     */
    SolveEngine(String token, String fileName) {
        /** A4Options is sending the full path of the file like /temp/folder/model.als */
        if (fileName.indexOf("/") != -1) {
            String[] splittedPath = fileName.split("/");
            fileName = splittedPath[splittedPath.length - 1];
        }

        if (fileName.indexOf(".") != -1)
            fileName = fileName.split("\\.")[0];

        this.solver = new SATModel(token, fileName);
        this.sat = null;
        this.vars = this.clauses = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see SATSolver#numberOfVariables()
     */
    @Override
    public int numberOfVariables() {
        return vars;
    }

    /**
     * {@inheritDoc}
     *
     * @see SATSolver#numberOfClauses()
     */
    @Override
    public int numberOfClauses() {
        return clauses;
    }

    /**
     * {@inheritDoc}
     *
     * @see SATSolver#addVariables(int)
     */
    @Override
    public void addVariables(int numVars) {
        if (numVars < 0)
            throw new IllegalArgumentException("numVars < 0: " + numVars);
        else if (numVars > 0) {

            for (Integer i = vars + 1; i <= vars + numVars; i++) {
                String varName = new StringBuffer().append("var").append(i.toString()).toString();
                try {
                    solver.addVariable(varName);
                } catch (ExistingVariableException e) {
                    throw new SATAbortedException("Internal error when building the model: " + e.getMessage());
                }
            }

            vars += numVars;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see SATSolver#addClause(int[])
     */
    @Override
    public boolean addClause(int[] lits) {
        if (!Boolean.FALSE.equals(sat)) {
            clauses++;
            try {
                solver.addCnfClause(lits);
            } catch (UnvalidClauseException e) {
                throw new SATAbortedException("Error from solve engine server: " + e.getMessage());
            }
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see SATSolver#solve()
     */
    @Override
    public boolean solve() throws SATAbortedException {
        if (solver == null)
            return false;
        if (!Boolean.FALSE.equals(sat)){
            try {
                solver.solve();
            } catch (SolvingStoppedException | UnusualResponseException | ServerException e) {
                throw new SATAbortedException("Error from solve engine server: " + e.getMessage());
            } catch (UnvalidTokenException | UnknownHostException e) {
                throw new SATAbortedException("Could not use SolveEngine solver: " + e.getMessage());
            } catch (UnsupportedEncodingException | NotImplementedException e) {
                //Should not happen
                throw new SATAbortedException("Fatal error in SolveEngine code");
            }

            sat = Boolean.valueOf(solver.getSolveStatus() == SolveStatusCode.SATISFIABLE);
        }
        return sat;
    }

    /**
     * {@inheritDoc}
     *
     * @see SATSolver#valueOf(int)
     */
    @Override
    public final boolean valueOf(int variable) {
        if (!Boolean.TRUE.equals(sat))
            throw new IllegalStateException();
        if (variable < 1 || variable > vars)
            throw new IllegalArgumentException(variable + " !in [1.." + vars + "]");

        String varName = solver.getVariableNames().get(variable);

        return solver.getVariables().get(varName).getValue();
    }

    /**
     * {@inheritDoc}
     *
     * @see SATSolver#free()
     */
    @Override
    public synchronized final void free() {
        sat = Boolean.FALSE;
        solver = null;
    }


    public static void test(String[] args) {
        // in order to make the test, enter a correct api-key (found by signing on at solve.satalia.com)
        final SolveEngine z = (SolveEngine) SATFactory.SolveEngine("anApiKey=", "testFile").instance();
        // z.addVariables(3);
        // int[] clause = {1,2,3};
        // z.addClause(clause);
        // int[] clause1 = {-3};
        // z.addClause(clause1);
        // System.out.println(z.solver.nVars());
        // z.addVariables(4);
        // System.out.println(z.solver.nVars());
        // clause1[0] = 7;
        // z.addClause(clause1);
        z.addVariables(1);
        int[] clause1 = {
                1
        };
        z.addClause(clause1);
        clause1[0] = -1;
        z.addClause(clause1);

        System.out.println(z.solve());
        // System.out.println(z.variablesThatAre(true, 1, 1));

    }

}

