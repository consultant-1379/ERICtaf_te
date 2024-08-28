package com.ericsson.cifwk.taf.executor.allure;

public class UploadScript {

    private final String localReportsStorage;
    private final String logSubDir;
    private final int expectedSuiteCount;
    private final boolean shouldUpload;
    private final boolean hasAllureService;

    private UploadScript(String localReportsStorage, String logSubDir,
                         int expectedSuiteCount, boolean shouldUpload, boolean hasAllureService) {
        this.localReportsStorage = localReportsStorage;
        this.logSubDir = logSubDir;
        this.expectedSuiteCount = expectedSuiteCount;
        this.shouldUpload = shouldUpload;
        this.hasAllureService = hasAllureService;
    }

    public String getLocalReportsStorage() {
        return localReportsStorage;
    }

    public String getLogSubDir() {
        return logSubDir;
    }

    public int getExpectedSuiteCount() {
        return expectedSuiteCount;
    }

    public boolean shouldUpload() {
        return shouldUpload;
    }

    public boolean hasAllureService() {
        return hasAllureService;
    }

    public static final class UploadScriptBuilder {
        private String localReportsStorage;
        private String logSubDir;
        private int expectedSuiteCount;
        private boolean shouldUpload;
        private boolean hasAllureService;

        private UploadScriptBuilder() {
        }

        public static UploadScriptBuilder anUploadScript() {
            return new UploadScriptBuilder();
        }

        public UploadScriptBuilder withLocalReportsStorage(String localReportsStorage) {
            this.localReportsStorage = localReportsStorage;
            return this;
        }

        public UploadScriptBuilder withLogSubDir(String logSubDir) {
            this.logSubDir = logSubDir;
            return this;
        }

        public UploadScriptBuilder withExpectedSuiteCount(int expectedSuiteCount) {
            this.expectedSuiteCount = expectedSuiteCount;
            return this;
        }

        public UploadScriptBuilder shouldUpload(boolean shouldUpload) {
            this.shouldUpload = shouldUpload;
            return this;
        }

        public UploadScriptBuilder hasAllureService(boolean hasAllureService) {
            this.hasAllureService = hasAllureService;
            return this;
        }

        public UploadScript build() {
            return new UploadScript(this.localReportsStorage, this.logSubDir, this.expectedSuiteCount,
                this.shouldUpload, this.hasAllureService);
        }
    }
}
