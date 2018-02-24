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
	public List<T> list();

	/**
	 * @return
	 */
	public T singleResult();

	/**
	 * @param field
	 * @param value
	 * @return
	 */
	public IQueryRevisionService<T> where( String field, String value );

	/**
	 * @param field
	 * @param value
	 * @return
	 */
	public IQueryRevisionService<T> where( String field, Number value );

	/**
	 * @param field
	 * @param value
	 * @return
	 */
	public IQueryRevisionService<T> whereLike( String field, String value );

	/**
	 * @param joinEntity
	 * @return
	 */
	public IQueryRevisionService<T> join( Class<?> joinEntity, long joinEntityId );

	/**
	 * @return
	 */
	public IQueryRevisionService<T> filterBy( String filters );

	/**
	 * @return
	 */
	public IQueryRevisionService<T> begin();

	/**
	 * @return
	 */
	public IQueryRevisionService<T> end();

	/**
	 * @return
	 */
	public IQueryRevisionService<T> and();

	/**
	 * @return
	 */
	public IQueryRevisionService<T> or();
}
