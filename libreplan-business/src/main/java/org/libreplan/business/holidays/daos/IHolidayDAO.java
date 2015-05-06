package org.libreplan.business.holidays.daos;

import java.util.Date;
import java.util.List;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.holidays.entities.Holiday;
import org.libreplan.business.users.entities.User;

public interface IHolidayDAO extends IGenericDAO<Holiday, Long> {

	List<Holiday> list(Date filterFrom, Date filterTo, User filterUser);
}
