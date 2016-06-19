package controllers.publix.actionannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import controllers.publix.actionannotation.PublixLoggingAction.PublixLogging;
import play.Logger;
import play.Logger.ALogger;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.With;

/**
 * Annotation definition for Play actions: logging of each action call, e.g.
 * 'publix_access - GET /publix/19/64/start'
 * 
 * @author Kristian Lange (2016)
 */
public class PublixLoggingAction extends Action<PublixLogging> {

	@With(PublixLoggingAction.class)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PublixLogging {
	}

	private ALogger publixLogger = Logger.of("publix_access");

	public F.Promise<Result> call(Http.Context ctx) throws Throwable {
		final Request request = ctx.request();
		publixLogger.info(request.method() + " " + request.uri());
		return delegate.call(ctx);
	}

}
