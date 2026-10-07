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

package org.wso2.identity.ssf.endpoint.v1.impl;

import org.wso2.identity.ssf.endpoint.v1.SsfApiService;
import org.wso2.identity.ssf.endpoint.v1.core.ServerSSFStreamService;
import org.wso2.identity.ssf.endpoint.v1.factories.ServerSSFStreamServiceFactory;
import org.wso2.identity.ssf.endpoint.v1.model.StatusRequest;
import org.wso2.identity.ssf.endpoint.v1.model.StreamRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * Implementation of the SsfApiService.
 */
public class SsfApiServiceImpl implements SsfApiService {

    private final ServerSSFStreamService serverSSFStreamService;

    public SsfApiServiceImpl() {

        try {
            serverSSFStreamService = ServerSSFStreamServiceFactory.getServerSSFStreamService();
        } catch (IllegalStateException e) {
            throw new RuntimeException("Error occurred while retrieving SSFStreamManagementService.", e);
        }
    }

    @Override
    public Response createStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                  StreamRequest streamRequest) {

        return Response.status(Response.Status.CREATED)
                .entity(serverSSFStreamService.createStream(streamRequest, httpServletRequest))
                .build();
    }

    @Override
    public Response getStreams(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                String streamId) {

        if (streamId != null) {
            return Response.ok().entity(serverSSFStreamService.getStream(streamId)).build();
        }
        return Response.ok().entity(serverSSFStreamService.getStreams()).build();
    }

    @Override
    public Response updateStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                  StreamRequest streamRequest) {

        return Response.ok().entity(serverSSFStreamService.updateStream(streamRequest, httpServletRequest)).build();
    }

    @Override
    public Response replaceStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                   StreamRequest streamRequest) {

        return Response.ok().entity(serverSSFStreamService.replaceStream(streamRequest, httpServletRequest)).build();
    }

    @Override
    public Response deleteStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                  String streamId) {

        serverSSFStreamService.deleteStream(streamId, httpServletRequest);
        return Response.noContent().build();
    }

    @Override
    public Response getStatus(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               String streamId) {

        return Response.ok().entity(serverSSFStreamService.getStatus(streamId)).build();
    }

    @Override
    public Response updateStatus(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                  StatusRequest statusRequest) {

        return Response.ok().entity(serverSSFStreamService.updateStatus(statusRequest)).build();
    }
}
