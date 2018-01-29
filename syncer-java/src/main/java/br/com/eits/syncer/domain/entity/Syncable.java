package br.com.eits.syncer.domain.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by eduardo on 17/01/2018.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Syncable
{
	/**
	 * Scope query for the current entity. You can reference different entities depending on the server
	 * implementation.
	 */
	String value() default "";
}
