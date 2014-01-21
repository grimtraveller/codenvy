/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.services;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Map;

/**
 * Extended interface for {@link Job}
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class Feature implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(Feature.class);

    /**
     * Forcing job execution.
     *
     * @param context
     *         the execution context
     * @throws JobExecutionException
     */
    public void forceExecute(Map<String, String> context) throws JobExecutionException {
        try {
            if (!Parameters.FROM_DATE.get(context).equals(Parameters.TO_DATE.get(context))) {
                throw new IllegalStateException("Force execution is allowed only per day");
            }

            Map<String, String> newContext = Utils.clone(context);
            putParametersInContext(newContext);

            doExecute(newContext);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            Map<String, String> newContext = initializeDefaultContext();
            putParametersInContext(newContext);

            doExecute(newContext);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /** Initialize context if job is being executed on regular basis */
    protected Map<String, String> initializeDefaultContext() throws ParseException {
        Map<String, String> context = Utils.newContext();
        Parameters.TO_DATE.putDefaultValue(context);
        Parameters.FROM_DATE.put(context, Parameters.TO_DATE.getDefaultValue());

        return context;
    }

    /** If need to override context or put additional parameters to it, */
    protected abstract void putParametersInContext(Map<String, String> context);

    /**
     * Execution.
     *
     * @param context
     *         the execution context
     */
    protected abstract void doExecute(Map<String, String> context) throws Exception;

    /** @return true if feature can be executed on regular basis. */
    public abstract boolean isAvailable();
}
