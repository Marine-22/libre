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

package org.zkoss.ganttz.limitingresources;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.Constraint.IConstraintViolationListener;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;

/**
 *
 * @author Francisco Javier Moran Rúa <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class LimitingDependencyComponent extends XulElement implements
        AfterCompose {

    private Div source;

    private Div destination;

    private DependencyType type;

    // private Dependency dependency;

    private IConstraintViolationListener<Date> violationListener;

    public LimitingDependencyComponent(Div source, Div destination) {
        Validate.notNull(source);
        Validate.notNull(destination);
        // Validate.isTrue(source.getTask() == dependency.getSource());
        // Validate.isTrue(destination.getTask() ==
        // dependency.getDestination());
        // this.type = dependency.getType();
        this.source = source;
        this.destination = destination;
        // this.dependency = dependency;
        violationListener = new IConstraintViolationListener<Date>() {

            @Override
            public void constraintViolated(Constraint<Date> constraint,
                    Date value) {
                // TODO mark graphically dependency as violated
            }
        };
        // this.dependency.addConstraintViolationListener(violationListener);
    }

    @Override
    public void afterCompose() {
        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                redrawDependency();
            }
        };
        // this.source.getTask().addFundamentalPropertiesChangeListener(listener);
        // this.destination.getTask().addFundamentalPropertiesChangeListener(listener);
    }

    /**
     * @return the idTaskOrig
     */
    public String getIdTaskOrig() {
        return source.getUuid();
    }

    public void setIdTaskOrig(String idTaskOrig) {
        this.source = findTaskComponent(idTaskOrig);

    }

    private TaskComponent findTaskComponent(String idTaskOrig) {
        return (TaskComponent) getFellow(idTaskOrig);
    }

    /**
     * @return the idTaskEnd
     */
    public String getIdTaskEnd() {
        return destination.getUuid();
    }

    public void setIdTaskEnd(String idTaskEnd) {
        this.destination = findTaskComponent(idTaskEnd);
    }

    public void zoomChanged() {
        redrawDependency();
    }

    public void redrawDependency() {
        response("zoomChanged", new AuInvoke(this, "draw"));
    }

    public boolean contains(Task task) {
        return false;
        // Task sourceTask = getSource().getTask();
        // Task destinationTask = getDestination().getTask();
        // return task.equals(sourceTask) || task.equals(destinationTask);
    }

    public Div getSource() {
        return source;
    }

    public Div getDestination() {
        return destination;
    }

    // public Dependency getDependency() {
    // return dependency;
    // }

    public DependencyType getDependencyType() {
        return type;
    }

    public boolean hasSameSourceAndDestination(Dependency dependency) {
        return false;
        // Task sourceTask = source.getTask();
        // Task destinationTask = destination.getTask();
        // return sourceTask.equals(dependency.getSource())
        // && destinationTask.equals(dependency.getDestination());
    }

}