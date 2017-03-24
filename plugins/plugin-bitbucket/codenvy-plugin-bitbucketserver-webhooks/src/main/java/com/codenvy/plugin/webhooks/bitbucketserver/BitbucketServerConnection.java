/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.plugin.webhooks.bitbucketserver;

import com.codenvy.plugin.webhooks.bitbucketserver.shared.Clone;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Repository;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Optional;

/**
 * Wrapper class for calls to Visual Studio Team Services REST API
 *
 * @author Igor Vinokur
 */
public class BitbucketServerConnection {

    private static final Logger LOG = LoggerFactory.getLogger(BitbucketServerConnection.class);

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 bitbucketEndpoint;

    @Inject
    public BitbucketServerConnection(final HttpJsonRequestFactory httpJsonRequestFactory,
                                     @Named("bitbucket.endpoint") String bitbucketEndpoint) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.bitbucketEndpoint = bitbucketEndpoint;
    }

    String getRepositoryCloneUrl(String projectKey, String repositorySlug) throws ServerException {
        String url = bitbucketEndpoint + "/rest/api/latest/projects/" + projectKey + "/repos/" + repositorySlug;

        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url).useGetMethod();
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            Repository repository = response.asDto(Repository.class);
            LOG.debug("Repository obtained: {}", repository);
            Optional<Clone> optional = repository.getLinks()
                                                 .getClone()
                                                 .stream()
                                                 .filter(clone -> "http".equals(clone.getName()))
                                                 .findFirst();
            if (optional.isPresent()) {
                return optional.get().getHref();
            } else {
                LOG.error("shit");
                throw new ServerException("shit");
            }

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }
}
