package de.projektlabor.makiekillerbot.cac.config.loading;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Loadable {
	public String name();
	public Class<? extends Loader<?>> loader();
}
