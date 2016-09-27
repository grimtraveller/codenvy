/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.organization.api.permissions;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.api.OrganizationService;
import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.codenvy.organization.api.permissions.OrganizationDomain.DELETE;
import static com.codenvy.organization.api.permissions.OrganizationDomain.DOMAIN_ID;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_SUBORGANIZATIONS;
import static com.codenvy.organization.api.permissions.OrganizationDomain.UPDATE;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link OrganizationPermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class OrganizationPermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER = new EnvironmentFilter();

    @Mock
    private OrganizationService service;

    @Mock
    private OrganizationManager manager;

    @Mock
    private static Subject subject;

    @InjectMocks
    @Spy
    private OrganizationPermissionsFilter permissionsFilter;

    @BeforeMethod
    public void setUp() throws Exception {
        when(manager.getById(anyString())).thenReturn(new OrganizationImpl("organization123", "test", null));
    }

    @Test
    public void shouldNotCheckPermissionsOnGettingOrganizationById() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .when()
               .get(SECURE_PATH + "/organization/organization123");

        verify(service).getById(eq("organization123"));
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldNotCheckPermissionsOnGettingOrganizationByName() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .when()
               .get(SECURE_PATH + "/organization/find?name=test");

        verify(service).find(eq("test"));
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldNotCheckPermissionsOnGettingOrganizations() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .when()
               .get(SECURE_PATH + "/organization");

        verify(service).getOrganizations(anyInt(), anyInt());
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnOrganizationUpdating() throws Exception {
        when(subject.hasPermission(DOMAIN_ID, "organization123", UPDATE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/organization/organization123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).update(eq("organization123"), any());
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("organization123"), eq(UPDATE));
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnParentOrgLevelOnChildOrganizationUpdating() throws Exception {
        when(manager.getById(anyString())).thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
        when(subject.hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/organization/organization123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).update(eq("organization123"), any());
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("parent123"), eq(MANAGE_SUBORGANIZATIONS));
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnChildOrganizationUpdatingWhenUserDoesNotHavePermissionsOnParentOrgLevel() throws Exception {
        when(manager.getById(anyString())).thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
        when(subject.hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS)).thenReturn(false);
        when(subject.hasPermission(DOMAIN_ID, "organization123", UPDATE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/organization/organization123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).update(eq("organization123"), any());
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("parent123"), eq(MANAGE_SUBORGANIZATIONS));
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("organization123"), eq(UPDATE));
    }

    @Test
    public void shouldCheckPermissionsOnOrganizationRemoving() throws Exception {
        when(subject.hasPermission(DOMAIN_ID, "organization123", DELETE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/organization/organization123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).remove(eq("organization123"));
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("organization123"), eq(DELETE));
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnParentOrgLevelOnChildOrganizationRemoving() throws Exception {
        when(manager.getById(anyString())).thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
        when(subject.hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/organization/organization123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).remove(eq("organization123"));
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("parent123"), eq(MANAGE_SUBORGANIZATIONS));
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnChildOrganizationRemovingWhenUserDoesNotHavePermissionsOnParentOrgLevel() throws Exception {
        when(manager.getById(anyString())).thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
        when(subject.hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS)).thenReturn(false);
        when(subject.hasPermission(DOMAIN_ID, "organization123", DELETE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/organization/organization123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).remove(eq("organization123"));
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("parent123"), eq(MANAGE_SUBORGANIZATIONS));
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("organization123"), eq(DELETE));
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void shouldNotCheckPermissionsOnOrganizationsGetting() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/organization");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getOrganizations(anyInt(), anyInt());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldNotCheckPermissionsOnRootOrganizationCreation() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .body(DtoFactory.newDto(OrganizationDto.class)
                                                         .withParent(null))
                                         .post(SECURE_PATH + "/organization");

        assertEquals(response.getStatusCode(), 204);
        verify(service).create(any());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnChildOrganizationCreation() throws Exception {
        when(subject.hasPermission(DOMAIN_ID, "parent-org", MANAGE_SUBORGANIZATIONS)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .body(DtoFactory.newDto(OrganizationDto.class)
                                                         .withParent("parent-org"))
                                         .post(SECURE_PATH + "/organization");

        assertEquals(response.getStatusCode(), 204);
        verify(service).create(any());
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("parent-org"), eq(MANAGE_SUBORGANIZATIONS));
    }

    @Test
    public void shouldThrowForbiddenExceptionOnChildOrganizationCreationIfUserDoesNotHaveCorrespondingPermission() throws Exception {
        when(subject.hasPermission(DOMAIN_ID, "parent-org", MANAGE_SUBORGANIZATIONS)).thenReturn(false);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .body(DtoFactory.newDto(OrganizationDto.class)
                                                         .withParent("parent-org"))
                                         .post(SECURE_PATH + "/organization");

        assertEquals(response.getStatusCode(), 403);
        verifyZeroInteractions(service);
        verify(subject).hasPermission(eq(DOMAIN_ID), eq("parent-org"), eq(MANAGE_SUBORGANIZATIONS));
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "The user does not have permission to perform this operation")
    public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
        final GenericResourceMethod mock = mock(GenericResourceMethod.class);
        Method injectLinks = OrganizationService.class.getMethod("getServiceDescriptor");
        when(mock.getMethod()).thenReturn(injectLinks);

        permissionsFilter.filter(mock, new Object[] {});
    }

    @Test(dataProvider = "coveredPaths")
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForPerformOperation(String path,
                                                                                               String method,
                                                                                               String action) throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

        Response response = request(given().auth()
                                           .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                           .contentType("application/json")
                                           .when(),
                                    SECURE_PATH + path,
                                    method);

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "The user does not have permission to " + action + " organization with id 'organization123'");

        verifyZeroInteractions(service);
    }

    @Test(dataProvider = "coveredPaths")
    public void shouldThrowNotFoundWhenUserRequestsNonExistedOrganization(String path,
                                                                          String method,
                                                                          String ignored) throws Exception {
        when(manager.getById(anyString())).thenThrow(new NotFoundException("Organization was not found"));

        Response response = request(given().auth()
                                           .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                           .contentType("application/json")
                                           .when(),
                                    SECURE_PATH + path,
                                    method);

        assertEquals(response.getStatusCode(), 404);
        assertEquals(unwrapError(response), "Organization was not found");

        verifyZeroInteractions(service);
    }

    @DataProvider(name = "coveredPaths")
    public Object[][] pathsProvider() {
        return new Object[][] {
                {"/organization/organization123", "post", UPDATE},
                {"/organization/organization123", "delete", DELETE},
                {"/organization/organization123/organizations", "get", MANAGE_SUBORGANIZATIONS}
        };
    }

    private Response request(RequestSpecification request, String path, String method) {
        switch (method) {
            case "post":
                return request.post(path);
            case "get":
                return request.get(path);
            case "delete":
                return request.delete(path);
            case "put":
                return request.put(path);
        }
        throw new RuntimeException("Unsupported method");
    }

    private static String unwrapError(Response response) {
        return unwrapDto(response, ServiceError.class).getMessage();
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}