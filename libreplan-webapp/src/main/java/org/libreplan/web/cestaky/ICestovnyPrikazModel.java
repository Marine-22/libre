package org.libreplan.web.cestaky;

import java.util.Date;
import java.util.List;

import org.libreplan.business.cestaky.entities.CestovnyPrikaz;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.entities.User;

public interface ICestovnyPrikazModel {

	public void initCreate();
	
	public CestovnyPrikaz getCestovnyPrikaz();

	public void initEdit(CestovnyPrikaz entity);

	public void confirmSave();

	public void delete(CestovnyPrikaz entity) throws InstanceNotFoundException;

	public List<CestovnyPrikaz> getCestovnePrikazy(Date filterFrom, Date filterTo, User filterUser);

	public void rejectCestovnyPrikaz(CestovnyPrikaz cp);

	public void approveCestovnyPrikaz(CestovnyPrikaz cp);

}
