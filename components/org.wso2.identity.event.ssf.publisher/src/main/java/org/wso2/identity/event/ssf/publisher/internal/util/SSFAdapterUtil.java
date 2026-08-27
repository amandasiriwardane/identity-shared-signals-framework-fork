package org.wso2.identity.event.ssf.publisher.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.identity.event.ssf.publisher.api.exception.SSFAdapterClientException;
import org.wso2.identity.event.ssf.publisher.api.exception.SSFAdapterServerException;
import org.wso2.identity.event.ssf.publisher.internal.constant.ErrorMessage;

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
}
