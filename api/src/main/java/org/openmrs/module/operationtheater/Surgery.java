package org.openmrs.module.operationtheater;

import org.openmrs.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;


import org.openmrs.module.operationtheater.attribute.converter.time.LocalDateTimeAttributeConverter;

/**
 * Defines a Surgery in the system.
 */
@Entity
@Table(name = "surgery")
public class Surgery extends BaseOpenmrsDataJPA {

	@Id
	@GeneratedValue
	@Column(name = "surgery_id")
	private Integer surgeryId;

	@ManyToOne
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "procedure_id", nullable = false)
	private Procedure procedure;

	@Column(name = "date_started")
	@Convert( converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime dateStarted;

	@Column(name = "date_finished")
	@Convert( converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime dateFinished;

	@OneToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "scheduling_data_id")
	private SchedulingData schedulingData;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "surgical_team",
			joinColumns = { @JoinColumn(name = "surgery_id", nullable = false, updatable = false) },
			inverseJoinColumns = { @JoinColumn(name = "provider_id", nullable = false, updatable = false) })
	private Set<Provider> surgicalTeam;


	@OneToOne(cascade = { CascadeType.ALL })
	@JoinTable(name = "surgery_info_obs",
				joinColumns = { @JoinColumn(name="surgery_id", referencedColumnName = "surgery_id", nullable = true, updatable = true) },
				inverseJoinColumns = { @JoinColumn(name = "obs_group_id", referencedColumnName = "obs_id", nullable = true, updatable = true) })
	private Obs SurgeryObsGroup;

	public int getSurgeryId() {
		return surgeryId;
	}

	public void setSurgeryId(int surgeryId) {
		this.surgeryId = surgeryId;
	}

	@Override
	public Integer getId() {
		return getSurgeryId();
	}

	@Override
	public void setId(Integer integer) {
		setSurgeryId(integer);
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Procedure getProcedure() {
		return procedure;
	}

	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	public boolean isSurgeryCompleted() {
		return dateFinished != null;
	}

	public SchedulingData getSchedulingData() {
		return schedulingData;
	}

	public void setSchedulingData(SchedulingData schedulingData) {
		this.schedulingData = schedulingData;
	}

	public Set<Provider> getSurgicalTeam() {
		return surgicalTeam;
	}

	public void setSurgicalTeam(Set<Provider> surgicalTeam) {
		this.surgicalTeam = surgicalTeam;
	}

	public Obs getSurgeryObsGroup() {
		return SurgeryObsGroup;
	}

	public void setSurgeryObsGroup(Obs surgeryObsGroup) {
		SurgeryObsGroup = surgeryObsGroup;
	}

	public LocalDateTime getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(LocalDateTime dateFinished) {
		this.dateFinished = dateFinished;
	}

	public LocalDateTime getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(LocalDateTime dateStarted) {
		this.dateStarted = dateStarted;
	}




}
