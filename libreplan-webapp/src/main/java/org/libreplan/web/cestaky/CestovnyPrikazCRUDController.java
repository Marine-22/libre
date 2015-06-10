package org.libreplan.web.cestaky;

import static org.libreplan.web.I18nHelper._;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.cestaky.entities.CestovnyPrikaz;
import org.libreplan.business.cestaky.entities.CestovnyPrikazState;
import org.libreplan.business.cestaky.entities.CestovnyProstriedok;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.holidays.entities.Holiday;
import org.libreplan.business.holidays.entities.HolidayState;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.business.users.entities.User.UserAuthenticationType;
import org.libreplan.web.UserUtil;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.common.components.NewDataSortableGrid;
import org.libreplan.web.holidays.HolidaysCRUDController;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;

public class CestovnyPrikazCRUDController extends BaseCRUDController<CestovnyPrikaz> implements EventListener{

	private static final long serialVersionUID = 1852557783984590484L;

	private ICestovnyPrikazModel cestovnyPrikazModel;
	private Date filterFrom;
	private Date filterTo;
	private Autocomplete filterResource;
	private NewDataSortableGrid table;
	
	private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(CestovnyPrikazCRUDController.class);
	
	@Autowired
	private MailSender mailSender;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
        comp.setAttribute("controller", this);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        filterFrom = c.getTime();
        c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        filterTo = c.getTime();
        filterResource = (Autocomplete) listWindow.getFellowIfAny("filterResource");
        filterResource.addEventListener("onChange", this);
        table = (NewDataSortableGrid) listWindow.getFellowIfAny("listing");
	}
	
    private Combobox prepareTransportTypesCombo() {
        Combobox combo = (Combobox) editWindow
                .getFellowIfAny("transportTypeTravelClaim");
        combo.getChildren().clear();
        for (CestovnyProstriedok prostriedok : CestovnyProstriedok.values()) {
            Comboitem item = combo.appendItem(_(prostriedok.getName()));
            item.setValue(prostriedok);
        }
        return combo;
    }
    
    public void setTransportType(Comboitem ci){
    	CestovnyProstriedok cp = (CestovnyProstriedok)ci.getValue();
    	getCestovnyPrikaz().setProstriedok(cp);
    }
	
	public CestovnyPrikaz getCestovnyPrikaz(){
		return cestovnyPrikazModel.getCestovnyPrikaz();
	}
	
    public List<CestovnyPrikaz> getCestovnePrikazy(){
    	User filterUser = null;
    	Worker selectedWorker = getSelectedResource();
    	if(selectedWorker == null){
    		if(!canSeeAll())
    			filterUser = UserUtil.getUserFromSession();
    	}
    	else{
    		filterUser = selectedWorker.getUser();
    	}
    	return cestovnyPrikazModel.getCestovnePrikazy(filterFrom, filterTo, filterUser);
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
		return _("Travel Claim");
	}
	
	public void onChange(){
		table.setModel(new ListModelArray(getCestovnePrikazy()));
	}

	@Override
	protected String getPluralEntityType() {
		return _("Travel Claims");
	}

	@Override
	protected void initCreate() {
		cestovnyPrikazModel.initCreate();
		getCestovnyPrikaz().setStav(CestovnyPrikazState.Zadana);
		getCestovnyPrikaz().setProstriedok(CestovnyProstriedok.SluzobneAuto);
		getCestovnyPrikaz().setZiadatel(UserUtil.getUserFromSession());
		Combobox cb = prepareTransportTypesCombo();
		setComboboxState(cb, CestovnyProstriedok.SluzobneAuto);
	}

	@Override
	protected void initEdit(CestovnyPrikaz entity) {
		Combobox cb = prepareTransportTypesCombo();
		cestovnyPrikazModel.initEdit(entity);
		setComboboxState(cb, entity.getProstriedok());
	}

	private void setComboboxState(Combobox cb, CestovnyProstriedok cp){
		for(Object o : cb.getItems()){
			Comboitem ci = (Comboitem)o;
			LOG.info("Comboitem: " + ci + "; Value: "+ci.getValue()+"; CestovnyProstriedok: " + cp); 
			if(ci.getValue().equals(cp)){
				cb.setSelectedItem(ci);
				return;
			}
		}
	}
	
	@Override
	protected void save() throws ValidationException {
		cestovnyPrikazModel.confirmSave();
		CestovnyPrikaz cp = getEntityBeingEdited();
		List<User> l = UserUtil.getUsersWithRole(UserRole.ROLE_CAN_DECIDE_TRAVEL_CLAIMS);
		if(!l.isEmpty()){
			List<String> to = new ArrayList<String>();
			for(User u : l){
				if(u.getEmail() != null && !"".equals(u.getEmail()))
					to.add(u.getEmail());
			}
			sendMailCreated(to.toArray(new String[0]), cp.getZiadatel().getFullName(), cp.getFullFrom(), cp.getFullTo());
		}
		
		// mail o vytvoreni
	}

	@Override
	protected CestovnyPrikaz getEntityBeingEdited() {
		return cestovnyPrikazModel.getCestovnyPrikaz();
	}

	@Override
	protected void delete(CestovnyPrikaz entity) throws InstanceNotFoundException {
		cestovnyPrikazModel.delete(entity);
		
	}

	public boolean canSeeAll(){
		return SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_CAN_SEE_ALL_TRAVEL_CLAIMS);
	}
	
	public boolean canDecide(){
		return SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_CAN_DECIDE_TRAVEL_CLAIMS);
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
	
	public void approveRequest(CestovnyPrikaz cp){
		if(cp.getStav() == CestovnyPrikazState.Schvalena) return;
		try{
			if(Messagebox.show(_("Approve travel claim request \n\"{0}\".\nAre you sure?", cp.getInfoText()),
					_("Confirm"), Messagebox.OK | Messagebox.CANCEL,
					Messagebox.QUESTION) == Messagebox.OK){
				cestovnyPrikazModel.approveCestovnyPrikaz(cp);
				sendMailApproved(cp.getZiadatel().getEmail(), cp.getFullFrom(), cp.getFullTo());
				showListWindow();
			}
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
	
	public void rejectRequest(CestovnyPrikaz cp){
		if(cp.getStav() == CestovnyPrikazState.Zamietnuta) return;
		try{
			if(Messagebox.show(_("Reject travel claim request \n\"{0}\".\nAre you sure?", cp.getInfoText()),
					_("Confirm"), Messagebox.OK | Messagebox.CANCEL,
					Messagebox.QUESTION) == Messagebox.OK){
				cestovnyPrikazModel.rejectCestovnyPrikaz(cp);
				sendMailRejected(cp.getZiadatel().getEmail(), cp.getFullFrom(), cp.getFullTo());
				showListWindow();
			}
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
	
	private void sendMailCreated(String[] recip, String ziadatel, String from, String to){
		if(recip == null || recip.length == 0) return;
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setTo(recip);
		smm.setSubject("Nová žiadosť o cestovný príkaz od " + ziadatel);
		smm.setText("Vážený schvalovateľ cestovných príkazov!\n\nPracovník " + ziadatel +
				" zadal cestovný príkaz " + from + " -> " + to
				+ ".\n\nProsím schválte/zamietnite túto požiadavku:\nhttp://external.iquap.com/libreplan/cestaky/cestaky.zul");
		mailSender.send(smm);
	}

	private void sendMailApproved(String recip, String from, String to){
		if(recip == null || "".equals(recip)) return;
		SimpleMailMessage smm = new SimpleMailMessage();
		StringBuffer sb = new StringBuffer();
		

		smm.setTo(recip);
		smm.setSubject("Oznam o schválení cestovného príkazu");
		
		sb.append("Vážený zadávateľ cestovného príkazu,\n\nVáš cestovný príkaz ");
		sb.append(from);
		sb.append(" -> ");
		sb.append(to);
		sb.append(" bol schválený.");
		sb.append("\n\n Prehľad cestovných príkazov nájdete na stránke: http://external.iquap.com/libreplan/cestaky/cestaky.zul");
		smm.setText(sb.toString());
		
		mailSender.send(smm);
	}

	private void sendMailRejected(String recip, String from, String to){
		if(recip == null || "".equals(recip)) return;
		SimpleMailMessage smm = new SimpleMailMessage();
		StringBuffer sb = new StringBuffer();
		

		smm.setTo(recip);
		smm.setSubject("Oznam o zamietnutí dovolenky");
		
		sb.append("Vážený zadávateľ cestovného príkazu,\n\nVáš cestovný príkaz ");
		sb.append(from);
		sb.append(" -> ");
		sb.append(to);
		sb.append(" bol zamietnutý.");
		sb.append("\n\n Prehľad cestovných príkazov nájdete na stránke: http://external.iquap.com/libreplan/cestaky/cestaky.zul");
		smm.setText(sb.toString());
		
		mailSender.send(smm);
	}
}
