package org.wso2.identity.event.ssf.publisher.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.identity.event.ssf.publisher.api.exception.SSFAdapterClientException;
import org.wso2.identity.event.ssf.publisher.api.exception.SSFAdapterServerException;
import org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage;
import org.wso2.identity.event.ssf.publisher.internal.constant.SSFAdapterConstants;

/**
 * Utility class for SSFAdapter.
 */
public class SSFAdapterUtil {

    private SSFAdapterUtil() {

    }

    /**
     * Get the correlation ID.
     *
     * @return Correlation ID.
     */
    public static String getCorrelationID(SecurityEventTokenPayload eventTokenPayload) {

        return eventTokenPayload.getRci();
    }
    /**
     * Handle client exceptions.
     *
     * @param error Error message.
     * @param data  Data.
     * @return SSFAdapterClientException.
     */
    public static SSFAdapterClientException handleClientException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }
        return new SSFAdapterClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Handle server exceptions.
     *
     * @param error     Error message.
     * @param throwable Throwable.
     * @param data      Data.
     * @return SSFAdapterServerException.
     */
    public static SSFAdapterServerException handleServerException(ErrorMessage error, Throwable throwable,
                                                                    String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, (Object[]) data);
        }
        return new SSFAdapterServerException(error.getMessage(), description, error.getCode(), throwable);
    }

    /**
     * Print diagnostic log for publisher operations.
     *
     * @param eventProfileName Name of the event profile.
     * @param eventProfileUri  URI of the event profile.
     * @param events           Events published.
     * @param endpoint         Endpoint URL.
     * @param action           Action performed.
     * @param status           Result status.
     * @param message          Result message.
     */
    public static void printPublisherDiagnosticLog(String eventProfileName, String eventProfileUri, String events,
                                                    String endpoint, String action, DiagnosticLog.ResultStatus status,
                                                    String message) {

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    SSFAdapterConstants.LogConstants.SSF_ADAPTER, action);
            diagnosticLogBuilder
                    .inputParam(SSFAdapterConstants.LogConstants.InputKeys.ENDPOINT, endpoint)
                    .inputParam(SSFAdapterConstants.LogConstants.InputKeys.EVENT_URI, eventProfileUri)
                    .inputParam(SSFAdapterConstants.LogConstants.InputKeys.EVENT_PROFILE_NAME, eventProfileName)
                    .inputParam(SSFAdapterConstants.LogConstants.InputKeys.EVENTS, events)
                    .resultMessage(message)
                    .resultStatus(status)
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION);
            LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
        }
    }

}

