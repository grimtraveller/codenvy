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
package com.codenvy.plugin.pullrequest.client.steps;

import com.codenvy.plugin.pullrequest.client.ContributeMessages;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.shared.dto.HostUser;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

import javax.inject.Inject;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * This step authorizes Codenvy on the VCS Host.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class AuthorizeCodenvyOnVCSHostStep implements Step {
    private final NotificationManager notificationManager;
    private final AppContext          appContext;
    private final ContributeMessages  messages;

    @Inject
    public AuthorizeCodenvyOnVCSHostStep(final NotificationManager notificationManager,
                                         final AppContext appContext,
                                         final ContributeMessages messages) {
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.messages = messages;
    }

    @Override
    public void execute(final WorkflowExecutor executor, Context context) {
        context.getVcsHostingService()
               .getUserInfo()
               .then(authSuccessOp(executor, context))
               .catchError(getUserErrorOp(executor, context));
    }

    private Operation<HostUser> authSuccessOp(final WorkflowExecutor executor, final Context context) {
        return new Operation<HostUser>() {
            @Override
            public void apply(HostUser user) throws OperationException {
                context.setHostUserLogin(user.getLogin());
                executor.done(AuthorizeCodenvyOnVCSHostStep.this, context);
            }
        };
    }

    private Operation<PromiseError> getUserErrorOp(final WorkflowExecutor executor, final Context context) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                try {
                    throw error.getCause();
                } catch (UnauthorizedException unEx) {
                    authenticate(executor, context);
                } catch (Throwable thr) {
                    handleThrowable(thr, executor, context);
                }
            }
        };
    }

    private void authenticate(final WorkflowExecutor executor, final Context context) {
        context.getVcsHostingService()
               .authenticate(appContext.getCurrentUser())
               .then(authSuccessOp(executor, context))
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError err) throws OperationException {
                       try {
                           throw err.getCause();
                       } catch (UnauthorizedException unEx) {
                           notificationManager.notify(messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostTitle(),
                                                      messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostContent(),
                                                      FAIL,
                                                      FLOAT_MODE);
                           executor.fail(AuthorizeCodenvyOnVCSHostStep.this, context, unEx.getLocalizedMessage());
                       } catch (Throwable thr) {
                           handleThrowable(thr, executor, context);
                       }
                   }
               });
    }

    private void handleThrowable(final Throwable thr, final WorkflowExecutor workflow, final Context context) {
        // If VCS provider does not support retrieving authorized user, continue creating pull request.
        // In this case, pull request will be created from repository's branch.
        if (getErrorCode(thr) == ErrorCodes.FAILED_TO_RETRIEVE_AUTHORIZED_TO_VCS_PROVIDER_USER) {
            workflow.done(AuthorizeCodenvyOnVCSHostStep.this, context);
        } else {
            notificationManager.notify(thr.getLocalizedMessage(), FAIL, FLOAT_MODE);
            workflow.fail(this, context, thr.getLocalizedMessage());
        }
    }
}
