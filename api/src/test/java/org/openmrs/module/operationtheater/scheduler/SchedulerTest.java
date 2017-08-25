package org.openmrs.module.operationtheater.scheduler;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.DaemonTokenUtil;
import org.openmrs.module.operationtheater.DbUtil;
import org.openmrs.module.operationtheater.DbUtilDefaultInserts;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.Time;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.domain.Anchor;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.Timetable;
import org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.EnvironmentMode;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.openmrs.module.operationtheater.DbUtil.Config;
import static org.openmrs.module.operationtheater.DbUtil.insertInto;

/**
 * Automatic scheduler integration test
 */
public class SchedulerTest extends BaseModuleContextSensitiveTest {

	private static DbSetupTracker dbSetupTracker = new DbSetupTracker();

	private ZonedDateTime refDate = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);

	private ZonedDateTime now;

	@Before
	public void setUp() {
		DaemonTokenUtil.passDaemonTokenToModule();

		Time timeMock = Mockito.mock(Time.class);
		now = refDate.withHour(11);
		when(timeMock.now()).thenReturn(now);
		Whitebox.setInternalState(Scheduler.INSTANCE, "time", timeMock);
	}

	@After
	public void tearDown() {
		//don't influence other tests
		Scheduler.INSTANCE.reset();
	}

	public void setUpDB_2Normal1LockedAnd1OutsidePlanningPeriodSurgery() throws Exception {
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(Config.PROCEDURE)
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay")
						.values("Appendectomy", 35, 25, 4)
						.build(),
				insertInto(Config.SCHEDULING_DATA)
						.columns("start", "end", "location_id", "date_locked")
						.values(
								Date.from(refDate.plusHours(36).toInstant()),
								Date.from(refDate.plusHours(36).plusMinutes(35 + 25).toInstant()),
								100,
								false)
						.values(
								Date.from(refDate.plusHours(36).toInstant()),
								Date.from(refDate.plusHours(36).plusMinutes(35 + 25).toInstant()),
								100,
								false)
						.values(
								Date.from(refDate.plusHours(60).toInstant()),
								Date.from(refDate.plusHours(60).plusMinutes(35 + 25).toInstant()),
								100,
								true)
						.values(
								Date.from(refDate.plusDays(200).toInstant()),
								Date.from(refDate.plusDays(200).plusHours(1).toInstant()),
								100,
								true)
						.build(),
				insertInto(Config.SURGERY, "date_created")
						.columns("patient_id", "procedure_id", "scheduling_data_id", "date_created")
						.values(100, 1, 1, Date.from(refDate.minusWeeks(1).toInstant()))
						.values(100, 1, 2, Date.from(refDate.minusWeeks(2).toInstant()))
						.values(100, 1, 3, Date.from(refDate.minusWeeks(3).toInstant())) //locked
						.values(100, 1, 4, Date.from(refDate.minusWeeks(3).toInstant())) //outside planning period
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetupTracker.launchIfNecessary(dbSetup);
	}

	public void setUpDB_2ConflictingSurgeries() throws Exception {
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(Config.PROCEDURE)
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay")
						.values("Appendectomy", 35, 25, 4)
						.build(),
				insertInto(Config.SCHEDULING_DATA)
						.columns("start", "end", "location_id", "date_locked")
						.values(
								Date.from(refDate.plusHours(12).toInstant()),
								Date.from(refDate.plusHours(13).plusMinutes(35 + 25).toInstant()),
								100,
								false)
						.values(
								Date.from(refDate.plusHours(13).toInstant()),
								Date.from(refDate.plusHours(14).plusMinutes(35 + 25).toInstant()),
								100,
								false)
						.build(),
				insertInto(Config.SURGERY, "date_created")
						.columns("patient_id", "procedure_id", "scheduling_data_id", "date_created")
						.values(100, 1, 1, Date.from(refDate.minusWeeks(1).toInstant()))
						.values(100, 1, 2, Date.from(refDate.minusWeeks(2).toInstant()))
						.build(),
				Operations.insertInto("surgical_team")
						.columns("surgery_id", "provider_id")
						.values(1, 1)
						.values(2, 1)
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetupTracker.launchIfNecessary(dbSetup);
	}

	public void setUpDb_OneSurgeryStarted() throws Exception {
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(Config.PROCEDURE)
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay")
						.values("Appendectomy", 35, 25, 4)
						.build(),
				insertInto(Config.SCHEDULING_DATA)
						.columns("start", "end", "location_id", "date_locked")
						.values(
								Date.from(now.toInstant()), Date.from(now.plusMinutes(35 + 25).toInstant()),
								100,
								false)
						.values(
								Date.from(refDate.plusHours(13).toInstant()),
								Date.from(refDate.plusHours(14).plusMinutes(35 + 25).toInstant()),
								100,
								false)
						.build(),
				insertInto(Config.SURGERY, "date_created")
						.columns("patient_id", "procedure_id", "scheduling_data_id", "date_created", "date_started")
								//surgery started with 15min delay
						.values(100, 1, 1,
								Date.from(refDate.minusWeeks(1).toInstant()),
								Date.from(now.minusMinutes(15).toInstant()))
						.values(100, 1, 2,
								Date.from(refDate.minusWeeks(2).toInstant()), null)
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetupTracker.launchIfNecessary(dbSetup);
	}

	/**
	 * @verifies solve 2 normal 1 locked and 1 outside planning period surgery
	 * @see Scheduler#solve(int)
	 */
	@Test
	public void solve_shouldSolve2Normal1LockedAnd1OutsidePlanningPeriodSurgery() throws Exception {
		setUpDB_2Normal1LockedAnd1OutsidePlanningPeriodSurgery();

		//modify solverFactory to come up with reproducible results
		SolverFactory solverFactory = Scheduler.INSTANCE.getSolverFactory();
		solverFactory.getSolverConfig().setEnvironmentMode(EnvironmentMode.FAST_ASSERT);
		Scheduler.INSTANCE.setSolverFactory(solverFactory);

		//call method under test
		Scheduler.INSTANCE.solve(14);

		//wait until a solution has been found
		long max_waiting_time = 180000;//3min
		long start = System.currentTimeMillis();
		while (Scheduler.INSTANCE.getStatus() == Scheduler.Status.RUNNING
				&& System.currentTimeMillis() < start + max_waiting_time) {
			Thread.currentThread().sleep(2000); //2s
		}

		//verify
		OperationTheaterService otService = Context.getService(OperationTheaterService.class);

		assertThat(Scheduler.INSTANCE.getStatus(), is(Scheduler.Status.SUCCEEDED));
		List<Surgery> surgeries = otService.getAllSurgeries(false);
		int i = 0;
		assertThat(surgeries.get(i).getUuid(), is("surgery1"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.withHour(11).toLocalDateTime()));
		i++;
		assertThat(surgeries.get(i).getUuid(), is("surgery2"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.withHour(11).toLocalDateTime()));
		assertThat(surgeries.get(i).getSchedulingData().getLocation(),
				is(not(surgeries.get(i - 1).getSchedulingData().getLocation())));
		i++;
		assertThat(surgeries.get(i).getUuid(), is("surgery3"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.plusHours(60).toLocalDateTime()));
		assertThat(surgeries.get(i).getSchedulingData().getLocation().getId(), is(100));
	}

	/**
	 * @verifies solve 2 conflicting surgeries
	 * @see Scheduler#solve(int)
	 */
	@Test
	public void solve_shouldSolve2ConflictingSurgeries() throws Exception {
		setUpDB_2ConflictingSurgeries();

		//modify solverFactory to come up with reproducible results
		SolverFactory solverFactory = Scheduler.INSTANCE.getSolverFactory();
		solverFactory.getSolverConfig().setEnvironmentMode(EnvironmentMode.FAST_ASSERT);
		Scheduler.INSTANCE.setSolverFactory(solverFactory);

		//call method under test
		Scheduler.INSTANCE.solve(14);

		//wait until a solution has been found
		long max_waiting_time = 180000;//3min
		long start = System.currentTimeMillis();
		while (Scheduler.INSTANCE.getStatus() == Scheduler.Status.RUNNING
				&& System.currentTimeMillis() < start + max_waiting_time) {
			Thread.currentThread().sleep(2000); //2s
		}

		//verify
		OperationTheaterService otService = Context.getService(OperationTheaterService.class);

		assertThat(Scheduler.INSTANCE.getStatus(), is(Scheduler.Status.SUCCEEDED));
		List<Surgery> surgeries = otService.getAllSurgeries(false);
		int i = 0;
		assertThat(surgeries.get(i).getUuid(), is("surgery1"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.withHour(11).toLocalDateTime()));
		assertThat(surgeries.get(i).getSchedulingData().getLocation().getId(), is(100));
		i++;
		assertThat(surgeries.get(i).getUuid(), is("surgery2"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.withHour(12).toLocalDateTime()));
		assertThat(surgeries.get(i).getSchedulingData().getLocation().getId(), is(100));
	}

	/**
	 * @verifies throw IllegalStateException if solve has already been started but not finished
	 * @see Scheduler#solve(int)
	 */
	@Test(expected = IllegalStateException.class)
	public void solve_shouldThrowIllegalStateExceptionIfSolveHasAlreadyBeenStartedButNotFinished() throws Exception {
		Whitebox.setInternalState(Scheduler.INSTANCE, "status", Scheduler.Status.RUNNING);

		//call method under test
		Scheduler.INSTANCE.solve(7);
	}

	/**
	 * @verifies properly setup the initial solution
	 * @see Scheduler#setupInitialSolution(int)
	 */
	@Test
	public void setupInitialSolution_shouldProperlySetupTheInitialSolution() throws Exception {
		setUpDB_2Normal1LockedAnd1OutsidePlanningPeriodSurgery();

		OperationTheaterService otService = Context.getService(OperationTheaterService.class);
		LocationService locationService = Context.getLocationService();
		Whitebox.setInternalState(Scheduler.INSTANCE, "otService", otService);
		Whitebox.setInternalState(Scheduler.INSTANCE, "locationService", locationService);

		//call method under test
		Timetable timetable = Scheduler.INSTANCE.setupInitialSolution(3);

		//verify
		//anchors
		List<Anchor> anchors = timetable.getAnchors();
		assertThat(anchors, hasSize(8));
		int i = 0;
		assertThat(anchors.get(i).getLocation(), is(nullValue()));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 1"));
		assertThat(anchors.get(i).getStart(), equalTo(now));
		assertThat(anchors.get(i).getRemainingTime(), is(360));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 1"));
		assertThat(anchors.get(i).getStart(), equalTo(refDate.plusDays(1).plusHours(8)));
		assertThat(anchors.get(i).getRemainingTime(), is(540));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 1"));
		assertThat(anchors.get(i).getStart(), equalTo(refDate.plusDays(2).plusHours(8)));
		assertThat(anchors.get(i).getRemainingTime(), is(240));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 1"));
		assertThat(anchors.get(i).getStart(), equalTo(refDate.plusDays(2).plusHours(12)));
		assertThat(anchors.get(i).getRemainingTime(), is(300));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 2"));
		assertThat(anchors.get(i).getStart(), equalTo(now));
		assertThat(anchors.get(i).getRemainingTime(), is(360));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 2"));
		assertThat(anchors.get(i).getStart(), equalTo(refDate.plusDays(1).plusHours(8)));
		assertThat(anchors.get(i).getRemainingTime(), is(540));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 2"));
		assertThat(anchors.get(i).getStart(), equalTo(refDate.plusDays(2).plusHours(8)));
		assertThat(anchors.get(i).getRemainingTime(), is(540));

		//planned surgeries
		List<PlannedSurgery> plannedSurgeries = timetable.getPlannedSurgeries();
		assertThat(plannedSurgeries, hasSize(3));
		i = 0;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery1"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(),
				is(refDate.plusDays(1).plusHours(8))); //start time is not the same as in db - determined by anchor
		assertThat(plannedSurgeries.get(i).getEnd(), is(refDate.plusDays(1).plusHours(9)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) anchors.get(2)));
		i++;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery2"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(),
				is(refDate.plusDays(1).plusHours(9))); //start time is not the same as in db - determined by previous element
		assertThat(plannedSurgeries.get(i).getEnd(), is(refDate.plusDays(1).plusHours(10)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) plannedSurgeries.get(0)));
		i++;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery3"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(), is(refDate.plusHours(60)));
		assertThat(plannedSurgeries.get(i).getEnd(), is(refDate.plusHours(61)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) anchors.get(4)));
	}

	/**
	 * @verifies setup the initial solution if a surgery is currently performed
	 * @see Scheduler#setupInitialSolution(int)
	 */
	@Test
	public void setupInitialSolution_shouldSetupTheInitialSolutionIfASurgeryIsCurrentlyPerformed() throws Exception {
		//prepare
		setUpDb_OneSurgeryStarted();

		OperationTheaterService otService = Context.getService(OperationTheaterService.class);
		LocationService locationService = Context.getLocationService();
		Whitebox.setInternalState(Scheduler.INSTANCE, "otService", otService);
		Whitebox.setInternalState(Scheduler.INSTANCE, "locationService", locationService);

		//call method under test
		Timetable timetable = Scheduler.INSTANCE.setupInitialSolution(1);

		//verify
		//anchors
		List<Anchor> anchors = timetable.getAnchors();
		assertThat(anchors, hasSize(3));
		int i = 0;
		assertThat(anchors.get(i).getLocation(), is(nullValue()));
		i++;
		assertThat(anchors.get(i).getStart(), is(now.minusMinutes(15)));
		assertThat(anchors.get(i).getLocation().getId(), is(100));
		assertThat(anchors.get(i).getRemainingTime(), is(375));
		i++;
		assertThat(anchors.get(i).getStart(), is(now));
		assertThat(anchors.get(i).getLocation().getId(), is(101));
		assertThat(anchors.get(i).getRemainingTime(), is(360));

		//planned surgeries
		List<PlannedSurgery> plannedSurgeries = timetable.getPlannedSurgeries();
		assertThat(plannedSurgeries, hasSize(2));
		i = 0;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery1"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(),
				is(now.minusMinutes(15))); //start time is not the same as in db - determined by anchor
		assertThat(plannedSurgeries.get(i).getEnd(), is(now.minusMinutes(15).plusMinutes(35)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) anchors.get(1)));
		i++;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery2"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(),
				is(now.plusMinutes(20))); //start time is not the same as in db - determined by anchor
		assertThat(plannedSurgeries.get(i).getEnd(), is(now.plusMinutes(20).plusHours(1)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) plannedSurgeries.get(0)));
	}
}
