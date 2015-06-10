package org.libreplan.business.cestaky.daos;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.libreplan.business.cestaky.entities.CestovnyPrikaz;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.users.entities.User;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CestovnyPrikazDAO extends GenericDAOHibernate<CestovnyPrikaz, Long> 
								implements ICestovnyPrikazDAO {

	
	@Override
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public List<CestovnyPrikaz> list(Date filterFrom, Date filterTo, User filterUser) {
		Session session = getSession();
		Query q = session.createQuery(
				"from CestovnyPrikaz cp where 1=1 " + 
				(filterTo == null ? "" : "and cp.zaciatokDatum < :dateTo ") + 
				(filterFrom == null ? "" : "and cp.koniecDatum > :dateFrom ") + 
				(filterUser == null ? "" : "and cp.ziadatel = :ziadatel"));
		q = (filterTo == null ? q : q.setParameter("dateTo", getMaxFromDay(filterTo)));
		q = (filterFrom == null ? q : q.setParameter("dateFrom", getMinFromDay(filterFrom)));
		q = (filterUser == null ? q : q.setParameter("ziadatel", filterUser));
		return (List<CestovnyPrikaz>) q.list();
	}
	
	private Date getMinFromDay(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}
	
	
	private Date getMaxFromDay(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		return c.getTime();
	}

}
