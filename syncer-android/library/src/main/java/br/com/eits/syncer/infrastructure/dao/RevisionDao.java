package br.com.eits.syncer.infrastructure.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import br.com.eits.syncer.domain.entity.Revision;

/**
 * Created by rodrigo.p.fraga on 03/11/16.
 */
public class RevisionDao extends RuntimeExceptionDao<Revision, Long>
{
    /**
     *
     * @param dao
     */
    public RevisionDao(Dao dao)
    {
        super(dao);
    }
}
