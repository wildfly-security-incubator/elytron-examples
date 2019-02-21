/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.auth.server.ModifiableRealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.authz.Attributes;
import org.wildfly.security.authz.MapAttributes;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.spec.ClearPasswordSpec;

import static org.wildfly.security.authz.RoleDecoder.KEY_ROLES;
import static org.wildfly.security.password.interfaces.ClearPassword.ALGORITHM_CLEAR;

@PermitAll
@Path("/")
public class Identity {

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"Admin", "User"})
    public String getIdentityInfo() {
        return identityInfoAsJson();
    }

    @POST
    @Path("/attribute/update/{key}")
    @RolesAllowed({"Admin", "User"})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAttribute(@PathParam("key") String key, String json) {
        if (invalidRequest(key, json)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        ModifiableRealmIdentity modifiableIdentity = null;
        try {
            modifiableIdentity = getModifiableIdentity();
            return update(modifiableIdentity, key, json);
        } catch (RealmUnavailableException e) {
            return Response.serverError().build();
        } finally {
            if (modifiableIdentity != null) {
                modifiableIdentity.dispose();
            }
        }
    }

    private Response update(ModifiableRealmIdentity modifiableIdentity, String key, String json) throws RealmUnavailableException {
        MapAttributes attributes = new MapAttributes(modifiableIdentity.getAttributes());
        if (attributes.containsKey(key)) {
            attributes.copyAndReplace(key, getValuesFromJson(new JSONArray(json.trim())));
            modifiableIdentity.setAttributes(attributes);
            return Response.ok("Successfully updated").build();
        } else {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @POST
    @Path("/password/update")
    @Consumes("text/plain")
    @RolesAllowed({"Admin", "User"})
    public Response updatePassword(String newPassword) {
        ModifiableRealmIdentity modifiableIdentity = null;
        try {
            modifiableIdentity = getModifiableIdentity();
            setNewPassword(modifiableIdentity, newPassword);
            return Response.ok("Password updated").build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            if (modifiableIdentity != null) {
                modifiableIdentity.dispose();
            }
        }
    }

    private void setNewPassword(ModifiableRealmIdentity modifiableIdentity, String newPassword) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(ALGORITHM_CLEAR, new WildFlyElytronProvider());
        PasswordCredential updatedPassword = new PasswordCredential(passwordFactory.generatePassword(new ClearPasswordSpec(newPassword.toCharArray())));
        HashSet<Credential> newCredentials = new HashSet<Credential>();
        newCredentials.add(updatedPassword);
        modifiableIdentity.setCredentials(newCredentials);
    }

    private ModifiableRealmIdentity getModifiableIdentity() throws RealmUnavailableException {
        return SecurityDomain.getCurrent().getIdentityForUpdate(securityContext.getUserPrincipal());
    }

    private String identityInfoAsJson() {
        SecurityIdentity identity = SecurityDomain.getCurrent().getCurrentSecurityIdentity();
        JSONObject response = new JSONObject();
        response.put("name", securityContext.getUserPrincipal().getName());
        response.put("attributes", attributesAsJson(identity.getAttributes()));
        return response.toString();
    }

    private JSONObject attributesAsJson(Attributes attributes) {
        JSONObject attributesObject = new JSONObject();
        for (String attribute : attributes.keySet()) {
            attributesObject.put(attribute, attributes.get(attribute));
        }
        return attributesObject;
    }

    private List<String> getValuesFromJson(JSONArray jsonArray) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

    private boolean invalidRequest(String key, String json) {
        if (key.equals(KEY_ROLES)) {
            throw new ForbiddenException("Cannot update Roles");
        }
        if (json == null || json.length() == 0) {
            return true;
        }
        try {
            new JSONArray(json);
        } catch (JSONException ex1) {
            return true;
        }
        return false;
    }
}
