/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecurityConfig {

    private boolean enabled = false;

    private CredentialsFactoryConfig memberCredentialsConfig = new CredentialsFactoryConfig();

    private List<LoginModuleConfig> memberLoginModuleConfigs = new ArrayList<LoginModuleConfig>();

    private List<LoginModuleConfig> clientLoginModuleConfigs = new ArrayList<LoginModuleConfig>();

    private PermissionPolicyConfig clientPolicyConfig = new PermissionPolicyConfig();

    private Set<PermissionConfig> clientPermissionConfigs = new HashSet<PermissionConfig>();

    public boolean isEnabled() {
        return enabled;
    }

    public SecurityConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public SecurityConfig addMemberLoginModuleConfig(LoginModuleConfig loginModuleConfig) {
        memberLoginModuleConfigs.add(loginModuleConfig);
        return this;
    }

    public SecurityConfig addClientLoginModuleConfig(LoginModuleConfig loginModuleConfig) {
        clientLoginModuleConfigs.add(loginModuleConfig);
        return this;
    }

    public SecurityConfig addClientPermissionConfig(PermissionConfig permissionConfig) {
        clientPermissionConfigs.add(permissionConfig);
        return this;
    }

    public List<LoginModuleConfig> getClientLoginModuleConfigs() {
        return clientLoginModuleConfigs;
    }

    public SecurityConfig setClientLoginModuleConfigs(List<LoginModuleConfig> loginModuleConfigs) {
        this.clientLoginModuleConfigs = loginModuleConfigs;
        return this;
    }

    public List<LoginModuleConfig> getMemberLoginModuleConfigs() {
        return memberLoginModuleConfigs;
    }

    public SecurityConfig setMemberLoginModuleConfigs(List<LoginModuleConfig> memberLoginModuleConfigs) {
        this.memberLoginModuleConfigs = memberLoginModuleConfigs;
        return this;
    }

    public PermissionPolicyConfig getClientPolicyConfig() {
        return clientPolicyConfig;
    }

    public SecurityConfig setClientPolicyConfig(PermissionPolicyConfig policyConfig) {
        this.clientPolicyConfig = policyConfig;
        return this;
    }

    public Set<PermissionConfig> getClientPermissionConfigs() {
        return clientPermissionConfigs;
    }

    public SecurityConfig setClientPermissionConfigs(Set<PermissionConfig> permissions) {
        this.clientPermissionConfigs = permissions;
        return this;
    }

    public CredentialsFactoryConfig getMemberCredentialsConfig() {
        return memberCredentialsConfig;
    }

    public SecurityConfig setMemberCredentialsConfig(CredentialsFactoryConfig credentialsFactoryConfig) {
        this.memberCredentialsConfig = credentialsFactoryConfig;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SecurityConfig");
        sb.append("{enabled=").append(enabled);
        sb.append(", memberCredentialsConfig=").append(memberCredentialsConfig);
        sb.append(", memberLoginModuleConfigs=").append(memberLoginModuleConfigs);
        sb.append(", clientLoginModuleConfigs=").append(clientLoginModuleConfigs);
        sb.append(", clientPolicyConfig=").append(clientPolicyConfig);
        sb.append(", clientPermissionConfigs=").append(clientPermissionConfigs);
        sb.append('}');
        return sb.toString();
    }
}
