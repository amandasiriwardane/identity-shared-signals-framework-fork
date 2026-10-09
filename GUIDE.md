# SSF Connector Setup Guide

How to connect the Shared Signals Framework (SSF) / CAEP connector to a running WSO2 Identity Server instance. This covers only the connector itself (the jars built from `identity-shared-signals-framework`) - it assumes the server already has the prerequisite platform-level support from `carbon-identity-framework`, `identity-webhook-event-handlers`, and `identity-event-publishers`. Those are existing-code changes in other repos, not part of the connector, and ship as part of a normal IS release once their PRs merge.

## Prerequisites

- A running WSO2 IS 7.3.0+ instance.
- The server already has: `Webhook.properties` (Map<String, Object>) support and the `IDN_WEBHOOK_PROPERTIES` schema (`carbon-identity-framework`); the common event-hook infrastructure (`identity-webhook-event-handlers`); and the generic HTTP delivery adapter (`identity-event-publishers`). On a release that doesn't have these yet, see the handover notes for the prerequisite jars needed ahead of release - that's platform setup, not a connector-setup step, so it isn't repeated here.

## 1. Jars you need

| Jar | What it does |
|---|---|
| `org.wso2.identity.webhook.caep.event.handler-*.jar` | Builds CAEP-shaped event payloads (session established/presented/revoked, credential change) from WSO2's internal events. |
| `org.wso2.identity.event.ssf.publisher-*.jar` | Signs and delivers CAEP events as Security Event Tokens (SETs) to each stream's subscribed endpoint. |
| `org.wso2.identity.ssf.stream.management-*.jar` | Core stream CRUD - every SSF stream is a CAEP-profiled webhook under the hood. |
| `org.wso2.identity.ssf.discovery-*.jar` | Serves the public `/.well-known/ssf-configuration` discovery document. |
| `org.wso2.identity.ssf.endpoint.common-*.jar` | Shared OSGi lookup glue for the REST layer. |
| `org.wso2.identity.ssf.endpoint.v1-*.jar` | The actual `/ssf/stream` and `/ssf/stream/status` REST API. |


## 2. Copy jars to `<IS_HOME>/repository/components/dropins/`

```
org.wso2.identity.webhook.caep.event.handler-*.jar
org.wso2.identity.event.ssf.publisher-*.jar
org.wso2.identity.ssf.stream.management-*.jar
org.wso2.identity.ssf.discovery-*.jar
```

These are OSGi bundles. Don't also copy them into any webapp's `WEB-INF/lib` - duplicating a bundle into two classloaders causes a `ClassCastException` at runtime.

## 3. Copy jars to `<IS_HOME>/repository/deployment/server/webapps/api/WEB-INF/lib/`

```
org.wso2.identity.ssf.endpoint.common-*.jar
org.wso2.identity.ssf.endpoint.v1-*.jar
```

## 4. Register the REST resource class

File: `<IS_HOME>/repository/deployment/server/webapps/api/WEB-INF/web.xml`

Find the `jaxrs.serviceClasses` init-param under the servlet block that already lists the other v1 REST APIs, and add:

```
org.wso2.identity.ssf.endpoint.v1.SsfApi,
```

This is a manual step on every deployment

## 5. Exempt the discovery endpoint from authentication

If the server's `resource-access-control-v2.xml` doesn't already have this (it ships by default once the framework PR merges), add by hand to `<IS_HOME>/repository/conf/identity/resource-access-control-v2.xml`:

```xml
<Resource context="(.*)/.well-known/ssf-configuration(.*)" secured="false" http-method="all"/>
```

## 6. Configure `<IS_HOME>/repository/conf/deployment.toml`

```toml
[webhooks.properties]
enable = true

[[api_resources]]
name = "SSF Stream Management API"
identifier = "/ssf/stream"
requiresAuthorization = true
type = "TENANT"
description = "API for managing SSF event streams"

[[api_resources.scopes]]
displayName = "Read SSF Streams"
name = "ssf.read"

[[api_resources.scopes]]
displayName = "Manage SSF Streams"
name = "ssf.manage"

[[resource.access_control]]
context = "(.*)/api/server/v1/ssf/stream(.*)"
secure = "true"
http_method = "GET"
scopes = ["ssf.read"]

[[resource.access_control]]
context = "(.*)/api/server/v1/ssf/stream(.*)"
secure = "true"
http_method = "POST, PUT, PATCH, DELETE"
scopes = ["ssf.manage"]
```

## 7. Restart the server

## 8. Authorize a client application for the new scopes

In Console -> Applications -> (your application) -> Authorize APIs:

- Select **SSF Stream Management API**.
- Enable `ssf.read` and `ssf.manage`.
- Generate a new access token for that application, explicitly requesting these scopes (e.g. `scope=ssf.read ssf.manage`) - being authorized in Console only makes a scope *eligible*; the token request still has to ask for it.

## 9. Verify

- `GET https://<host>/.well-known/ssf-configuration` should return the discovery document, unauthenticated.
- `POST https://<host>/api/server/v1/ssf/stream` with a valid token should create a stream and return a populated `aud`.
- `GET` the same path (list) should also return populated `aud` / `events_requested` / `events_delivered` per stream, not empty arrays.

## 10. Example requests

Replace `<host>`, `<client_id>`, `<client_secret>`, and `<stream_id>` as needed. `-k` skips TLS verification, for a local server with a self-signed cert - drop it against a real one.

### Discovery document (no auth)

```bash
curl -k https://<host>/.well-known/ssf-configuration
```

### Get an access token

Scope must be explicitly requested - being authorized in Console only makes it *eligible*.

```bash
curl -k -X POST https://<host>/oauth2/token \
  -u "<client_id>:<client_secret>" \
  -d "grant_type=client_credentials&scope=ssf.read ssf.manage"
```

### Create a stream

```bash
curl -k -X POST https://<host>/api/server/v1/ssf/stream \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "delivery": {
      "method": "urn:ietf:rfc:8935",
      "endpoint_url": "https://example.com/webhook"
    },
    "events_requested": [
      "https://schemas.openid.net/secevent/caep/event-type/session-established",
      "https://schemas.openid.net/secevent/caep/event-type/session-revoked"
    ],
    "description": "Example receiver"
  }'
```

### List all streams

```bash
curl -k https://<host>/api/server/v1/ssf/stream \
  -H "Authorization: Bearer <access_token>"
```

### Get one stream

```bash
curl -k "https://<host>/api/server/v1/ssf/stream?stream_id=<stream_id>" \
  -H "Authorization: Bearer <access_token>"
```

### Partially update a stream (PATCH)

```bash
curl -k -X PATCH https://<host>/api/server/v1/ssf/stream \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "stream_id": "<stream_id>",
    "description": "Updated description"
  }'
```

### Fully replace a stream (PUT)

```bash
curl -k -X PUT https://<host>/api/server/v1/ssf/stream \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "stream_id": "<stream_id>",
    "delivery": {
      "method": "urn:ietf:rfc:8935",
      "endpoint_url": "https://example.com/webhook-v2"
    },
    "events_requested": [
      "https://schemas.openid.net/secevent/caep/event-type/session-established"
    ]
  }'
```

### Get a stream's status

```bash
curl -k "https://<host>/api/server/v1/ssf/stream/status?stream_id=<stream_id>" \
  -H "Authorization: Bearer <access_token>"
```

### Update a stream's status

```bash
curl -k -X POST https://<host>/api/server/v1/ssf/stream/status \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "stream_id": "<stream_id>",
    "status": "disabled"
  }'
```

### Delete a stream

```bash
curl -k -X DELETE "https://<host>/api/server/v1/ssf/stream?stream_id=<stream_id>" \
  -H "Authorization: Bearer <access_token>"
```
