package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.annotations.Parameter;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BuildParameterHolderFactoryTest {

    @Test
    public void shouldCreateHolderAndPopulateParentData() throws Exception {
        final Map<String, String> paramMap = Maps.newHashMap();

        paramMap.put(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID, "executionId");
        paramMap.put("subClassParam2", "123");

        TestBuildParameters result = BuildParameterHolderFactory.createHolder(TestBuildParameters.class, new Function<String, String>() {
            @Override
            public String apply(String paramName) {
                return paramMap.get(paramName);
            }
        });

        assertThat(result.getSubClassParam1(), is(nullValue()));
        assertThat(result.getExecutionId(), equalTo("executionId"));
        assertThat(result.getSubClassParam2(), equalTo(123));
    }

    @Test
    public void shouldGetAllParameters() throws Exception {
        TestBuildParameters buildParams = new TestBuildParameters();
        buildParams.setExecutionId("executionId");
        buildParams.setSubClassParam1("subClassParam1Value");

        Map<String, String> allParameters = BuildParameterHolderFactory.asMap(buildParams);
        assertEquals(2, allParameters.size());
        assertThat(allParameters, hasEntry(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID, "executionId"));
        assertThat(allParameters, hasEntry("subClassParam1", "subClassParam1Value"));
    }

    public static class TestBuildParameters extends CommonBuildParameters {

        @Parameter(name = "subClassParam1")
        private String subClassParam1;
        @Parameter(name = "subClassParam2")
        private Integer subClassParam2;

        public String getSubClassParam1() {
            return subClassParam1;
        }

        public void setSubClassParam1(String subClassParam1) {
            this.subClassParam1 = subClassParam1;
        }

        public Integer getSubClassParam2() {
            return subClassParam2;
        }

        public void setSubClassParam2(Integer subClassParam2) {
            this.subClassParam2 = subClassParam2;
        }

    }

}