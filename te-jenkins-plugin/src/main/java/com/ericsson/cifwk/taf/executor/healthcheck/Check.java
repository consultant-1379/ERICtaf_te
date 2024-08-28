package com.ericsson.cifwk.taf.executor.healthcheck;

public interface Check {

    void check(HealthCheckContext context);

    public static class Result {
        boolean success;
        String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

}
