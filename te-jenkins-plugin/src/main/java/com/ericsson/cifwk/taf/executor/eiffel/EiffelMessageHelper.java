package com.ericsson.cifwk.taf.executor.eiffel;

import com.ericsson.cifwk.taf.executor.api.ArtifactInfo;
import com.ericsson.cifwk.taf.executor.api.Host;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collection;

public class EiffelMessageHelper {

    public static String parseTestWares(Collection<ArtifactInfo> testWares) {
        String str = Joiner.on(",").skipNulls().join(
                Iterables.transform(testWares, new Function<ArtifactInfo, String>() {
                    @Override
                    public String apply(@Nullable ArtifactInfo gav) {
                        if (gav == null) return null;
                        return String.format("%s:%s:%s", gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
                    }
                })
        );
        return str;
    }

    public static String parseTestResource(Collection<Host> testResource) {
        String str = Joiner.on(",").skipNulls().join(
                Iterables.transform(testResource, new Function<Host, String>() {
                    @Override
                    public String apply(@Nullable Host host) {
                        if (host == null) return null;
                        return host.getIpAddress();
                    }
                })
        );
        return str;
    }

}
