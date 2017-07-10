package org.openmrs.module.operationtheater.scheduler.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
//import org.joda.time.DateTime;
//import org.joda.time.Interval;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.solver.MovablePlannedSurgerySelectionFilter;
import org.openmrs.module.operationtheater.scheduler.solver.PlannedSurgeryDifficultyComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.threeten.extra.Interval;

/**
 * This class is the Planning entity of this optimization problem
 * This means it contains attributes which will be changed by the solver during the solution finding process
 * This attributes are called Planning Variables - see the @PlanningVariable annotation on the corresponding setters
 * possible values of a planning variable is defined by the value range provider
 */
@PlanningEntity(movableEntitySelectionFilter = MovablePlannedSurgerySelectionFilter.class,
		difficultyComparatorClass = PlannedSurgeryDifficultyComparator.class)
public class PlannedSurgery implements TimetableEntry {

	private OperationTheaterService otService; //= Context.getService(OperationTheaterService.class);

	//
	private Surgery surgery;

	// Planning variables: will change during planning (between score calculations)
	private TimetableEntry previousTimetableEntry;

	//shadow variable - automatically calculated if the underlying genuine planning variable (start) changes its value
	//not changed by the solver
	private ZonedDateTime start;

	private ZonedDateTime end;

	private Location location;

	private TimetableEntry nextTimetableEntry;

	public PlannedSurgery(OperationTheaterService otService) {
		this.otService = otService;
	}

	public PlannedSurgery() {
	}

	/**
	 * @param other
	 * @return true if intervals overlap
	 * @should return false if any of the date object are null
	 * @should return if the two intervals overlap
	 */
	public boolean isOverlapping(PlannedSurgery other) {
		if (start == null || end == null || other.start == null || other.end == null) {
			return false;
		}

		//if Interval is constructed with a null parameter it assumes a current timestamp
		return Interval.of(this.start.toInstant(), this.end.toInstant())
				.overlaps(Interval.of(other.start.toInstant(), other.end.toInstant()));
	}

	/**
	 * convenient method used by drool file and helps to keep this file readable
	 *
	 * @return
	 * @should return true if location start or end variables are null
	 * @should return if current scheduling is outside available times
	 */
	public boolean isOutsideAvailableTimes() {
		if (start == null || end == null || location == null) {
			return true;
		}

		Interval available = otService.getLocationAvailableTime(location, start.toLocalDate());
		Interval scheduled = Interval.of(start.toInstant(), end.toInstant());
		Interval overlap = null;
		if (available.isConnected(scheduled)){
			overlap = scheduled.intersection(available);
			return !scheduled.equals(overlap);
		}

		return true;
	}

	public Surgery getSurgery() {

		return surgery;
	}

	public void setSurgery(Surgery surgery) {

		this.surgery = surgery;
	}

	@PlanningVariable(graphType = PlanningVariableGraphType.CHAINED, valueRangeProviderRefs = { "anchorRange", "plannedSurgeryRange" })
	public TimetableEntry getPreviousTimetableEntry() {

		return previousTimetableEntry;
	}

	/**
	 * @param previousTimetableEntry
	 * @should set all shadow variables to null if previousTimetableEntry is null
	 * @should update all shadow variables
	 * @should set values of shadow variables correctly if surgery has been started
	 */
	public void setPreviousTimetableEntry(TimetableEntry previousTimetableEntry) {
		this.previousTimetableEntry = previousTimetableEntry;
		if (previousTimetableEntry == null) {
			location = null;
			start = null;
			end = null;
			return;
		}
		setStart(previousTimetableEntry.getEnd());
		//if surgery has already been started correct surgery end time, because otPreparation has already been performed
		if (surgery.getDateStarted() != null) {
			int interventionDuration = surgery.getProcedure().getInterventionDuration();
			end = ZonedDateTime.of(surgery.getDateStarted(), ZoneId.systemDefault()).plusMinutes(interventionDuration);
		}
		location = previousTimetableEntry.getLocation();
	}

	@Override
	public TimetableEntry getNextTimetableEntry() {
		return nextTimetableEntry;
	}

	@Override
	public void setNextTimetableEntry(TimetableEntry nextTimeTableEntry) {
		this.nextTimetableEntry = nextTimeTableEntry;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public ZonedDateTime getStart() {
		return start;
	}

	/**
	 * @param start
	 * @should set start and update end accordingly
	 */
	public void setStart(ZonedDateTime start) {
		setStart(start, true);
	}

	/**
	 * convenient method used for junit testing
	 *
	 * @param start
	 * @param calculateEndTime
	 */
	public void setStart(ZonedDateTime start, boolean calculateEndTime) {
		this.start = start;
		if (calculateEndTime) {
			if (start == null) {
				end = null;
			} else {
				int interventionDuration = surgery.getProcedure().getInterventionDuration();
				int otPreparationDuration = surgery.getProcedure().getOtPreparationDuration();
				ZonedDateTime endDate = start.plusMinutes(interventionDuration + otPreparationDuration);
				setEnd(endDate);
			}
		}
	}

	public ZonedDateTime getEnd() {
		return end;
	}

	public void setEnd(ZonedDateTime end) {
		this.end = end;
	}

	/**
	 * @return
	 * @should return the entire duration this surgery occupies the ot when nextTimeTableEntry is null
	 * @should return value from its successor in the chain added to the entire duration this surgery occupies the ot
	 */
	@Override
	public int getChainLengthInMinutes() {
		Procedure procedure = surgery.getProcedure();
		int duration = procedure.getInterventionDuration() + procedure.getOtPreparationDuration();
		return nextTimetableEntry == null ? duration : nextTimetableEntry.getChainLengthInMinutes() + duration;
	}

	@Override
	public String toString() {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
		String startStr = start == null ? "null      " : fmt.format(start);
		String endStr = end == null ? "null      " : fmt.format(end);
		return "\n         PS {" +
				"surgery=" + surgery.getUuid() +
				"} -> " + previousTimetableEntry;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof PlannedSurgery) {
			PlannedSurgery other = (PlannedSurgery) o;
			return new EqualsBuilder()
					.append(surgery, other.surgery)
					.append(previousTimetableEntry, other.previousTimetableEntry)
					.append(nextTimetableEntry, other.nextTimetableEntry)
					.append(start, other.start)
					.append(end, other.end)
					.append(location, other.location)
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(surgery)
				.append(previousTimetableEntry)
				.append(nextTimetableEntry)
				.append(start)
				.append(end)
				.append(location)
				.toHashCode();
	}

	/**
	 * updates the begin and finish times of the surgery object
	 * and stores it in the db
	 *
	 * @param service
	 * @should update schedulingData object and persist it into the db if location is not null
	 * @should set set start end and location fields to null if location is null
	 */
	public void persist(OperationTheaterService service) {
		SchedulingData scheduling = surgery.getSchedulingData();
		if (scheduling == null) {
			scheduling = new SchedulingData();
			surgery.setSchedulingData(scheduling);
		}
		if (location != null) {
			scheduling.setStart(start.toLocalDateTime());
			scheduling.setEnd(end.toLocalDateTime());
			scheduling.setLocation(location);

		} else {
			scheduling.setStart(null);
			scheduling.setEnd(null);
			scheduling.setLocation(null);
		}
		service.saveSurgery(surgery);
	}
}
