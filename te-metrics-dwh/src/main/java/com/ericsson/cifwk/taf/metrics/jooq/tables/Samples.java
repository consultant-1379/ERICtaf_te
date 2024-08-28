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
public class Samples extends org.jooq.impl.TableImpl<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord> {

	private static final long serialVersionUID = 102670454;

	/**
	 * The singleton instance of <code>taf_performance.samples</code>
	 */
	public static final com.ericsson.cifwk.taf.metrics.jooq.tables.Samples SAMPLES = new com.ericsson.cifwk.taf.metrics.jooq.tables.Samples();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord> getRecordType() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord.class;
	}

	/**
	 * The column <code>taf_performance.samples.id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.thread_id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Long> THREAD_ID = createField("thread_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.vuser_id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.String> VUSER_ID = createField("vuser_id", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.protocol</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.String> PROTOCOL = createField("protocol", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.target</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.String> TARGET = createField("target", org.jooq.impl.SQLDataType.CLOB.length(65535).nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.request_type</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.String> REQUEST_TYPE = createField("request_type", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.request_size</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Long> REQUEST_SIZE = createField("request_size", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.response_code</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Integer> RESPONSE_CODE = createField("response_code", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.success</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Byte> SUCCESS = createField("success", org.jooq.impl.SQLDataType.TINYINT.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.response_time</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Integer> RESPONSE_TIME = createField("response_time", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.latency</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Integer> LATENCY = createField("latency", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.response_size</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Long> RESPONSE_SIZE = createField("response_size", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>taf_performance.samples.time_id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.sql.Timestamp> TIME_ID = createField("time_id", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>taf_performance.samples.test_case_id</code>.
	 */
	public final org.jooq.TableField<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Long> TEST_CASE_ID = createField("test_case_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * Create a <code>taf_performance.samples</code> table reference
	 */
	public Samples() {
		this("samples", null);
	}

	/**
	 * Create an aliased <code>taf_performance.samples</code> table reference
	 */
	public Samples(java.lang.String alias) {
		this(alias, com.ericsson.cifwk.taf.metrics.jooq.tables.Samples.SAMPLES);
	}

	private Samples(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord> aliased) {
		this(alias, aliased, null);
	}

	private Samples(java.lang.String alias, org.jooq.Table<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.ericsson.cifwk.taf.metrics.jooq.TafPerformance.TAF_PERFORMANCE, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, java.lang.Long> getIdentity() {
		return com.ericsson.cifwk.taf.metrics.jooq.Keys.IDENTITY_SAMPLES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord> getPrimaryKey() {
		return com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_SAMPLES_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord>>asList(com.ericsson.cifwk.taf.metrics.jooq.Keys.KEY_SAMPLES_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SamplesRecord, ?>>asList(com.ericsson.cifwk.taf.metrics.jooq.Keys.FK_SAMPLES_TIME, com.ericsson.cifwk.taf.metrics.jooq.Keys.FK_SAMPLES_TEST_CASES1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.ericsson.cifwk.taf.metrics.jooq.tables.Samples as(java.lang.String alias) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.Samples(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.ericsson.cifwk.taf.metrics.jooq.tables.Samples rename(java.lang.String name) {
		return new com.ericsson.cifwk.taf.metrics.jooq.tables.Samples(name, null);
	}
}
