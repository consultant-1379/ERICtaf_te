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
public class TestSuites extends org.jooq.impl.TableImpl<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord> {

	private static final long serialVersionUID = 701337044;

	/**
	 * The singleton instance of <code>taf_performance.test_suites</code>
	 */
	public static final com.ericsson.cifwk.taf.metrics.jooq.tables.TestSuites TEST_SUITES = new com.ericsson.cifwk.taf.metrics.jooq.tables.TestSuites();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord> getRecordType() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord.class;
	}

	/**
	 * The column <code>taf_performance.test_suites.id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord, java.lang.Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.test_suites.name</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>taf_performance.test_suites.execution_id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord, java.lang.Long> EXECUTION_ID = createField("execution_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * Create a <code>taf_performance.test_suites</code> table reference
	 */
	public TestSuites() {
		this("test_suites", null);
	}

	/**
	 * Create an aliased <code>taf_performance.test_suites</code> table reference
	 */
	public TestSuites(java.lang.String alias) {
		this(alias, com.ericsson.cifwk.taf.metrics.jooq.tables.TestSuites.TEST_SUITES);
	}

	private TestSuites(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord> aliased) {
		this(alias, aliased, null);
	}

	private TestSuites(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.ericsson.cifwk.taf.metrics.jooq.TafPerformance.TAF_PERFORMANCE, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord> getPrimaryKey() {
		return com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_TEST_SUITES_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord>>asList(com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_TEST_SUITES_PRIMARY, com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_TEST_SUITES_NAME_UNIQUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestSuitesRecord, ?>>asList(com.ericsson.cifwk.taf.metrics.jooq.Keys.FK_TEST_SUITES_EXECUTIONS1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.ericsson.cifwk.taf.metrics.jooq.tables.TestSuites as(java.lang.String alias) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.TestSuites(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.ericsson.cifwk.taf.metrics.jooq.tables.TestSuites rename(java.lang.String name) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.TestSuites(name, null);
	}
}
