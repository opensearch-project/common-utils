package com.amazon.opendistroforelasticsearch.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;

import java.util.List;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.INJECTED_USER;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_INJECTED_ROLES;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_USE_INJECTED_USER_DEFAULT;


/**
 * For background jobs usage only. Roles injection can be done using transport layer only.
 * You can't inject roles using REST api.
 *
 * Java example Usage:
 *
 *      try (InjectSecurity injectSecurity = new InjectSecurity(id, settings, client.threadPool().getThreadContext())) {
 *
 *          //add roles to be injected from the configuration.
 *          injectSecurity.inject("user, Arrays.toList("role_1,role_2"));
 *
 *          //Elasticsearch calls that needs to executed in security context.
 *
 *          SearchRequestBuilder searchRequestBuilder = client.prepareSearch(monitor.indexpattern);
 *          SearchResponse searchResponse = searchRequestBuilder
 *                             .setFrom(0).setSize(100).setExplain(true).  execute().actionGet();
 *
 *      } catch (final ElasticsearchSecurityException ex){
 *            //handle the security exception
 *      }
 *
 * Kotlin usage with Coroutines:
 *
 *    //You can also use launch, based on usecase.
 *    runBlocking(RolesInjectorContextElement(monitor.id, settings, threadPool.threadContext, monitor.associatedRoles)) {
 *       //Elasticsearch calls that needs to executed in security context.
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

    public InjectSecurity(String id, Settings settings, ThreadContext tc) {
        this.id = id;
        this.settings = settings;
        this.threadContext = tc;

        this.ctx = tc.newStoredContext(true);
        log.trace("{}, InjectSecurity constructor: {}", Thread.currentThread().getName(), id);
    }

    public void inject(final String user, final List<String> roles) {
        boolean injectUser = settings.getAsBoolean(OPENDISTRO_SECURITY_USE_INJECTED_USER_DEFAULT, false);
        if( injectUser)
            injectUser(user);
        else
            injectRoles(roles);
    }

    public void injectUser(final String user) {
        if(Strings.isNullOrEmpty(user)) {
            return;
        }

        if(threadContext.getTransient(INJECTED_USER) == null) {
            threadContext.putTransient(INJECTED_USER, user);
            log.debug("{}, InjectSecurity - inject roles: {}", Thread.currentThread().getName(), id);
        } else {
            log.error("{}, InjectSecurity- most likely thread context corruption : {}", Thread.currentThread().getName(), id);
        }
    }

    public void injectRoles(final List<String> roles) {
        if(roles == null || roles.size() == 0) {
            return;
        }

        String rolesStr = roles.stream().collect(Collectors.joining(","));
        String injectStr = "plugin|"+rolesStr;
        if(threadContext.getTransient(OPENDISTRO_SECURITY_INJECTED_ROLES) == null) {
            threadContext.putTransient(OPENDISTRO_SECURITY_INJECTED_ROLES, injectStr);
            log.debug("{}, InjectSecurity - inject roles: {}", Thread.currentThread().getName(), id);
        } else {
            log.error("{}, InjectSecurity- most likely thread context corruption : {}", Thread.currentThread().getName(), id);
        }
    }

    @Override
    public void close() {
        if(ctx != null) {
            ctx.close();
            log.debug("{}, InjectSecurity close : {}", Thread.currentThread().getName(), id);
        }
    }
}
