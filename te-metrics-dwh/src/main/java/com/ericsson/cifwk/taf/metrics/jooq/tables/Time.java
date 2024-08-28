/**
 * This class is generated by jOOQ
 */
package com.ericsson.cifwk.taf.metrics.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Time extends org.jooq.impl.TableImpl<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord> {

	private static final long serialVersionUID = -36641991;

	/**
	 * The singleton instance of <code>taf_performance.time</code>
	 */
	public static final com.ericsson.cifwk.taf.metrics.jooq.tables.Time TIME = new com.ericsson.cifwk.taf.metrics.jooq.tables.Time();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord> getRecordType() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord.class;
	}

	/**
	 * The column <code>taf_performance.time.id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord, java.sql.Timestamp> ID = createField("id", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>taf_performance.time.day</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord, java.sql.Date> DAY = createField("day", org.jooq.impl.SQLDataType.DATE.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.time.hour</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord, java.lang.Integer> HOUR = createField("hour", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.time.minute</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord, java.lang.Integer> MINUTE = createField("minute", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>taf_performance.time</code> table reference
	 */
	public Time() {
		this("time", null);
	}

	/**
	 * Create an aliased <code>taf_performance.time</code> table reference
	 */
	public Time(java.lang.String alias) {
		this(alias, com.ericsson.cifwk.taf.metrics.jooq.tables.Time.TIME);
	}

	private Time(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord> aliased) {
		this(alias, aliased, null);
	}

	private Time(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.ericsson.cifwk.taf.metrics.jooq.TafPerformance.TAF_PERFORMANCE, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord> getPrimaryKey() {
		return com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_TIME_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TimeRecord>>asList(com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_TIME_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.ericsson.cifwk.taf.metrics.jooq.tables.Time as(java.lang.String alias) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.Time(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.ericsson.cifwk.taf.metrics.jooq.tables.Time rename(java.lang.String name) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.Time(name, null);
	}
}
