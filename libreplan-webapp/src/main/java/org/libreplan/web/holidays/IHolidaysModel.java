package org.libreplan.web.holidays;

import java.util.Date;
import java.util.List;

import org.libreplan.business.holidays.entities.Holiday;
import org.libreplan.business.users.entities.User;

public interface IHolidaysModel {

	public void initCreate();

	public void initEdit(Holiday entity);

	public void confirmSave();

	public Holiday getHoliday();

	public boolean deleteHoliday(Holiday entity);

	public List<Holiday> getHolidays();

	public List<Holiday> getHolidays(Date filterFrom, Date filterTo,User filterUser);

	public void approveHoliday(Holiday h);

	public void rejectHoliday(Holiday h);

}
