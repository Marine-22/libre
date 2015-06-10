package org.libreplan.business.cestaky.daos;

import java.util.Date;
import java.util.List;

import org.libreplan.business.cestaky.entities.CestovnyPrikaz;
import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.users.entities.User;

public interface ICestovnyPrikazDAO extends IGenericDAO<CestovnyPrikaz, Long>{

	List<CestovnyPrikaz> list(Date filterFrom, Date filterTo, User filterUser);

}
