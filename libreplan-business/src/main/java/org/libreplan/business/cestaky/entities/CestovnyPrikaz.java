package org.libreplan.business.cestaky.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.users.entities.User;

public class CestovnyPrikaz extends BaseEntity implements IHumanIdentifiable, Comparable<CestovnyPrikaz>{


	protected CestovnyPrikaz(){}
	
	public static CestovnyPrikaz create(){
		return (CestovnyPrikaz) create(new CestovnyPrikaz());
	}
	
	private User ziadatel;
	private String zaciatokMiesto;
	private String koniecMiesto;
	private Date zaciatokDatum;
	private Date koniecDatum;
	private String ucelCesty;
	private String spolucestujuci;
	private CestovnyProstriedok prostriedok;
	private CestovnyPrikazState stav;
	
	public User getZiadatel() {
		return ziadatel;
	}

	public void setZiadatel(User ziadatel) {
		this.ziadatel = ziadatel;
	}

	public String getZaciatokMiesto() {
		return zaciatokMiesto;
	}

	public void setZaciatokMiesto(String zaciatokMiesto) {
		this.zaciatokMiesto = zaciatokMiesto;
	}

	public String getKoniecMiesto() {
		return koniecMiesto;
	}

	public void setKoniecMiesto(String koniecMiesto) {
		this.koniecMiesto = koniecMiesto;
	}

	public Date getZaciatokDatum() {
		return zaciatokDatum;
	}

	public void setZaciatokDatum(Date zaciatokDatum) {
		this.zaciatokDatum = zaciatokDatum;
	}

	public Date getKoniecDatum() {
		return koniecDatum;
	}

	public void setKoniecDatum(Date koniecDatum) {
		this.koniecDatum = koniecDatum;
	}

	public String getUcelCesty() {
		return ucelCesty;
	}

	public void setUcelCesty(String ucelCesty) {
		this.ucelCesty = ucelCesty;
	}

	public String getSpolucestujuci() {
		return spolucestujuci;
	}

	public void setSpolucestujuci(String spolucestujuci) {
		this.spolucestujuci = spolucestujuci;
	}

	public CestovnyProstriedok getProstriedok() {
		return prostriedok;
	}

	public void setProstriedok(CestovnyProstriedok prostriedok) {
		this.prostriedok = prostriedok;
	}

	public CestovnyPrikazState getStav() {
		return stav;
	}

	public void setStav(CestovnyPrikazState stav) {
		this.stav = stav;
	}
	
	public String getInfoText(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		return ziadatel.getFullName() + " " + zaciatokMiesto + "-" + sdf.format(zaciatokDatum) + " -> " + koniecMiesto + "-" + sdf.format(koniecDatum);
	}

	@Override
	public String getHumanId() {
		if(zaciatokDatum == null || koniecDatum == null) return "NEW";
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		return "CestovnyPrikaz: " + ziadatel.getFullName() + " od:" + sdf.format(zaciatokDatum) + 
				" do:" + sdf.format(koniecDatum) + " " + zaciatokMiesto + "-" + koniecMiesto;
	}

	public String getFullFrom() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		return zaciatokMiesto + "-" + sdf.format(zaciatokDatum);
	}

	public String getFullTo() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		return koniecMiesto + "-" + sdf.format(koniecDatum);
	}
	
	@Override
	public int compareTo(CestovnyPrikaz o) {
		return this.zaciatokDatum.compareTo(o.getZaciatokDatum());
	}
}
