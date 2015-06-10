package org.libreplan.business.cestaky.entities;

public enum CestovnyProstriedok {

	VlastneAuto("Own Car"),
	SluzobneAuto("Company Car"),
	Autobus("Bus"),
	Vlak("Train"),
	Lietadlo("Airplain"),
	Ponorka("Yellow submarine");
	
	private String name;
	private CestovnyProstriedok(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
