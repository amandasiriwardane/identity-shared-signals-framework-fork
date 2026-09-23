package org.wso2.identity.webhook.caep.event.handler.api.builder;

import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.publisher.api.model.EventPayload;
import org.wso2.identity.webhook.caep.event.handler.internal.model.CAEPTokenClaimsChangeEventPayload;
import org.wso2.identity.webhook.common.event.handler.api.builder.TokenEventPayloadBuilder;
import org.wso2.identity.webhook.common.event.handler.api.constants.Constants;
import org.wso2.identity.webhook.common.event.handler.api.model.EventData;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for building CAEP token claims change event payloads.
 */
public class CAEPTokenEventPayloadBuilder implements TokenEventPayloadBuilder {

    @Override
    public EventPayload buildTokenClaimsChangeEvent(EventData eventData) throws IdentityEventException {

        // TODO: placeholder claims - real assembly mechanism is an open design question
        // (full snapshot vs. diff vs. specific-trigger scope), not yet resolved.
        Map<String, String> claims = new HashMap<>();
        claims.put("placeholder_claim", "placeholder_value");

        return new CAEPTokenClaimsChangeEventPayload.Builder()
                .eventTimeStamp(System.currentTimeMillis())
                .claims(claims)
                .build();
    }

    @Override
    public EventPayload buildAccessTokenRevokeEvent(EventData eventData) throws IdentityEventException {
        // No corresponding CAEP event type for token revoke/issue - CAEP only defines
        // token-claims-change. Not implemented.
        return null;
    }

    @Override
    public EventPayload buildAccessTokenIssueEvent(EventData eventData) throws IdentityEventException {
        return null;
    }

    @Override
    public Constants.EventSchema getEventSchemaType() {
        return Constants.EventSchema.CAEP;
    }
}
