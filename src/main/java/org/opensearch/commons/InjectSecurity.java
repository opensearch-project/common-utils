/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons;

import static org.opensearch.commons.ConfigConstants.INJECTED_USER;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_INJECTED_ROLES;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USE_INJECTED_USER_FOR_PLUGINS;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.common.Strings;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;

/**
 * For background jobs usage only. User or Roles injection can be done using transport layer only.
 * You can't inject using REST api.
 *
 * Roles injection is based on this new feature in security plugin: https://github.com/opensearch-project/security/pull/560
 *
 * Java example Usage:
 *
 *      try (InjectSecurity injectSecurity = new InjectSecurity(id, settings, client.threadPool().getThreadContext())) {
 *
 *          //add roles to be injected from the configuration.
 *          injectSecurity.inject("user, Arrays.toList("role_1,role_2"));
 *
 *          //OpenSearch calls that needs to executed in security context.
 *
 *          SearchRequestBuilder searchRequestBuilder = client.prepareSearch(monitor.indexpattern);
 *          SearchResponse searchResponse = searchRequestBuilder
 *                             .setFrom(0).setSize(100).setExplain(true).  execute().actionGet();
 *
 *      } catch (final OpenSearchSecurityException ex){
 *            //handle the security exception
 *      }
 *
 * Kotlin usage with Coroutines:
 *
 *    //You can also use launch, based on usecase.
 *    runBlocking(RolesInjectorContextElement(monitor.id, settings, threadPool.threadContext, monitor.associatedRoles)) {
 *       //OpenSearch calls that needs to executed in security context.
 *    }
 *
 *    class InjectContextElement(val id: String, val settings: Settings, val threadContext: ThreadContext, val roles: String)
 *     : ThreadContextElement<Unit> {
 *
 *     companion object Key : CoroutineContext.Key<RolesInjectorContextElement>
 *     override val key: CoroutineContext.Key<*>
 *         get() = Key
 *
 *     var injectSecurity = InjectSecurity(id, settings, threadContext)
 *
 *     override fun updateThreadContext(context: CoroutineContext) {
 *         injectSecurity.injectRoles(roles)
 *     }
 *
 *     override fun restoreThreadContext(context: CoroutineContext, oldState: Unit) {
 *         injectSecurity.close()
 *     }
 *   }
 *
 */
public class InjectSecurity implements AutoCloseable {

    private String id;
    private ThreadContext.StoredContext ctx = null;
    private ThreadContext threadContext;
    private Settings settings;
    private final Logger log = LogManager.getLogger(this.getClass());

    /**
     * Create InjectSecurity object. This is auto-closeable. Id is used only for logging purpose.
     * @param id
     * @param settings
     * @param tc
     */
    public InjectSecurity(final String id, final Settings settings, final ThreadContext tc) {
        this.id = id;
        this.settings = settings;
        this.threadContext = tc;

        this.ctx = tc.newStoredContext(true);
        log.trace("{}, InjectSecurity constructor: {}", Thread.currentThread().getName(), id);
    }

    /**
     * Injects user or roles, based on opendistro_security_use_injected_user_for_plugins setting. By default injects roles.
     * @param user
     * @param roles
     */
    public void inject(final String user, final List<String> roles) {
        boolean injectUser = settings.getAsBoolean(OPENSEARCH_SECURITY_USE_INJECTED_USER_FOR_PLUGINS, false);
        if (injectUser)
            injectUser(user);
        else
            injectRoles(roles);
    }

    /**
     * Injects user.
     * @param user
     */
    public void injectUser(final String user) {
        if (Strings.isNullOrEmpty(user)) {
            return;
        }

        if (threadContext.getTransient(INJECTED_USER) == null) {
            threadContext.putTransient(INJECTED_USER, user);
            log.debug("{}, InjectSecurity - inject roles: {}", Thread.currentThread().getName(), id);
        } else {
            log.error("{}, InjectSecurity- most likely thread context corruption : {}", Thread.currentThread().getName(), id);
        }
    }

    /**
     * Injects roles. Comma separated roles.
     * @param roles
     */
    public void injectRoles(final List<String> roles) {

        if ((roles == null) || (roles.size() == 0)) {
            return;
        }

        final String rolesStr = String.join(",", roles);
        String injectStr = "plugin|" + rolesStr;
        if (threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES) == null) {
            threadContext.putTransient(OPENSEARCH_SECURITY_INJECTED_ROLES, injectStr);
            log.debug("{}, InjectSecurity - inject roles: {}", Thread.currentThread().getName(), id);
        } else {
            log.error("{}, InjectSecurity- most likely thread context corruption : {}", Thread.currentThread().getName(), id);
        }
    }

    /**
     * Allows one to set the property in threadContext if possible to the value provided. If not possible returns false.
     * @param property
     * @param value
     * @return boolean
     */
    public boolean injectProperty(final String property, final Object value) {
        if (Strings.isNullOrEmpty(property) || value == null || threadContext.getTransient(property) != null) {
            log.debug("{}, InjectSecurity - cannot inject property: {}", Thread.currentThread().getName(), id);
            return false;
        } else {
            threadContext.putTransient(property, value);
            log.debug("{}, InjectSecurity - inject property: {}", Thread.currentThread().getName(), id);
            return true;
        }
    }

    @Override
    public void close() {
        if (ctx != null) {
            ctx.close();
            log.trace("{}, InjectSecurity close : {}", Thread.currentThread().getName(), id);
        }
    }
}
