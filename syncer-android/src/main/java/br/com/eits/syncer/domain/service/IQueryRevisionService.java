package br.com.eits.syncer.domain.service;

import java.util.List;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public interface IQueryRevisionService<T>
{
	/**
	 * @return
	 */
	List<T> list();

	/**
	 * @return
	 */
	T singleResult();

	/**
	 * @param field
	 * @param value
	 * @return
	 */
	IQueryRevisionService<T> where( String field, Object value );

	/**
	 *
	 */
	IQueryRevisionService<T> where( String field, Object value, String operator );

	IQueryRevisionService<T> where( String field );

	IQueryRevisionService<T> eq( Object value );

	IQueryRevisionService<T> gt( Object value );

	IQueryRevisionService<T> lt( Object value );

	IQueryRevisionService<T> goe( Object value );

	IQueryRevisionService<T> loe( Object value );

	IQueryRevisionService<T> castAs( String field, String castAs );

	/**
	 * @param field
	 * @param value
	 * @return
	 */
	IQueryRevisionService<T> where( String field, Number value );

	/**
	 * @param field
	 * @param value
	 * @return
	 */
	IQueryRevisionService<T> whereLike( String field, String value );

	/**
	 * @param joinEntity
	 * @return
	 */
	IQueryRevisionService<T> join( Class<?> joinEntity, long joinEntityId );

	/**
	 * @return
	 */
	IQueryRevisionService<T> filterBy( String filters );

	/**
	 * @return
	 */
	IQueryRevisionService<T> begin();

	/**
	 * @return
	 */
	IQueryRevisionService<T> end();

	/**
	 * @return
	 */
	IQueryRevisionService<T> and();

	/**
	 * @return
	 */
	IQueryRevisionService<T> or();
}
