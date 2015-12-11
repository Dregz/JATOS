package services.publix;

import javax.inject.Inject;
import javax.inject.Singleton;

import daos.common.GroupResultDao;
import daos.common.StudyResultDao;
import models.common.Group;
import models.common.GroupResult;
import models.common.GroupResult.GroupState;
import models.common.StudyResult;

/**
 * Handles groups, e.g. joining or leaving a GroupResult. Members of a
 * GroupResult are StudyResults, which represents a particular study run.
 * 
 * All GroupResult members exchange messages via WebSockets that are called
 * group channels in JATOS. The message dispatching system is implemented with
 * Akka.
 * 
 * @author Kristian Lange (2015)
 */
@Singleton
public class GroupMessagingService {

	private final StudyResultDao studyResultDao;
	private final GroupResultDao groupResultDao;

	@Inject
	GroupMessagingService(StudyResultDao studyResultDao,
			GroupResultDao groupResultDao) {
		this.studyResultDao = studyResultDao;
		this.groupResultDao = groupResultDao;
	}

	/**
	 * Checks whether this StudyResult has a GroupResult that is not in state
	 * FINISHED.
	 */
	public boolean hasUnfinishedGroupResult(StudyResult studyResult) {
		GroupResult groupResult = studyResult.getGroupResult();
		return groupResult != null
				&& groupResult.getGroupState() != GroupState.FINISHED;
	}

	/**
	 * Joins the first GroupResult where the max number of members is not
	 * reached yet and returns it. If such doesn't exist it creates a new one
	 * and persists it.
	 */
	public GroupResult join(StudyResult studyResult, Group group) {
		// If we already have a GroupResult just return it
		if (hasUnfinishedGroupResult(studyResult)) {
			return studyResult.getGroupResult();
		}

		// Look in the DB if we have an incomplete GroupResult. If not create
		// new one.
		GroupResult groupResult = groupResultDao.findFirstMaxNotReached(group);
		if (groupResult == null) {
			groupResult = new GroupResult(group);
			groupResultDao.create(groupResult);
		}

		// Add StudyResult to GroupResult and vice versa
		groupResult.addStudyResult(studyResult);
		studyResult.setGroupResult(groupResult);

		groupResultDao.update(groupResult);
		studyResultDao.update(studyResult);
		return groupResult;
	}

	public void leave(StudyResult studyResult) {
		GroupResult groupResult = studyResult.getGroupResult();
		if (groupResult == null) {
			return;
		}

		moveStudyResultToHistory(studyResult, groupResult);

		// TODO If GroupResult has no more members empty remove it from DB
		// if (groupResult.getStudyResultList().isEmpty()) {
		// groupResultDao.remove(groupResult);
		// }

	}

	private void moveStudyResultToHistory(StudyResult studyResult,
			GroupResult groupResult) {
		groupResult.removeStudyResult(studyResult);
		groupResult.addStudyResultToHistory(studyResult);
		groupResultDao.update(groupResult);
	}

}