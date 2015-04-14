package org.libreplan.business.holidays.entities;

import java.util.Date;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.resources.entities.Worker;

public class Holiday extends BaseEntity implements IHumanIdentifiable{

	protected Holiday(){}
	
	public static Holiday getHoliday(){
		return new Holiday();
	}
	
	private Worker ziadatel;
	private Date from;
	private Date to;
	private HolidayState state;
	
	public Worker getZiadatel() {
		return ziadatel;
	}
	public void setZiadatel(Worker ziadatel) {
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
	public enum HolidayState{
		ZADANA,
		SCHVALENA,
		ZAMIETNUTA
	}
	
	@Override
	public String getHumanId() {
		return null;
	}
}
