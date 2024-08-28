package com.ericsson.cifwk.taf.executor.cluster.cloud;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/06/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class GridCloudSlaveProvidersTest {

    @Mock
    private CloudSlaveProviderValidator notSetUpProviderValidator;

    @Mock
    private CloudSlaveProviderValidator setUpProviderValidator;

    @Spy
    private GridCloudSlaveProviders unit;

    @Before
    public void setUp() {
        GridCloudSlaveProviders.INSTANCE = unit;
        doReturn(false).when(notSetUpProviderValidator).isProviderSetUp();
        doReturn(true).when(setUpProviderValidator).isProviderSetUp();
    }

    @Test
    public void exist_happyPath() throws Exception {
        doReturn(singletonList(setUpProviderValidator)).when(unit).loadAllValidatorsViaSpi();
        assertThat(GridCloudSlaveProviders.providersExist()).isTrue();
    }
    @Test
    public void exist_notAllSetUp() throws Exception {
        doReturn(asList(setUpProviderValidator, notSetUpProviderValidator)).when(unit).loadAllValidatorsViaSpi();
        assertThat(GridCloudSlaveProviders.providersExist()).isTrue();
    }
    @Test
    public void exist_allNotSetUp() throws Exception {
        doReturn(singletonList(notSetUpProviderValidator)).when(unit).loadAllValidatorsViaSpi();
        assertThat(GridCloudSlaveProviders.providersExist()).isFalse();
    }

}