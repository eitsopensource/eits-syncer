package br.com.eits.syncer.domain.entity;

import io.requery.android.database.sqlite.SQLiteDatabase;

/**
 * Created by eduardo on 05/04/18.
 */

public interface PostReceiveHook
{
	void afterInsert( SQLiteDatabase database, Revision<?> revision );
}
