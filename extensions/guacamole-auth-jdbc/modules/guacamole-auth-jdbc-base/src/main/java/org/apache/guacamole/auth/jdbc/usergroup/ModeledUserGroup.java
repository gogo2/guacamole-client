/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.auth.jdbc.usergroup;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.jdbc.JDBCEnvironment;
import org.apache.guacamole.auth.jdbc.base.ModeledPermissions;
import org.apache.guacamole.auth.jdbc.user.ModeledAuthenticatedUser;
import org.apache.guacamole.form.BooleanField;
import org.apache.guacamole.form.Field;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.net.auth.RelatedObjectSet;
import org.apache.guacamole.net.auth.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the UserGroup object which is backed by a database model.
 */
public class ModeledUserGroup extends ModeledPermissions<UserGroupModel>
        implements UserGroup {

    /**
     * The Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModeledUserGroup.class);
    
    /**
     * All possible attributes of user groups organized as individual,
     * logical forms.
     */
    public static final Collection<Form> ATTRIBUTES = Collections.emptyList();
    
    /**
     * The names of all attributes which are explicitly supported by this
     * extension's UserGroup objects.
     */
    public static final Set<String> ATTRIBUTE_NAMES = Collections.emptySet();

    /**
     * Provider for RelatedObjectSets containing the user groups of which this
     * user group is a member.
     */
    @Inject
    private Provider<UserGroupParentUserGroupSet> parentUserGroupSetProvider;

    /**
     * Provider for RelatedObjectSets containing the users that are members of
     * this user group.
     */
    @Inject
    private Provider<UserGroupMemberUserSet> memberUserSetProvider;

    /**
     * Provider for RelatedObjectSets containing the user groups that are
     * members of this user group.
     */
    @Inject
    private Provider<UserGroupMemberUserGroupSet> memberUserGroupSetProvider;
    
    /**
     * The environment associated with this instance of the JDBC authentication
     * module.
     */
    @Inject
    private JDBCEnvironment environment;

    /**
     * Whether attributes which control access restrictions should be exposed
     * via getAttributes() or allowed to be set via setAttributes().
     */
    private boolean exposeRestrictedAttributes = false;

    /**
     * Initializes this ModeledUserGroup, associating it with the current
     * authenticated user and populating it with data from the given user group
     * model.
     *
     * @param currentUser
     *     The user that created or retrieved this object.
     *
     * @param model
     *     The backing model object.
     *
     * @param exposeRestrictedAttributes
     *     Whether attributes which control access restrictions should be
     *     exposed via getAttributes() or allowed to be set via
     *     setAttributes().
     */
    public void init(ModeledAuthenticatedUser currentUser, UserGroupModel model,
            boolean exposeRestrictedAttributes) {
        super.init(currentUser, model);
        this.exposeRestrictedAttributes = exposeRestrictedAttributes;
    }
    
    @Override
    public boolean isDisabled() {
        return getModel().isDisabled();
    }
    
    @Override
    public void setDisabled(boolean disabled) {
        getModel().setDisabled(disabled);
    }

    /**
     * Stores all restricted (privileged) attributes within the given Map,
     * pulling the values of those attributes from the underlying user group
     * model. If no value is yet defined for an attribute, that attribute will
     * be set to null.
     *
     * @param attributes
     *     The Map to store all restricted attributes within.
     */
    private void putRestrictedAttributes(Map<String, String> attributes) {

    }

    /**
     * Stores all restricted (privileged) attributes within the underlying user
     * group model, pulling the values of those attributes from the given Map.
     *
     * @param attributes
     *     The Map to pull all restricted attributes from.
     */
    private void setRestrictedAttributes(Map<String, String> attributes) {

    }

    @Override
    public Set<String> getSupportedAttributeNames() {
        return ATTRIBUTE_NAMES;
    }

    @Override
    public Map<String, String> getAttributes() {

        // Include any defined arbitrary attributes
        Map<String, String> attributes = super.getAttributes();

        // Include restricted attributes only if they should be exposed
        if (exposeRestrictedAttributes)
            putRestrictedAttributes(attributes);

        return attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {

        // Set arbitrary attributes
        super.setAttributes(attributes);

        // Assign restricted attributes only if they are exposed
        if (exposeRestrictedAttributes)
            setRestrictedAttributes(attributes);

    }

    @Override
    public RelatedObjectSet getUserGroups() throws GuacamoleException {
        UserGroupParentUserGroupSet parentUserGroupSet = parentUserGroupSetProvider.get();
        parentUserGroupSet.init(getCurrentUser(), this);
        return parentUserGroupSet;
    }

    @Override
    public RelatedObjectSet getMemberUsers() throws GuacamoleException {
        UserGroupMemberUserSet memberUserSet = memberUserSetProvider.get();
        memberUserSet.init(getCurrentUser(), this);
        return memberUserSet;
    }

    @Override
    public RelatedObjectSet getMemberUserGroups() throws GuacamoleException {
        UserGroupMemberUserGroupSet memberUserGroupSet = memberUserGroupSetProvider.get();
        memberUserGroupSet.init(getCurrentUser(), this);
        return memberUserGroupSet;
    }
    
    @Override
    public boolean isCaseSensitive() {
        try {
            return environment.getCaseSensitivity().caseSensitiveGroupNames();
        }
        catch (GuacamoleException e) {
            LOGGER.error("Error while retrieving case sensitivity configuration: {}. "
                       + "Group names comparisons will be case-sensitive.",
                      e.getMessage());
            LOGGER.debug("An exception was caught when attempting to retrieve the "
                       + "case sensitivity configuration.", e);
            return true;
        }
    }

}
