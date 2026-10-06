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

import org.wso2.identity.ssf.endpoint.v1.*;
import org.wso2.identity.ssf.endpoint.v1.model.*;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;


public interface SsfApiService {

      public Response createStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, StreamRequest streamRequest);

      public Response deleteStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String streamId);

      public Response getStatus(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String streamId);

      public Response getStreams(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String streamId);

      public Response replaceStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, StreamRequest streamRequest);

      public Response updateStatus(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, StatusRequest statusRequest);

      public Response updateStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, StreamRequest streamRequest);
}
