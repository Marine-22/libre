package org.libreplan.business.holidays.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.users.entities.User;

public class Holiday extends BaseEntity implements IHumanIdentifiable, Comparable<Holiday>{

	protected Holiday(){}
	
	public static Holiday create(){
		return (Holiday) create(new Holiday());
	}
	
	private User ziadatel;
	private Date from;
	private Date to;
	private HolidayState state;
	private String note;
	

	public User getZiadatel() {
		return ziadatel;
	}

	public void setZiadatel(User ziadatel) {
		this.ziadatel = ziadatel;
	}

	public Date getFrom() {
		return from;
	}
	public void setFrom(Date from) {
		this.from = from;
	}
	public Date getTo() {
		return to;
	}
	public void setTo(Date to) {
		this.to = to;
	}
	public HolidayState getState() {
		return state;
	}
	public void setState(HolidayState state) {
		this.state = state;
	}
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String getHumanId() {
		if(from == null || to == null) return "NEW";
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		return "from " + sdf.format(from) + " to " + sdf.format(to);
	}
	
	@Override
	public int compareTo(Holiday o) {
		return (int)((this.from.getTime()/1000) - (o.from.getTime()/1000));
	}
	
	public String toString(){
		return "Holiday: " + getHumanId() + " note: " + note;
	}
	
	public String getInfoText(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		return ziadatel.getFullName() + " " + sdf.format(from) + "-" + sdf.format(to);
	}

}
