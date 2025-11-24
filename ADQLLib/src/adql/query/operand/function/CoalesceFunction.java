package adql.query.operand.function;

/*
 * This file is part of ADQLLibrary.
 *
 * ADQLLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ADQLLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ADQLLibrary.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2021 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import adql.query.ADQLObject;
import adql.query.operand.ADQLOperand;
import adql.query.operand.UnknownType;

import java.util.Arrays;
import java.util.Collection;

/**
 * <p>It represents the COALESCE function of ADQL.</p>
 *
 * <p>This function returns the first non-null argument.</p>
 *
 * <p><i><u>Example:</u><br/>COALESCE(utype, 'none').</i></p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.5 (12/2021)
 * @since 1.5
 */
public class CoalesceFunction extends ADQLFunction {

    protected final ADQLOperand[] operands;

    public CoalesceFunction(final ADQLOperand[] operands) throws Exception
    {
        // Create a new array of the same size:
        this.operands = new ADQLOperand[operands.length];

        // Append each item as a new operand (which will check it):
        for(int i=0; i< operands.length; i++)
            setParameter(i, operands[i]);
    }

    public CoalesceFunction(final Collection<ADQLOperand> operands) throws Exception
    {
        // Create a new array of the same size:
        this.operands = new ADQLOperand[operands.size()];

        // Append each item as a new operand (which will check it):
        int i = 0;
        for(ADQLOperand op : operands)
            setParameter(i++, op);
    }

    @Override
    public String getName() {
        return "COALESCE";
    }

    @Override
    public ADQLObject getCopy() throws Exception {
        return null;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean isGeometry() {
        return true;
    }

    @Override
    public int getNbParameters() {
        return operands.length;
    }

    @Override
    public ADQLOperand[] getParameters() {
        return Arrays.copyOf(operands, operands.length);
    }

    @Override
    public ADQLOperand getParameter(int index) throws ArrayIndexOutOfBoundsException {
        return operands[index];
    }

    @Override
    public ADQLOperand setParameter(int index, ADQLOperand replacer) throws ArrayIndexOutOfBoundsException, NullPointerException, Exception {
        // No NULL replacement:
        if (replacer == null)
            throw new NullPointerException("Impossible to replace a parameter of \"" + getName() + "\" by NULL!");

        // No replacement outside the existing array of arguments:
        if (index < 0 || index >= operands.length)
            throw new ArrayIndexOutOfBoundsException("No " + index + "-th parameter for the function \"" + getName() + "\"!");

        // Check the operand type is consistent with its neighbour argument:
        final ADQLOperand neighbour = getParameter((index == 0 ? 1 : index-1));
        if (neighbour != null){
            if (!(neighbour instanceof UnknownType) && !(replacer instanceof UnknownType)
                && (neighbour.isNumeric() != replacer.isNumeric() || neighbour.isString() != replacer.isString() || neighbour.isGeometry() != replacer.isGeometry()))
                throw new Exception("Impossible to set the following expression as argument of this \"" + getName() + "\" function: " + replacer.toADQL() + ". It must be of the same type as the other arguments.");
        }

        return operands[index] = replacer;
    }
}

