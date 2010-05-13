/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galic
 *                    ia
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

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.DatesMapperOnInterval;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;

/**
 * This class wraps ResourceLoad data inside an specific HTML Div component.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class QueueComponent extends XulElement implements
        AfterCompose {

    public static QueueComponent create(TimeTracker timeTracker,
            LimitingResourceQueue limitingResourceQueue) {
        return new QueueComponent(timeTracker,
                limitingResourceQueue);
    }

    private final LimitingResourceQueue limitingResourceQueue;
    private final TimeTracker timeTracker;
    private transient IZoomLevelChangedListener zoomChangedListener;
    private List<QueueTask> queueTasks = new ArrayList<QueueTask>();

    public List<QueueTask> getQueueTasks() {
        return queueTasks;
    }

    private QueueComponent(final TimeTracker timeTracker,
            final LimitingResourceQueue limitingResourceQueue) {
        this.limitingResourceQueue = limitingResourceQueue;
        this.timeTracker = timeTracker;
        createChildren(limitingResourceQueue, timeTracker.getMapper());
        zoomChangedListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                getChildren().clear();
                createChildren(limitingResourceQueue, timeTracker.getMapper());
                invalidate();
            }
        };
        this.timeTracker.addZoomListener(zoomChangedListener);
    }

    private void createChildren(LimitingResourceQueue limitingResourceQueue,
            IDatesMapper mapper) {
        List<QueueTask> queueTasks = createQueueTasks(mapper,
                limitingResourceQueue.getLimitingResourceQueueElements());
        if (queueTasks != null) {
            appendQueueTasks(queueTasks);
        }
    }

    private void appendQueueTasks(List<QueueTask> queueTasks) {
        for (QueueTask each: queueTasks) {
            appendQueueTask(each);
        }
    }

    private void appendQueueTask(QueueTask queueTask) {
        queueTasks.add(queueTask);
        appendChild(queueTask);
    }

    private static List<QueueTask> createQueueTasks(
            IDatesMapper datesMapper,
            Set<LimitingResourceQueueElement> list) {

        List<QueueTask> result = new ArrayList<QueueTask>();
        for (LimitingResourceQueueElement each : list) {
            result.add(createQueueTask(datesMapper, each));
        }
        return result;
    }

    private static QueueTask createQueueTask(IDatesMapper datesMapper, LimitingResourceQueueElement element) {
        validateQueueElement(element);
        return createDivForElement(datesMapper, element);
    }

    private static QueueTask createDivForElement(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {

        QueueTask result = new QueueTask(queueElement);
        result.setClass("queue-element");

        result.setTooltiptext(queueElement.getLimitingResourceQueue()
                .getResource().getName());

        result.setLeft(forCSS(getStartPixels(datesMapper, queueElement)));
        result.setWidth(forCSS(getWidthPixels(datesMapper, queueElement)));

        result.appendChild(generateNonWorkableShade(datesMapper, queueElement));

        return result;
    }

    private static Div generateNonWorkableShade(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {
        int workableHours = queueElement.getResource().getCalendar()
                .getCapacityAt(queueElement.getEndDate());

        int shadeWidth = new Long((24 - workableHours)
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR
                / datesMapper.getMilisecondsPerPixel()).intValue();

        int shadeLeft = new Long((workableHours - queueElement.getEndHour())
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR
                / datesMapper.getMilisecondsPerPixel()).intValue()
                + shadeWidth;
        ;

        Div notWorkableHoursShade = new Div();
        notWorkableHoursShade
                .setTooltiptext(_("Workable capacity for this period ")
                        + workableHours + _(" hours"));

        notWorkableHoursShade.setContext("");
        notWorkableHoursShade.setSclass("not-workable-hours");

        notWorkableHoursShade.setStyle("left: " + shadeLeft + "px; width: "
                + shadeWidth + "px;");
        return notWorkableHoursShade;
    }

    private static int getWidthPixels(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {
        return datesMapper.toPixels(getEndMillis(queueElement)
                - getStartMillis(queueElement));
    }

    private static long getStartMillis(LimitingResourceQueueElement queueElement) {
        return queueElement.getStartDate().toDateMidnight().getMillis()
                + (queueElement.getStartHour() * DatesMapperOnInterval.MILISECONDS_PER_HOUR);
    }

    private static long getEndMillis(LimitingResourceQueueElement queueElement) {
        return queueElement.getEndDate().toDateMidnight().getMillis()
                + (queueElement.getEndHour() * DatesMapperOnInterval.MILISECONDS_PER_HOUR);
    }

    private static String forCSS(int pixels) {
        return String.format("%dpx", pixels);
    }

    private static int getStartPixels(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {
        return datesMapper
                .toPixelsAbsolute((queueElement.getStartDate().toDateMidnight()
.getMillis() + queueElement.getStartHour()
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR));
    }

    public void appendQueueElement(LimitingResourceQueueElement element) {
        QueueTask queueTask = createQueueTask(element);
        appendQueueTask(queueTask);
        appendMenu(queueTask);
    }

    private QueueTask createQueueTask(LimitingResourceQueueElement element) {
        validateQueueElement(element);
        return createDivForElement(timeTracker.getMapper(), element);
    }

    public String getResourceName() {
        return limitingResourceQueue.getResource().getName();
    }

    private static void validateQueueElement(
            LimitingResourceQueueElement queueElement) {
        if ((queueElement.getStartDate() == null)
                || (queueElement.getEndDate() == null)) {
            throw new ValidationException(_("Invalid queue element"));
        }
    }

    private void appendMenu(QueueTask divElement) {
        if (divElement.getPage() != null) {
            MenuBuilder<QueueTask> menuBuilder = MenuBuilder.on(divElement
                    .getPage(), divElement);
            menuBuilder.item(_("Unassign"), "/common/img/ico_borrar.png",
                    new ItemAction<QueueTask>() {
                        @Override
                        public void onEvent(QueueTask choosen, Event event) {
                            unnasign(choosen);
                        }
                    });
            divElement.setContext(menuBuilder.createWithoutSettingContext());
        }
    }

    private void unnasign(QueueTask choosen) {
        final LimitingResourcesPanel panel = LimitingResourcesPanel
                .getLimitingResourcesPanel(choosen.getParent());
        panel.unschedule(choosen);
    }

    private void appendContextMenus() {
        for (QueueTask each : queueTasks) {
            appendMenu(each);
        }
    }

    @Override
    public void afterCompose() {
        appendContextMenus();
    }

}