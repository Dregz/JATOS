package utils.common;

import java.io.IOException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import general.common.MessagesStrings;
import models.common.Study;
import models.common.old.StudyV2;

/**
 * Unmarshalling of an JSON string to a study. The study's JSON string can be in
 * different versions of the study to support older JATOS' versions.
 * 
 * For each unmarshalling a new instance of this unmarshaller has to be created.
 * 
 * @author Kristian Lange 2015
 */
public class StudyUploadUnmarshaller extends UploadUnmarshaller<Study> {

	private Study study;

	@Inject
	StudyUploadUnmarshaller(IOUtils ioUtils) {
		super(ioUtils);
	}

	/**
	 * Accepts an JSON String and turns the data object within this JSON String
	 * into an object of type Study. It can handle different versions of the
	 * study model. The version is determined by the version field in the JSON
	 * string. Each supported study version has its own model which is used for
	 * unmarshaling.
	 */
	@Override
	protected Study concreteUnmarshaling(String jsonStr) throws IOException {
		JsonNode node = JsonUtils.OBJECTMAPPER.readTree(jsonStr).findValue(
				JsonUtils.VERSION);
		int version = node.asInt();
		if (version > Study.SERIAL_VERSION) {
			throw new IOException(MessagesStrings.TOO_NEW_STUDY_VERSION);
		}
		switch (version) {
		case 0:
		case 2:
			node = JsonUtils.OBJECTMAPPER.readTree(jsonStr).findValue(
					JsonUtils.DATA);
			StudyV2 studyV2 = JsonUtils.OBJECTMAPPER.treeToValue(node,
					StudyV2.class);
			study = bindV2(studyV2);
			break;
		case 3:
			// Current version
			node = JsonUtils.OBJECTMAPPER.readTree(jsonStr).findValue(
					JsonUtils.DATA);
			study = JsonUtils.OBJECTMAPPER.treeToValue(node, Study.class);
			break;
		default:
			throw new IOException(MessagesStrings.UNSUPPORTED_STUDY_VERSION);
		}
		return study;
	}

	/**
	 * Binding of study version 2
	 */
	private Study bindV2(StudyV2 studyV2) {
		Study study = new Study();
		study.setUuid(studyV2.getUuid());
		study.setTitle(studyV2.getTitle());
		study.setDescription(studyV2.getDescription());
		study.setDate(studyV2.getDate());
		study.setLocked(studyV2.isLocked());
		studyV2.getAllowedWorkerList().stream()
				.forEach(study::addAllowedWorkerType);
		study.setDirName(studyV2.getDirName());
		study.setComments(studyV2.getComments());
		study.setJsonData(studyV2.getJsonData());
		study.setComponentList(studyV2.getComponentList());
		return study;
	}

}