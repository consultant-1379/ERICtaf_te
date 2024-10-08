/**
 * This class is generated by jOOQ
 */
package com.ericsson.cifwk.taf.metrics.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TestCasesRecord extends org.jooq.impl.UpdatableRecordImpl<com.ericsson.cifwk.taf.metrics.jooq.tables.records.TestCasesRecord> implements org.jooq.Record3<java.lang.Long, java.lang.String, java.lang.Long> {

	private static final long serialVersionUID = 299578008;

	/**
	 * Setter for <code>taf_performance.test_cases.id</code>.
	 */
	public void setId(java.lang.Long value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>taf_performance.test_cases.id</code>.
	 */
	public java.lang.Long getId() {
		return (java.lang.Long) getValue(0);
	}

	/**
	 * Setter for <code>taf_performance.test_cases.name</code>.
	 */
	public void setName(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>taf_performance.test_cases.name</code>.
	 */
	public java.lang.String getName() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>taf_performance.test_cases.test_suite_id</code>.
	 */
	public void setTestSuiteId(java.lang.Long value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>taf_performance.test_cases.test_suite_id</code>.
	 */
	public java.lang.Long getTestSuiteId() {
		return (java.lang.Long) getValue(2);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Long> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record3 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Long, java.lang.String, java.lang.Long> fieldsRow() {
		return (org.jooq.Row3) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Long, java.lang.String, java.lang.Long> valuesRow() {
		return (org.jooq.Row3) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field1() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.TestCases.TEST_CASES.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.TestCases.TEST_CASES.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field3() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.TestCases.TEST_CASES.TEST_SUITE_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value3() {
		return getTestSuiteId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TestCasesRecord value1(java.lang.Long value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TestCasesRecord value2(java.lang.String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TestCasesRecord value3(java.lang.Long value) {
		setTestSuiteId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TestCasesRecord values(java.lang.Long value1, java.lang.String value2, java.lang.Long value3) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached TestCasesRecord
	 */
	public TestCasesRecord() {
		super(com.ericsson.cifwk.taf.metrics.jooq.tables.TestCases.TEST_CASES);
	}

	/**
	 * Create a detached, initialised TestCasesRecord
	 */
	public TestCasesRecord(java.lang.Long id, java.lang.String name, java.lang.Long testSuiteId) {
		super(com.ericsson.cifwk.taf.metrics.jooq.tables.TestCases.TEST_CASES);

		setValue(0, id);
		setValue(1, name);
		setValue(2, testSuiteId);
	}
}
