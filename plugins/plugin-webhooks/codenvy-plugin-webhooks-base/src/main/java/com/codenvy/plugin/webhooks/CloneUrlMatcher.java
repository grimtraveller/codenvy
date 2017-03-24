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
package com.codenvy.plugin.webhooks;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Matcher that checks clone url to be related to given project and branch.
 *
 * @author Igor Vinokur
 */
public interface CloneUrlMatcher {

    /**
     * Default implementation of {@link CloneUrlMatcher}.
     */
    CloneUrlMatcher DEFAULT_CLONE_URL_MATCHER = (project, repositoryUrl, branch) -> {
        if (isNullOrEmpty(repositoryUrl) || isNullOrEmpty(branch)) {
            return false;
        }

        final SourceStorageDto source = project.getSource();
        if (source == null) {
            return false;
        }

        final String projectType = source.getType();
        final String projectLocation = source.getLocation();
        final String projectBranch = source.getParameters().get("branch");

        if (isNullOrEmpty(projectType) || isNullOrEmpty(projectLocation)) {
            return false;
        }
        return (repositoryUrl.equals(projectLocation) || (repositoryUrl + ".git").equals(projectLocation)) &&
               ("master".equals(branch) || (!isNullOrEmpty(projectBranch) && branch.equals(projectBranch)));
    };

    /**
     * Whether or not a given clone url matches given repository and branch of the given project.
     *
     * @param project
     *         the project to check
     * @param cloneUrl
     *         the clone URL that the project source location has to match
     * @param branch
     *         the branch that the project has to match
     */
    boolean isCloneUrlMatching(final ProjectConfigDto project, final String cloneUrl, final String branch);
}
