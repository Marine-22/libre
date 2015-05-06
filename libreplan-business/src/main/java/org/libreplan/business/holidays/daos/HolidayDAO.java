package org.libreplan.business.holidays.daos;


import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.holidays.entities.Holiday;
import org.libreplan.business.users.entities.User;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HolidayDAO  extends GenericDAOHibernate<Holiday, Long> implements IHolidayDAO{
	
	@Override
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public List<Holiday> list(Date filterFrom, Date filterTo, User filterUser) {
		Session session = getSession();
		Query q = session.createQuery(
				"from Holiday h where h.from < :dateTo and h.to > :dateFrom" + 
				(filterUser == null ? "" : " and h.ziadatel = :ziadatel"))
				.setParameter("dateTo", filterTo)
				.setParameter("dateFrom", filterFrom);
		q = (filterUser == null ? q : q.setParameter("ziadatel", filterUser));
		return (List<Holiday>) q.list();
	}
	
}
