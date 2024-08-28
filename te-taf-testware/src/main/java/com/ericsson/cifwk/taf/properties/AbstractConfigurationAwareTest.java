package com.ericsson.cifwk.taf.properties;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationBuilder;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/04/2016
 */
abstract class AbstractConfigurationAwareTest extends TafTestBase {

    protected final TafConfiguration tafConfiguration = new TafConfigurationBuilder().build();

}
