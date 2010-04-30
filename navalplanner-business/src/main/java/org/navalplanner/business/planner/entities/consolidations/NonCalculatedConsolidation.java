/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.planner.entities.consolidations;

import java.util.SortedSet;
import java.util.TreeSet;

import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;


/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class NonCalculatedConsolidation extends Consolidation {

    private SortedSet<NonCalculatedConsolidatedValue> consolidatedValues = new TreeSet<NonCalculatedConsolidatedValue>(
            new ConsolidatedValueComparator());

    private DirectAdvanceAssignment directAdvanceAssignment;

    public static NonCalculatedConsolidation create(
            DirectAdvanceAssignment directAdvanceAssignment) {
        return create(new NonCalculatedConsolidation(directAdvanceAssignment));
    }

    public static NonCalculatedConsolidation create(
            DirectAdvanceAssignment directAdvanceAssignment,
            SortedSet<NonCalculatedConsolidatedValue> consolidatedValues) {
        return create(new NonCalculatedConsolidation(directAdvanceAssignment,
                consolidatedValues));
    }

    protected NonCalculatedConsolidation() {

    }

    protected NonCalculatedConsolidation(
            DirectAdvanceAssignment directAdvanceAssignment,
            SortedSet<NonCalculatedConsolidatedValue> consolidatedValues) {
        this(directAdvanceAssignment);
        this.setConsolidatedValues(consolidatedValues);
    }

    public NonCalculatedConsolidation(
            DirectAdvanceAssignment directAdvanceAssignment) {
        this.directAdvanceAssignment = directAdvanceAssignment;
    }

    @Override
    public SortedSet<ConsolidatedValue> getConsolidatedValues() {
        return new TreeSet<ConsolidatedValue>(consolidatedValues);
    }

    public void setConsolidatedValues(
            SortedSet<NonCalculatedConsolidatedValue> consolidatedValues) {
        this.consolidatedValues = consolidatedValues;
    }

    public void setDirectAdvanceAssignment(
            DirectAdvanceAssignment directAdvanceAssignment) {
        this.directAdvanceAssignment = directAdvanceAssignment;
    }

    public DirectAdvanceAssignment getDirectAdvanceAssignment() {
        return directAdvanceAssignment;
    }

}