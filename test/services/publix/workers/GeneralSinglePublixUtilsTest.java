package services.publix.workers;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.fest.assertions.Fail;
import org.junit.Test;

import exceptions.publix.ForbiddenPublixException;
import exceptions.publix.PublixException;
import models.common.Study;
import models.common.workers.GeneralSingleWorker;
import play.mvc.Http.Cookie;
import services.publix.PublixErrorMessages;
import services.publix.PublixUtilsTest;
import services.publix.workers.GeneralSingleErrorMessages;
import services.publix.workers.GeneralSinglePublixUtils;

/**
 * @author Kristian Lange
 */
public class GeneralSinglePublixUtilsTest
		extends PublixUtilsTest<GeneralSingleWorker> {

	private GeneralSingleErrorMessages generalSingleErrorMessages;
	private GeneralSinglePublixUtils generalSinglePublixUtils;

	@Override
	public void before() throws Exception {
		super.before();
		generalSinglePublixUtils = application.injector()
				.instanceOf(GeneralSinglePublixUtils.class);
		publixUtils = generalSinglePublixUtils;
		generalSingleErrorMessages = application.injector()
				.instanceOf(GeneralSingleErrorMessages.class);
		errorMessages = generalSingleErrorMessages;
	}

	@Override
	public void after() throws Exception {
		super.after();
	}

	@Test
	public void checkRetrieveTypedWorker()
			throws NoSuchAlgorithmException, IOException, PublixException {
		GeneralSingleWorker worker = new GeneralSingleWorker();
		persistWorker(worker);

		GeneralSingleWorker retrievedWorker = publixUtils
				.retrieveTypedWorker(worker.getId());
		assertThat(retrievedWorker.getId()).isEqualTo(worker.getId());
	}

	@Test
	public void checkRetrieveTypedWorkerWrongType()
			throws NoSuchAlgorithmException, IOException, PublixException {
		try {
			generalSinglePublixUtils
					.retrieveTypedWorker(admin.getWorker().getId());
			Fail.fail();
		} catch (ForbiddenPublixException e) {
			assertThat(e.getMessage()).isEqualTo(generalSingleErrorMessages
					.workerNotCorrectType(admin.getWorker().getId()));
		}
	}

	@Test
	public void checkStudyInCookie() throws NoSuchAlgorithmException,
			IOException, ForbiddenPublixException {
		Study study = importExampleStudy();
		addStudy(study);

		Cookie cookie = mock(Cookie.class);

		// Done studies but not this one
		when(cookie.value()).thenReturn("3:4:5");
		generalSinglePublixUtils.checkStudyInGeneralSingleCookie(study, cookie);

		// Empty cookie value is allowed
		when(cookie.value()).thenReturn("");
		generalSinglePublixUtils.checkStudyInGeneralSingleCookie(study, cookie);

		// Weired cookie value is allowed
		when(cookie.value()).thenReturn("foo");
		generalSinglePublixUtils.checkStudyInGeneralSingleCookie(study, cookie);

		// Clean-up
		removeStudy(study);
	}

	@Test
	public void checkStudyInCookieAlreadyDone() throws NoSuchAlgorithmException,
			IOException, ForbiddenPublixException {
		Study study = importExampleStudy();
		addStudy(study);

		Cookie cookie = mock(Cookie.class);
		// Put this study UUID into the cookie
		when(cookie.value()).thenReturn(study.getUuid());

		try {
			generalSinglePublixUtils.checkStudyInGeneralSingleCookie(study,
					cookie);
			Fail.fail();
		} catch (PublixException e) {
			assertThat(e.getMessage())
					.isEqualTo(PublixErrorMessages.STUDY_CAN_BE_DONE_ONLY_ONCE);
		}

		// Clean-up
		removeStudy(study);
	}

	@Test
	public void addStudyToCookie()
			throws NoSuchAlgorithmException, IOException {
		Study study = importExampleStudy();
		addStudy(study);

		Cookie cookie = mock(Cookie.class);

		// Cookie with two study IDs
		when(cookie.value()).thenReturn("10:20");
		String cookieValue = generalSinglePublixUtils
				.addStudyUuidToGeneralSingleCookie(study, cookie);
		assertThat(cookieValue).isEqualTo("10:20:" + study.getUuid());

		// No cookie
		cookieValue = generalSinglePublixUtils
				.addStudyUuidToGeneralSingleCookie(study, null);
		assertThat(cookieValue).isEqualTo(study.getUuid());

		// Clean-up
		removeStudy(study);
	}

}