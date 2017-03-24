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
package org.eclipse.che.ide.ext.bitbucket.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketConnection;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketConnectionProvider;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketProjectImporter;
import org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketExceptionMapper;
import org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketService;
import org.eclipse.che.inject.DynaModule;

/**
 * The module that contains configuration of the server side part of the Bitbucket extension.
 *
 * @author Kevin Pollet
 */
@DynaModule
public class BitbucketModule extends AbstractModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(BitbucketConnection.class).toProvider(BitbucketConnectionProvider.class);
        bind(BitbucketService.class);
        bind(BitbucketExceptionMapper.class);

        Multibinder.newSetBinder(binder(), ProjectImporter.class).addBinding().to(BitbucketProjectImporter.class);
        //Multibinder.newSetBinder(binder(), SshKeyUploader.class).addBinding().to(BitbucketKeyUploader.class);
    }
}
