package com.alness.lifemaster.permissions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;

import com.alness.lifemaster.permissions.security.PermissionRouteResolver;
import com.alness.lifemaster.permissions.security.PermissionTarget;

class PermissionRouteResolverTests {
    private final PermissionRouteResolver resolver = new PermissionRouteResolver("/api/v1");

    @ParameterizedTest
    @CsvSource({
            "GET,/api/v1/category,categories,READ",
            "POST,/api/v1/category,categories,CREATE",
            "PUT,/api/v1/category/3fd90c4a,categories,UPDATE",
            "DELETE,/api/v1/category/3fd90c4a,categories,DELETE",
            "GET,/api/v1/users/4f94fd16/expenses,expenses,READ",
            "POST,/api/v1/users/4f94fd16/income,incomes,CREATE",
            "GET,/api/v1/users/4f94fd16/accounts,financial-accounts,READ",
            "POST,/api/v1/users/4f94fd16/assistance,asistencia,CREATE",
            "GET,/api/v1/users/4f94fd16/bank-imports/template.xlsx,bank-import,READ",
            "POST,/api/v1/users/4f94fd16/bank-imports/file,bank-import,CREATE",
            "PATCH,/api/v1/users/4f94fd16/alerts/abc/read,alerts,UPDATE",
            "GET,/api/v1/audit-events/search,audit,READ",
            "GET,/api/v1/profiles,users,READ",
            "PUT,/api/v1/modules/abc,app-modules,UPDATE",
            "PUT,/api/v1/permissions/profiles/a/modules/b,app-modules,UPDATE"
    })
    void resolvesBackendRoutesToStableModulePermissions(
            String method, String uri, String permissionKey, PermissionAction action) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);

        PermissionTarget target = resolver.resolve(request).orElseThrow();

        assertThat(target.permissionKey()).isEqualTo(permissionKey);
        assertThat(target.action()).isEqualTo(action);
    }
}
