package org.openmrs.module.operationtheater.scheduler;

import org.junit.Test;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.Timetable;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

/**
 * Tests the optaplanner configuration specified in scheduler/solverConfig.xml
 */
public class OptaplannerConfigTest {

	@Test
	public void shouldSetUpSolver() throws Exception {
		SolverFactory solverFactory = SolverFactory.createFromXmlResource("scheduler/solverConfig.xml");
		SolverConfig config = solverFactory.getSolverConfig();

		//model definition
		//		assertThat(config.getSolutionClass(), is(Timetable.class));
		assertEquals(Timetable.class, config.getSolutionClass());
		assertThat(config.getEntityClassList(), hasSize(1));
		//		assertThat(config.getPlanningEntityClassList().get(0), is(PlannedSurgery.class)); //Todo find out how to use assertThat with generics correctly
		assertEquals(PlannedSurgery.class, config.getEntityClassList().get(0));

		//score function definition
		ScoreDirectorFactoryConfig scoreConfig = config.getScoreDirectorFactoryConfig();
		assertThat(scoreConfig.getScoreDefinitionType(), is(ScoreDefinitionType.HARD_SOFT));
		assertThat(scoreConfig.getScoreDrlList(), hasSize(1));
		assertThat(scoreConfig.getScoreDrlList(), contains("scheduler/scoreRules.drl"));

		//optimization algorithm definition
		//TODO
	}
}
