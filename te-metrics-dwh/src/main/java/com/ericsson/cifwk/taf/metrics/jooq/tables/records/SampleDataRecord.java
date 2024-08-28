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
public class SampleDataRecord extends org.jooq.impl.UpdatableRecordImpl<com.ericsson.cifwk.taf.metrics.jooq.tables.records.SampleDataRecord> implements org.jooq.Record6<java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long> {

	private static final long serialVersionUID = 1832212477;

	/**
	 * Setter for <code>taf_performance.sample_data.id</code>.
	 */
	public void setId(java.lang.Long value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>taf_performance.sample_data.id</code>.
	 */
	public java.lang.Long getId() {
		return (java.lang.Long) getValue(0);
	}

	/**
	 * Setter for <code>taf_performance.sample_data.request_body</code>.
	 */
	public void setRequestBody(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>taf_performance.sample_data.request_body</code>.
	 */
	public java.lang.String getRequestBody() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>taf_performance.sample_data.request_headers</code>.
	 */
	public void setRequestHeaders(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>taf_performance.sample_data.request_headers</code>.
	 */
	public java.lang.String getRequestHeaders() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>taf_performance.sample_data.response_body</code>.
	 */
	public void setResponseBody(java.lang.String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>taf_performance.sample_data.response_body</code>.
	 */
	public java.lang.String getResponseBody() {
		return (java.lang.String) getValue(3);
	}

	/**
	 * Setter for <code>taf_performance.sample_data.response_headers</code>.
	 */
	public void setResponseHeaders(java.lang.String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>taf_performance.sample_data.response_headers</code>.
	 */
	public java.lang.String getResponseHeaders() {
		return (java.lang.String) getValue(4);
	}

	/**
	 * Setter for <code>taf_performance.sample_data.sample_id</code>.
	 */
	public void setSampleId(java.lang.Long value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>taf_performance.sample_data.sample_id</code>.
	 */
	public java.lang.Long getSampleId() {
		return (java.lang.Long) getValue(5);
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
	// Record6 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long> fieldsRow() {
		return (org.jooq.Row6) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long> valuesRow() {
		return (org.jooq.Row6) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field1() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA.REQUEST_BODY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA.REQUEST_HEADERS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field4() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA.RESPONSE_BODY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field5() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA.RESPONSE_HEADERS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field6() {
		return com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA.SAMPLE_ID;
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
		return getRequestBody();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getRequestHeaders();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value4() {
		return getResponseBody();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value5() {
		return getResponseHeaders();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value6() {
		return getSampleId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleDataRecord value1(java.lang.Long value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleDataRecord value2(java.lang.String value) {
		setRequestBody(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleDataRecord value3(java.lang.String value) {
		setRequestHeaders(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleDataRecord value4(java.lang.String value) {
		setResponseBody(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleDataRecord value5(java.lang.String value) {
		setResponseHeaders(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleDataRecord value6(java.lang.Long value) {
		setSampleId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleDataRecord values(java.lang.Long value1, java.lang.String value2, java.lang.String value3, java.lang.String value4, java.lang.String value5, java.lang.Long value6) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached SampleDataRecord
	 */
	public SampleDataRecord() {
		super(com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA);
	}

	/**
	 * Create a detached, initialised SampleDataRecord
	 */
	public SampleDataRecord(java.lang.Long id, java.lang.String requestBody, java.lang.String requestHeaders, java.lang.String responseBody, java.lang.String responseHeaders, java.lang.Long sampleId) {
		super(com.ericsson.cifwk.taf.metrics.jooq.tables.SampleData.SAMPLE_DATA);

		setValue(0, id);
		setValue(1, requestBody);
		setValue(2, requestHeaders);
		setValue(3, responseBody);
		setValue(4, responseHeaders);
		setValue(5, sampleId);
	}
}
