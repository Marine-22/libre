package org.libreplan.web.holidays;

import static org.libreplan.web.I18nHelper._;

import java.net.URL;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.holidays.entities.Holiday;
import org.libreplan.business.holidays.entities.HolidayState;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.UserUtil;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;

public class HolidaysCRUDController extends BaseCRUDController<Holiday> implements EventListener{

	private static final long serialVersionUID = 2819554270058315937L;
	
	private IHolidaysModel holidaysModel;
	private boolean hasRight;
	private Date filterFrom;
	private Date filterTo;
	private Autocomplete filterResource;
	
	@Autowired
	private MailSender mailSender;

	@SuppressWarnings("unused")
	private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(HolidaysCRUDController.class);
	
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);
        hasRight = SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_HOLIDAY_APPROVING);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        filterFrom = c.getTime();
        c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        filterTo = c.getTime();
        
        filterResource = (Autocomplete) listWindow.getFellowIfAny("filterResource");
        filterResource.addEventListener("onChange", this);
    }
    
    public Holiday getHoliday(){
    	return holidaysModel.getHoliday();
    }
    
    public boolean hasRight(){
    	return hasRight;
    }
    
    public List<Holiday> getHolidays(){
    	User filterUser = null;
    	Worker selectedWorker = getSelectedResource();
    	if(selectedWorker == null){
    		if(!hasRight)
    			filterUser = UserUtil.getUserFromSession();
    	}
    	else{
    		filterUser = selectedWorker.getUser();
    	}
    	return holidaysModel.getHolidays(filterFrom, filterTo, filterUser);
    }
    
    private Worker getSelectedResource() {
        Comboitem itemSelected = filterResource.getSelectedItem();
        if ((itemSelected != null) && (((Worker) itemSelected.getValue()) != null)) {
            return (Worker) itemSelected.getValue();
        }
        return null;
    }

	@Override
	protected String getEntityType() {
		return _("Holiday");
	}

	@Override
	protected String getPluralEntityType() {
		return _("Holidays");
	}

	@Override
	protected void initCreate() {
		holidaysModel.initCreate();
		getHoliday().setState(HolidayState.ZADANA);
		getHoliday().setZiadatel(UserUtil.getUserFromSession());
	}

	@Override
	protected void initEdit(Holiday entity) {
		holidaysModel.initEdit(entity);
	}

	@Override
	protected void save() throws ValidationException {
		Holiday h = getEntityBeingEdited();
		holidaysModel.confirmSave();
		List<User> l = UserUtil.getUsersWithRole(UserRole.ROLE_HOLIDAY_APPROVING);
		if(!l.isEmpty()){
			List<String> to = new ArrayList<String>();
			for(User u : l){
				if(u.getEmail() != null && !"".equals(u.getEmail()))
					to.add(u.getEmail());
			}
			LOG.info("Pouzivatelia na poslanie mailu: " + to);
			
			SimpleMailMessage smm = new SimpleMailMessage();
			smm.setTo(to.toArray(new String[0]));
			smm.setSubject("Nová žiadosť o dovolenku od " + h.getZiadatel().getFullName());
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			smm.setText("Vážený schvalovateľ dovoleniek!\n\nPracovník " + h.getZiadatel().getFullName() +
					" požiadal o dovolenku v termíne od " + sdf.format(h.getFrom()) + " do " + sdf.format(h.getTo())
					+ ".\n\nProsím schválte/zamietnite túto požiadavku:\nhttp://external.iquap.com/libreplan-webapp/holidays/holidays.zul");
			mailSender.send(smm);
		}
	}

	@Override
	protected Holiday getEntityBeingEdited() {
		return holidaysModel.getHoliday();
		
	}

	@Override
	protected void delete(Holiday entity) throws InstanceNotFoundException {
		holidaysModel.deleteHoliday(entity);
	}

	public Date getFilterFrom() {
		return filterFrom;
	}

	public void setFilterFrom(Date filterFrom) {
		this.filterFrom = filterFrom;
	}

	public Date getFilterTo() {
		return filterTo;
	}

	public void setFilterTo(Date filterTo) {
		this.filterTo = filterTo;
	}
	
	public void approveRequest(Holiday h){
		if(h.getState() == HolidayState.SCHVALENA) return;
		try{
			if(Messagebox.show(_("Approve holliday request \n\"{0}\".\nAre you sure?", h.getInfoText()),
					_("Confirm"), Messagebox.OK | Messagebox.CANCEL,
					Messagebox.QUESTION) == Messagebox.OK){
				holidaysModel.approveHoliday(h);
				showListWindow();
			}
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
	
	public void rejectRequest(Holiday h){
		if(h.getState() == HolidayState.ZAMIETNUTA) return;
		try{
			if(Messagebox.show(_("Reject holliday request \n\"{0}\".\nAre you sure?", h.getInfoText()),
					_("Confirm"), Messagebox.OK | Messagebox.CANCEL,
					Messagebox.QUESTION) == Messagebox.OK){
				holidaysModel.rejectHoliday(h);
				showListWindow();
			}
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onEvent(Event evt) throws Exception {
		super.onEvent(evt);
		showListWindow();
	}
	
}
