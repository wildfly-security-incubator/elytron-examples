package org.wildfly.security.examples;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.wildfly.extension.elytron.Configurable;
import org.wildfly.security.authz.RoleMapper;
import org.wildfly.security.authz.Roles;

/**
 * Simple role-mapper, which maps role from key in configuration map to appropriate value.
 */
public class MyRoleMapper implements RoleMapper, Configurable {

    private Map<String, String> configuration;

    @Override
    public void initialize(final Map<String, String> configuration) {
        this.configuration = configuration;
    }

    @Override
    public Roles mapRoles(Roles rolesToMap) {
        Set<String> mappedRoles = new HashSet<>();
        Iterator<String> iterator = rolesToMap.iterator();

        while(iterator.hasNext()) {
            String mappedRole = configuration.get(iterator.next());
            if (mappedRole != null) {
                mappedRoles.add(mappedRole);
            }
        }

        return Roles.fromSet(mappedRoles);
    }

}
