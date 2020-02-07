package com.tdssrc.grails.gorm

import com.tdssrc.grails.CryptoUtils
import groovy.transform.CompileStatic
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.usertype.UserType

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

@CompileStatic
class GormEncryptedDateAsStringType implements UserType{
	@Override
	int[] sqlTypes() {
		[Types.DATE] as int[]
	}

	@Override
	Class returnedClass() {
		Date
	}

	@Override
	boolean equals(Object x, Object y) throws HibernateException {
		x == y
	}

	@Override
	int hashCode(Object x) throws HibernateException {
		x.hashCode()
	}

	@Override
	Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
		String value = rs.getString(names[0])
		(value == null) ? null : new Date(CryptoUtils.decrypt(value).toLong())
	}

	@Override
	void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
		if (value) {
			Date date = (Date) value
			st.setString( index, CryptoUtils.encrypt("${date.time}") )
		} else {
			st.setNull( index, Types.VARCHAR)
		}
	}

	@Override
	Object deepCopy(Object value) throws HibernateException {
		(value == null) ? null : new Date(((Date)value).time)
	}

	@Override
	boolean isMutable() {
		false
	}

	@Override
	Serializable disassemble(Object value) throws HibernateException {
		(value == null) ? null : new Date(((Date)value).time)
	}

	@Override
	Object assemble(Serializable cached, Object owner) throws HibernateException {
		(cached == null) ? null : new Date(((Date)cached).time)
	}

	@Override
	Object replace(Object original, Object target, Object owner) throws HibernateException {
		original
	}
}
