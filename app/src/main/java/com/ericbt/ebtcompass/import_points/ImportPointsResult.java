package com.ericbt.ebtcompass.import_points;

public class ImportPointsResult {
    private int importedPointCount;

    private Exception exception;

    public int getImportedPointCount() {
        return importedPointCount;
    }

    public void setImportedPointCount(int importedPointCount) {
        this.importedPointCount = importedPointCount;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
