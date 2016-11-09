package br.com.eits.androidsyncer.infrastructure.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import br.com.eits.androidsyncer.domain.entity.Revision;

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
