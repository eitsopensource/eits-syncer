package br.com.eits.syncer.infrastructure.dao.types;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDateType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class CalendarDataType extends BaseDateType
{
	/**
	 *
	 */
	private static final CalendarDataType singleton = new CalendarDataType();

	/**
	 *
	 * @return
	 */
	public static CalendarDataType getSingleton()
	{
		return singleton;
	}

	/**
	 *
	 */
	private CalendarDataType()
	{
		super( SqlType.DATE, new Class<?>[]{Calendar.class} );
	}

	/**
	 * Here for others to subclass.
	 *
	 * @param sqlType
	 * @param classes
	 */
	protected CalendarDataType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		final DateStringFormatConfig dateFormatConfig = super.convertDateStringConfig(fieldType, super.defaultDateFormatConfig);
		try
		{
			return new Timestamp(parseDateString(dateFormatConfig, defaultStr).getTime());
		}
		catch (ParseException e)
		{
			throw SqlExceptionUtil.create("Problems parsing default date string '" + defaultStr + "' using '"+ dateFormatConfig + '\'', e);
		}
	}

	/**
	 *
	 * @param fieldType
	 * @param results
	 * @param columnPos
	 * @return
	 * @throws SQLException
     */
	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException
	{
		return results.getTimestamp(columnPos);
	}

	/**
	 *
	 * @param fieldType
	 * @param sqlArg
	 * @param columnPos
     * @return
     */
	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
	{
		final Timestamp value = (Timestamp) sqlArg;
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime( new Date(value.getTime()) );
		return calendar;
	}

	/**
	 *
	 * @param fieldType
	 * @param javaObject
     * @return
     */
	@Override
	public Object javaToSqlArg( FieldType fieldType, Object javaObject )
	{
		Calendar date = (Calendar) javaObject;
		return new Timestamp(date.getTime().getTime());
	}

	@Override
	public boolean isArgumentHolderRequired()
	{
		return true;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String[] getAssociatedClassNames()
	{
		return new String[]{"java.util.Calendar"};
	}

	/**
	 *
	 * @param field
	 * @return
	 */
	@Override
	public boolean isValidForField(Field field)
	{
		return Calendar.class.isAssignableFrom(field.getType());
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Class<?> getPrimaryClass()
	{
		return Calendar.class;
	}
}