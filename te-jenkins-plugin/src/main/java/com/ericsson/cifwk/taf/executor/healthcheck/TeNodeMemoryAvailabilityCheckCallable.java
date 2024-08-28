package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.de.tools.cli.CliTool;
import com.ericsson.de.tools.cli.CliToolShell;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.de.tools.cli.CliCommandResult;
import com.ericsson.de.tools.cli.CliTools;

public class TeNodeMemoryAvailabilityCheckCallable  extends AbstractTeNodeHealthCheckCallable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeNodeMemoryAvailabilityCheckCallable.class);

    private int minmemorySpaceGB;
    private String nodeName;

    public TeNodeMemoryAvailabilityCheckCallable(String nodeName, int minmemorySpaceGB) {
        super(nodeName);
        this.minmemorySpaceGB = minmemorySpaceGB;
        this.nodeName = nodeName;
    }

    @SuppressFBWarnings
    @Override
    public String doCheck(HealthParam check) {
        CliCommandResult result = null;
            try {
                LOGGER.info("Fetching free memory from Node " + nodeName);
                CliTool localCliTool = CliTools.local().build();
                 result = localCliTool.execute("free | grep Mem: | awk '{print $2,$4}' | tr -d '\n'",30);
                if (result.getExitCode() == 0) {
                    String[] output = result.getOutput().split(" ");
                    long totalMem = Integer.parseInt(output[0]);
                    long freeMem = Integer.parseInt(output[1]);
                    // int CacheMem =  Integer.parseInt(output[2]);
                    LOGGER.info("Total Memory : "+ totalMem );
                    LOGGER.info("Free Memory : "+ freeMem );
                    long freeMemPercentage = (freeMem * 100) / totalMem;
                    if (freeMemPercentage <= minmemorySpaceGB) {
                        LOGGER.info(String.format("Total free memory %s is less than %s percent of total memory %s GB ",
                                freeMemPercentage, minmemorySpaceGB,totalMem));
                        return failCheck(check, "Total free memory  "+ freeMemPercentage  + " is less than " +minmemorySpaceGB + " percent of total memory  " );
                    }
                }
                else {
                    return failCheck(check, "Failed to get free memory");
                }
            } catch (Exception e) { // NOSONAR
                LOGGER.error("Error while getting free memory " + e);
                return failCheck(check, "error querying free memory : " + result  + " " + e);
            }
        return toJson(check);
    }

    @Override
    public String getCheckName(String nodeName) {
        return "Node " + nodeName + " has adequate free memory ";
    }

}
