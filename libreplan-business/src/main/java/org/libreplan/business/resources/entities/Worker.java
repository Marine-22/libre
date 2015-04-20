/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.resources.entities;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Transient;
import javax.validation.constraints.AssertTrue;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;

import javax.validation.Valid;

import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;

/**
 * This class models a worker.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class Worker extends Resource {


    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(Worker.class);
    
    public static Worker create() {
        return create(new Worker());
    }

    public static Worker create(String code) {
        return create(new Worker(), code);
    }

    public static Worker create(String firstName, String surname,
        String nif) {

        return create(new Worker(firstName, surname, nif));

    }

    public static Worker createUnvalidated(String code, String firstName,
        String surname, String nif) {

        Worker worker = create(new Worker(), code);

        worker.firstName = firstName;
        worker.surname = surname;
        worker.nif = nif;

        return worker;

    }

    public void updateUnvalidated(String firstName, String surname, String nif) {

        if (!StringUtils.isBlank(firstName)) {
            this.firstName = firstName;
        }

        if (!StringUtils.isBlank(surname)) {
            this.surname = surname;
        }

        if (!StringUtils.isBlank(nif)) {
            this.nif = nif;
        }

    }

    private final static ResourceEnum type = ResourceEnum.WORKER;

    private String firstName;

    private String surname;

    private String nif;

    private User user;
    
    private Set<TypeOfWorkHours> typeOfWorkHours;
    
    @Transient
    private TypeOfWorkHours tempTowh;

    /**
     * Constructor for hibernate. Do not use!
     */
    public Worker() {

    }

    private Worker(String firstName, String surname, String nif) {
        this.firstName = firstName;
        this.surname = surname;
        this.nif = nif;
    }

    public String getDescription() {
        return getSurname() + "," + getFirstName();
    }

    @Override
    public String getShortDescription() {
        return getDescription() + " (" + getNif() + ")";
    }

    @NotEmpty(message="worker's first name not specified")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @NotEmpty(message="worker's surname not specified")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return getSurname() + ", " + getFirstName();
    }

    @NotEmpty(message="Worker ID cannot be empty")
    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public boolean isVirtual() {
        return false;
    }

    public boolean isReal() {
        return !isVirtual();
    }

    @AssertTrue(message = "ID already used. It has to be be unique")
    public boolean isUniqueFiscalCodeConstraint() {
        if (!areFirstNameSurnameNifSpecified()) {
            return true;
        }

        try {
        /* Check the constraint. */
            Worker worker = Registry.getWorkerDAO()
                    .findByNifAnotherTransaction(nif);
            if (isNewObject()) {
                return false;
            } else {
                return worker.getId().equals(getId());
            }
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

    protected boolean areFirstNameSurnameNifSpecified() {

       return !StringUtils.isBlank(firstName) &&
           !StringUtils.isBlank(surname) &&
           !StringUtils.isBlank(nif);

   }

   @Override
   protected boolean isCriterionSatisfactionOfCorrectType(
      CriterionSatisfaction c) {
        return c.getResourceType().equals(ResourceEnum.WORKER);

   }

    @Override
    public ResourceEnum getType() {
        return type;
    }

    @Override
    public String getHumanId() {
        if (firstName == null) {
            return surname;
        }
        if (surname == null) {
            return firstName;
        }
        return firstName + " " + surname;
    }

    @Valid
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.addRole(UserRole.ROLE_BOUND_USER);
        }
    }

    @AssertTrue(message = "User already bound to other worker")
    public boolean isUserNotBoundToOtherWorkerConstraint() {
        if (user == null || user.isNewObject()) {
            return true;
        }

        IUserDAO userDAO = Registry.getUserDAO();
        User foundUser = userDAO.findOnAnotherTransaction(user.getId());
        if (foundUser == null) {
            return true;
        }

        Worker worker = foundUser.getWorker();
        if (worker == null) {
            return true;
        }

        if (getId() == null) {
            return false;
        }

        return getId().equals(worker.getId());
    }

    @AssertTrue(message = "Queue-based resources cannot be bound to any user")
    public boolean isLimitingResourceNotBoundToUserConstraint() {
        if (isLimitingResource()) {
            return user == null;
        }
        return true;
    }

    @AssertTrue(message = "Virtual resources cannot be bound to any user")
    public boolean isVirtualResourceNotBoundToUserConstraint() {
        if (isVirtual()) {
            return user == null;
        }
        return true;
    }

    public void updateUserData() {
        if (user != null) {
            user.setFirstName(firstName);
            user.setLastName(surname);
        }
    }

    @AssertTrue(message = "Bound user does not have the proper role")
    public boolean isBoundUserHaveProperRoleConstraint() {
        if (user == null) {
            return true;
        }
        return user.getRoles().contains(UserRole.ROLE_BOUND_USER);
    }

	public Set<TypeOfWorkHours> getTypeOfWorkHours() {
		return typeOfWorkHours;
	}

	public void setTypeOfWorkHours(Set<TypeOfWorkHours> typeOfWorkHours) {
		this.typeOfWorkHours = typeOfWorkHours;
	}

	public TypeOfWorkHours getNewTypeOfHours(){
		LOG.info("getNewTypeOfHours. typeOfWorkHours = " + typeOfWorkHours + "; tempTowh=" + tempTowh);
		if(tempTowh != null) return tempTowh;
		if(this.typeOfWorkHours == null)
			this.typeOfWorkHours = new HashSet<TypeOfWorkHours>();

		tempTowh = TypeOfWorkHours.create();
		TypeOfWorkHours last = findTypOfHours(new Date());
		if(last != null){
			tempTowh.setValidFrom(last.getValidFrom());
			tempTowh.setDefaultPrice(last.getDefaultPrice());
		}
		tempTowh.setWorker(this);
		LOG.info("tempTowh = " + tempTowh + "; typeOfWorkHours = " + typeOfWorkHours);
		return tempTowh;
	}
	
	public void resolveTypeOfHours() {
		LOG.info("resolveTypeOfHours. typeOfWorkHours = " + typeOfWorkHours);
		LOG.info("tempTowh = " + tempTowh);
		boolean found = false;
		for(TypeOfWorkHours t : typeOfWorkHours){
			if(t.getValidFrom().getTime() == tempTowh.getValidFrom().getTime()){
				LOG.info("datumy sa rovnaju. t=" + t + " ;tempTowh=" + tempTowh);
				// nerobim pridanie ale zmenu existujucej
				t.setDefaultPrice(tempTowh.getDefaultPrice());
				found = true;
				break;
			}
		}
		if(!found) typeOfWorkHours.add(tempTowh);
		cleanTypeOfHours();
	}
	
	// nastavi mena entitam type of hours podla ich platnosti
	private void cleanTypeOfHours(){
		LOG.info("cleanTypeOfHours; typeOfWorkHours=" + typeOfWorkHours);
		List<TypeOfWorkHours> l = new ArrayList<TypeOfWorkHours>();
		l.addAll(typeOfWorkHours);
		
		Collections.sort(l, new Comparator<TypeOfWorkHours>() {
			public int compare(TypeOfWorkHours o1, TypeOfWorkHours o2) {
				return o1.getValidFrom().compareTo(o2.getValidFrom());
			}
		});
		

		LOG.info("cleanTypeOfHours; sorted list typeOfWorkHours=" + l);
		int listSize = l.size();
		for(int i = 0; i < listSize; i++){
			TypeOfWorkHours tmp = l.get(i);
			if((i+1) == listSize){ // posledny type of hours
				tmp.setName(getFirstName() + " " + getSurname() + " hour price");
			}
			else{ // ma nasledovnikov
				TypeOfWorkHours tmp1 = l.get(i+1);
				tmp.setName(getFirstName() + " " + getSurname() + " hour price (do "+new SimpleDateFormat("dd.MM.yyyy").format(tmp1.getValidFrom())+")" );
			}
		}

		LOG.info("cleanTypeOfHours; final list typeOfWorkHours=" + typeOfWorkHours);
	}
	
	public TypeOfWorkHours findTypOfHours(LocalDate date) {
		return findTypOfHours(date.toDate());
	}
	
	private TypeOfWorkHours findTypOfHours(Date date) {
		if(typeOfWorkHours != null){
			TypeOfWorkHours t = null; // t hladam co najblizsie k nejakemu datumu tak, kde t.datum < date 
			TypeOfWorkHours tMin = null;
			long minDiff = Long.MAX_VALUE;
			long maxDiff = Long.MIN_VALUE;
			
			for(TypeOfWorkHours towh : typeOfWorkHours){
				long diff = date.getTime() - towh.getValidFrom().getTime();
				if(0 <= diff && diff < minDiff){
					t = towh;
					minDiff = diff;
				}
				if(maxDiff < diff && diff < 0){
					maxDiff = diff;
					tMin = towh;
				}
			}
			if(t == null)
				return tMin;
			return t;
		}
		return null;

	}
   
}
