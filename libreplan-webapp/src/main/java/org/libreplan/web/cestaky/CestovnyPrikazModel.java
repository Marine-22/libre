package org.libreplan.web.cestaky;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.libreplan.business.cestaky.daos.ICestovnyPrikazDAO;
import org.libreplan.business.cestaky.entities.CestovnyPrikaz;
import org.libreplan.business.cestaky.entities.CestovnyPrikazState;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/cestaky/cestaky.zul")
public class CestovnyPrikazModel implements ICestovnyPrikazModel {

	@Autowired
	private ICestovnyPrikazDAO cestovnyPrikazDAO;
	
	private CestovnyPrikaz cestovnyPrikaz;
	
	
	@Override
	public void initCreate() {
		cestovnyPrikaz = CestovnyPrikaz.create();
	}
	
	@Override
	public CestovnyPrikaz getCestovnyPrikaz() {
		return cestovnyPrikaz;
	}
	
	@Override
    @Transactional(readOnly = true)
	public void initEdit(CestovnyPrikaz entity) {
        Validate.notNull(entity);
        cestovnyPrikaz = getFromDB(entity);
	}
	
    @Transactional(readOnly = true)
    private CestovnyPrikaz getFromDB(CestovnyPrikaz entity) {
        return getFromDB(entity.getId());
    }
    
    @Transactional(readOnly = true)
    private CestovnyPrikaz getFromDB(Long id) {
        try {
        	CestovnyPrikaz result = cestovnyPrikazDAO.find(id);
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
    private void forceLoadEntities(CestovnyPrikaz entity) {
        if(entity.getZiadatel() != null) {
        	entity.getZiadatel().getLoginName();
        }
    }
    
    @Override
    @Transactional
    public void confirmSave() {
    	cestovnyPrikazDAO.save(cestovnyPrikaz);
    }
    
    @Override
    public void delete(CestovnyPrikaz entity) throws InstanceNotFoundException {
    	cestovnyPrikazDAO.remove(entity.getId());
    }
    
    @Override
    public List<CestovnyPrikaz> getCestovnePrikazy(Date filterFrom, Date filterTo, User filterUser) {
    	return cestovnyPrikazDAO.list(filterFrom, filterTo, filterUser);
    }
    
    @Override
    @Transactional
    public void approveCestovnyPrikaz(CestovnyPrikaz cp) {
    	cp.setStav(CestovnyPrikazState.Schvalena);
    	cestovnyPrikazDAO.reattach(cp);
    }
    
    @Override
    @Transactional
    public void rejectCestovnyPrikaz(CestovnyPrikaz cp) {
    	cp.setStav(CestovnyPrikazState.Zamietnuta);
    	cestovnyPrikazDAO.reattach(cp);
    }
}
