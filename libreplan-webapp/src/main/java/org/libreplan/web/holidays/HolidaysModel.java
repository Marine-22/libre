package org.libreplan.web.holidays;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.holidays.daos.IHolidayDAO;
import org.libreplan.business.holidays.entities.Holiday;
import org.libreplan.business.holidays.entities.HolidayState;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/holidays/holidays.zul")
public class HolidaysModel implements IHolidaysModel{
	
	@SuppressWarnings("unused")
	private static final org.apache.commons.logging.Log LOG = LogFactory
	            .getLog(HolidaysModel.class);
	   
	@Autowired
	private IHolidayDAO holidaysDAO;
	
	private Holiday holiday;
	
	
	@Override
	public List<Holiday> getHolidays() {
		return holidaysDAO.list(Holiday.class);
	}
	
	@Override
	public void initCreate() {
		holiday = Holiday.create();
	}
	
	@Override
    @Transactional(readOnly = true)
	public void initEdit(Holiday entity) {
        Validate.notNull(entity);
        holiday = getFromDB(entity);
	}
	
    @Transactional(readOnly = true)
    private Holiday getFromDB(Holiday entity) {
        return getFromDB(entity.getId());
    }

    @Transactional(readOnly = true)
    private Holiday getFromDB(Long id) {
        try {
        	Holiday result = holidaysDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Load entities that will be needed in the conversation
     *
     * @param company
     */
    private void forceLoadEntities(Holiday entity) {
        if(entity.getZiadatel() != null) {
        	entity.getZiadatel().getLoginName();
        }
    }
    
    @Override
    @Transactional
    public void confirmSave() {
    	holidaysDAO.save(holiday);
    }
    
    @Override
    @Transactional
    public boolean deleteHoliday(Holiday entity) {
        try {
        	holidaysDAO.remove(entity.getId());
        } catch (InstanceNotFoundException e) {
            return false;
        }
        return true;
    }
    
    @Override
    public Holiday getHoliday() {
    	return holiday;
    }
    
    @Override
    public List<Holiday> getHolidays(Date filterFrom, Date filterTo, User filterUser) {
    	return holidaysDAO.list(filterFrom, filterTo, filterUser);
    }
    
    @Transactional
    public void approveHoliday(Holiday h) {
    	h.setState(HolidayState.SCHVALENA);
    	holidaysDAO.reattach(h);
    }
    
    @Transactional
    public void rejectHoliday(Holiday h) {
    	h.setState(HolidayState.ZAMIETNUTA);
    	holidaysDAO.reattach(h);
    }
}
