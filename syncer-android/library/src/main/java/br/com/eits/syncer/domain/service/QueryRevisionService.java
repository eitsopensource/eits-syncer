package br.com.eits.syncer.domain.service;

import java.util.ArrayList;
import java.util.List;

import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.infrastructure.dao.SQLiteHelper;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public class QueryRevisionService <T> extends RevisionService<T> implements IQueryRevisionService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/

    private String tables = "";
    private String joinTable = "";
    private String where = "";

    private List<Object> whereArguments = new ArrayList<Object>();

    private String groupBy;
    private String having;
    private String orderBy;

    /**
     * @param entityClass
     */
    public QueryRevisionService( Class<T> entityClass )
    {
        super(entityClass);

        this.tables = SQLiteHelper.TABLE_REVISION;
        this.joinTable = "json_each(" + SQLiteHelper.COLUMN_ENTITY + ")";
        this.where = this.where.concat( SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND " );
        this.whereArguments.add( entityClass.getName() );
    }

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     * @param field
     * @param value
     * @return
     */
    @Override
    public IQueryRevisionService where( String field, String value )
    {
        this.where = this.where.concat( field + " = ?" );
        this.whereArguments.add( value );
        return this;
    }

    /**
     * @param joinEntity
     * @return
     */
    @Override
    public IQueryRevisionService join( Class<?> joinEntity, String joinEntityId )
    {
        final String simpleClassName = joinEntity.getSimpleName().substring(0, 1).toLowerCase() + joinEntity.getSimpleName().substring(1);

        this.tables = this.tables.concat( ", " + this.joinTable );

        //TODO: Fazer pegar o nome do ID e usar em json_extract(entity, '$.simpleClassName.entityIdName')

        //"json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%" + filters + "%'";
        //SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal();

//        String selectToGetIdName = "(SELECT "+SQLiteHelper.TABLE_REVISION+"."+SQLiteHelper.COLUMN_ENTITY_ID_NAME+" FROM "+ SQLiteHelper.TABLE_REVISION+" WHERE "+SQLiteHelper.COLUMN_ENTITY_CLASSNAME+" = "+joinEntity.getName()+" LIMIT 1";

        this.where = this.where.concat( "json_extract("+ SQLiteHelper.COLUMN_ENTITY + ", '$." + simpleClassName + "." + SQLiteHelper.COLUMN_ENTITY_ID_NAME + "') = ?" );
        this.whereArguments.add( joinEntity );

        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public IQueryRevisionService and() {
        this.where = this.where.concat(" AND ");
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public IQueryRevisionService or() {
        this.where = this.where.concat(" OR ");
        return this;
    }

    /**
     * @return
     */
    @Override
    public List<T> list()
    {
        final List<Revision<T>> revisions = this.revisionDao.listByCustomQuery( this );

        final List<T> entities = new ArrayList<T>();
        for ( Revision<T> revision : revisions )
        {
            entities.add( revision.getEntity() );
        }

        return entities;
    }

    /*-------------------------------------------------------------------
    * 		 				    GETTERS
    *-------------------------------------------------------------------*/

    /**
     *
     * @return
     */
    public String getGroupBy() {
        return groupBy;
    }

    /**
     *
     * @return
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     *
     * @return
     */
    public String getTables() {
        return tables;
    }

    /**
     *
     * @return
     */
    public String getHaving() {
        return having;
    }

    /**
     *
     * @return
     */
    public List<Object> getWhereArguments() {
        return whereArguments;
    }

    /**
     *
     * @return
     */
    public String getWhere() {
        return where;
    }
}
