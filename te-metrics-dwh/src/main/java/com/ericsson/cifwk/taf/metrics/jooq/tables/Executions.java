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
public class Executions extends org.jooq.impl.TableImpl<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord> {

	private static final long serialVersionUID = 318989011;

	/**
	 * The singleton instance of <code>taf_performance.executions</code>
	 */
	public static final com.ericsson.cifwk.taf.metrics.jooq.tables.Executions EXECUTIONS = new com.ericsson.cifwk.taf.metrics.jooq.tables.Executions();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord> getRecordType() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord.class;
	}

	/**
	 * The column <code>taf_performance.executions.id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord, java.lang.Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.executions.name</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * Create a <code>taf_performance.executions</code> table reference
	 */
	public Executions() {
		this("executions", null);
	}

	/**
	 * Create an aliased <code>taf_performance.executions</code> table reference
	 */
	public Executions(java.lang.String alias) {
		this(alias, com.ericsson.cifwk.taf.metrics.jooq.tables.Executions.EXECUTIONS);
	}

	private Executions(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Executions(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.ericsson.cifwk.taf.metrics.jooq.TafPerformance.TAF_PERFORMANCE, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord> getPrimaryKey() {
		return com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_EXECUTIONS_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.ExecutionsRecord>>asList(com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_EXECUTIONS_PRIMARY, com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_EXECUTIONS_ID_UNIQUE, com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_EXECUTIONS_NAME_UNIQUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.ericsson.cifwk.taf.metrics.jooq.tables.Executions as(java.lang.String alias) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.Executions(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.ericsson.cifwk.taf.metrics.jooq.tables.Executions rename(java.lang.String name) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.Executions(name, null);
	}
}
