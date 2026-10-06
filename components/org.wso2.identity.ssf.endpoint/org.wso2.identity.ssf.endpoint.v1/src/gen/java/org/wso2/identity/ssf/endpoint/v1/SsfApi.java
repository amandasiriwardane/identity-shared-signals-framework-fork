/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.ssf.endpoint.v1;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import java.io.InputStream;
import java.util.List;

import org.wso2.identity.ssf.endpoint.v1.model.Error;
import org.wso2.identity.ssf.endpoint.v1.model.StatusRequest;
import org.wso2.identity.ssf.endpoint.v1.model.StatusResponse;
import org.wso2.identity.ssf.endpoint.v1.model.StreamList;
import org.wso2.identity.ssf.endpoint.v1.model.StreamRequest;
import org.wso2.identity.ssf.endpoint.v1.model.StreamResponse;
import org.wso2.identity.ssf.endpoint.v1.SsfApiService;
import org.wso2.identity.ssf.endpoint.v1.factories.SsfApiServiceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@Path("/ssf")
@Api(description = "The ssf API")

public class SsfApi  {

    private final SsfApiService delegate;

    public SsfApi() {

        this.delegate = SsfApiServiceFactory.getSsfApi();
    }

    @Valid
    @POST
    @Path("/stream")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create Stream", notes = "Create a new SSF event stream.    <b>Scope(Permission) required:</b> `ssf.manage`   ", response = StreamResponse.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "SSF Stream", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Stream Created", response = StreamResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 500, message = "Internal server error", response = Error.class)
    })
    public Response createStream(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, @ApiParam(value = "" ,required=true) @Valid StreamRequest streamRequest) {

        return delegate.createStream(httpServletRequest, httpServletResponse, streamRequest );
    }

    @Valid
    @DELETE
    @Path("/stream")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete Stream", notes = "Delete a stream, identified by its stream_id query parameter.  <b>Scope(Permission) required:</b> `ssf.manage` ", response = Void.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "SSF Stream", })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Stream Deleted", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "Stream not found", response = Error.class),
        @ApiResponse(code = 500, message = "Internal server error", response = Error.class)
    })
    public Response deleteStream(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse,     @Valid @NotNull(message = "Property  cannot be null.") @ApiParam(value = "",required=true)  @QueryParam("stream_id") String streamId) {

        return delegate.deleteStream(httpServletRequest, httpServletResponse, streamId );
    }

    @Valid
    @GET
    @Path("/stream/status")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Stream Status", notes = "Get a stream's current status, identified by its stream_id query parameter.  <b>Scope(Permission) required:</b> `ssf.read` ", response = StatusResponse.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "SSF Stream", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = StatusResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "Stream not found", response = Error.class),
        @ApiResponse(code = 500, message = "Internal server error", response = Error.class)
    })
    public Response getStatus(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse,     @Valid @NotNull(message = "Property  cannot be null.") @ApiParam(value = "",required=true)  @QueryParam("stream_id") String streamId) {

        return delegate.getStatus(httpServletRequest, httpServletResponse, streamId );
    }

    @Valid
    @GET
    @Path("/stream")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Stream(s)", notes = "Get a single stream's configuration by its stream_id query parameter, or - if stream_id is omitted - every stream's configuration for the tenant.  <b>Scope(Permission) required:</b> `ssf.read` ", response = StreamList.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "SSF Stream", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A single StreamResponse if stream_id was given, otherwise a StreamList. ", response = StreamList.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "Stream not found", response = Error.class),
        @ApiResponse(code = 500, message = "Internal server error", response = Error.class)
    })
    public Response getStreams(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse,     @Valid@ApiParam(value = "Stream ID. If omitted, every stream for the tenant is returned.")  @QueryParam("stream_id") String streamId) {

        return delegate.getStreams(httpServletRequest, httpServletResponse, streamId );
    }

    @Valid
    @PUT
    @Path("/stream")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Replace Stream", notes = "Fully replace a stream's Receiver-Supplied configuration. The stream is identified by the stream_id field in the request body.  <b>Scope(Permission) required:</b> `ssf.manage` ", response = StreamResponse.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "SSF Stream", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Stream Replaced", response = StreamResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "Stream not found", response = Error.class),
        @ApiResponse(code = 500, message = "Internal server error", response = Error.class)
    })
    public Response replaceStream(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, @ApiParam(value = "" ,required=true) @Valid StreamRequest streamRequest) {

        return delegate.replaceStream(httpServletRequest, httpServletResponse, streamRequest );
    }

    @Valid
    @POST
    @Path("/stream/status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update Stream Status", notes = "Enable or disable a stream, identified by the stream_id field in the request body. Only \"enabled\" and \"disabled\" are supported; \"paused\" is a valid value per the SSF spec but is rejected here, since it only has meaning for poll-based delivery.  <b>Scope(Permission) required:</b> `ssf.manage` ", response = StatusResponse.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "SSF Stream", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Status Updated", response = StatusResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "Stream not found", response = Error.class),
        @ApiResponse(code = 500, message = "Internal server error", response = Error.class)
    })
    public Response updateStatus(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, @ApiParam(value = "" ,required=true) @Valid StatusRequest statusRequest) {

        return delegate.updateStatus(httpServletRequest, httpServletResponse, statusRequest );
    }

    @Valid
    @PATCH
    @Path("/stream")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update Stream", notes = "Partially update a stream - only Receiver-Supplied fields present on the request are changed. The stream is identified by the stream_id field in the request body.  <b>Scope(Permission) required:</b> `ssf.manage` ", response = StreamResponse.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "SSF Stream" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Stream Updated", response = StreamResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "Stream not found", response = Error.class),
        @ApiResponse(code = 500, message = "Internal server error", response = Error.class)
    })
    public Response updateStream(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, @ApiParam(value = "" ,required=true) @Valid StreamRequest streamRequest) {

        return delegate.updateStream(httpServletRequest, httpServletResponse, streamRequest );
    }

}
