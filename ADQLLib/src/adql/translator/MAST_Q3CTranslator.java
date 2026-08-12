package adql.translator;

import adql.query.constraint.Comparison;
import adql.query.constraint.ComparisonOperator;
import adql.query.operand.function.geometry.ContainsFunction;
import adql.query.operand.function.geometry.DistanceFunction;
import adql.query.operand.function.geometry.IntersectsFunction;

/**
 * MAST-specific Postgres/Q3C translator.
 *
 * This class intentionally keeps the default Q3C behavior in place until the
 * MAST-specific DISTANCE/CONTAINS/INTERSECTS semantics are implemented.
 */
public class MAST_Q3CTranslator extends Q3CTranslator {

    public MAST_Q3CTranslator() {
        super(false);
    }

    public MAST_Q3CTranslator(boolean allCaseSensitive) {
        super(allCaseSensitive);
    }

    public MAST_Q3CTranslator(boolean catalog, boolean schema, boolean table, boolean column) {
        super(catalog, schema, table, column);
    }

    public String translate(DistanceFunction fct, final String radius) throws TranslationException {
        StringBuffer str = new StringBuffer("q3c_join(");
        str.append(translate(fct.getP1())).append(",");
        str.append(translate(fct.getP2())).append(",");
        str.append(radius);
        str.append(")");
        str.append(" = 1"); 
        return str.toString();
    }

    @Override
    public String translate(ContainsFunction fct) throws TranslationException {
        return super.translate(fct);
    }

    @Override
    public String translate(IntersectsFunction fct) throws TranslationException {
        return super.translate(fct);
    }

    @Override
    public String translate(Comparison comp) throws TranslationException {
        if ((comp.getLeftOperand() instanceof ContainsFunction || comp.getLeftOperand() instanceof IntersectsFunction)
                &&
                (comp.getOperator() == ComparisonOperator.EQUAL || comp.getOperator() == ComparisonOperator.NOT_EQUAL)
                && comp.getRightOperand().isNumeric())
            return translate(comp.getLeftOperand()) + " " + comp.getOperator().toADQL() + " '"
                    + translate(comp.getRightOperand()) + "'";
        else if ((comp.getRightOperand() instanceof ContainsFunction
                || comp.getRightOperand() instanceof IntersectsFunction)
                && (comp.getOperator() == ComparisonOperator.EQUAL || comp.getOperator() == ComparisonOperator.NOT_EQUAL)
                && comp.getLeftOperand().isNumeric())
            return "'" + translate(comp.getLeftOperand()) + "' " + comp.getOperator().toADQL() + " "
                    + translate(comp.getRightOperand());
        else if ((comp.getLeftOperand() instanceof DistanceFunction)
                && (comp.getOperator() == ComparisonOperator.LESS_THAN || comp.getOperator() == ComparisonOperator.LESS_OR_EQUAL)
                && comp.getRightOperand().isNumeric())
            return translate((DistanceFunction) comp.getLeftOperand(), translate(comp.getRightOperand()).toString());
        else if ((comp.getRightOperand() instanceof DistanceFunction)
                && (comp.getOperator() == ComparisonOperator.LESS_THAN || comp.getOperator() == ComparisonOperator.LESS_OR_EQUAL)
                && comp.getLeftOperand().isNumeric())
            return translate((DistanceFunction) comp.getRightOperand(), translate(comp.getLeftOperand()).toString());
        else
            return super.translate(comp);
    }
}
