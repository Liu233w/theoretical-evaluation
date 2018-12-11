package edu.nwpu.soft.ma.theoreticalEvaluation.runner;

public class CoverageRunnerException extends Exception {
    public CoverageRunnerException() {
    }

    public CoverageRunnerException(String message) {
        super(message);
    }

    public CoverageRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CoverageRunnerException(Throwable cause) {
        super(cause);
    }

    public CoverageRunnerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
