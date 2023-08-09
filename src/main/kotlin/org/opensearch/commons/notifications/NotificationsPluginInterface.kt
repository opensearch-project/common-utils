/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications

import org.opensearch.action.ActionListener
import org.opensearch.action.ActionResponse
import org.opensearch.client.node.NodeClient
import org.opensearch.common.io.stream.Writeable
import org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetChannelListRequest
import org.opensearch.commons.notifications.action.GetChannelListResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetPluginFeaturesRequest
import org.opensearch.commons.notifications.action.GetPluginFeaturesResponse
import org.opensearch.commons.notifications.action.LegacyPublishNotificationRequest
import org.opensearch.commons.notifications.action.LegacyPublishNotificationResponse
import org.opensearch.commons.notifications.action.NotificationsActions.CREATE_NOTIFICATION_CONFIG_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.DELETE_NOTIFICATION_CONFIG_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.GET_CHANNEL_LIST_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.GET_NOTIFICATION_CONFIG_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.GET_PLUGIN_FEATURES_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.LEGACY_PUBLISH_NOTIFICATION_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.SEND_NOTIFICATION_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.utils.SecureClientWrapper
import org.opensearch.commons.utils.recreateObject

/**
 * All the transport action plugin interfaces for the Notification plugin
 */
object NotificationsPluginInterface {

    /**
     * Create notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun createNotificationConfig(
        client: NodeClient,
        request: CreateNotificationConfigRequest,
        listener: ActionListener<CreateNotificationConfigResponse>
    ) {
        client.execute(
            CREATE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response -> recreateObject(response) { CreateNotificationConfigResponse(it) } }
        )
    }

    /**
     * Update notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun updateNotificationConfig(
        client: NodeClient,
        request: UpdateNotificationConfigRequest,
        listener: ActionListener<UpdateNotificationConfigResponse>
    ) {
        client.execute(
            UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response -> recreateObject(response) { UpdateNotificationConfigResponse(it) } }
        )
    }

    /**
     * Delete notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun deleteNotificationConfig(
        client: NodeClient,
        request: DeleteNotificationConfigRequest,
        listener: ActionListener<DeleteNotificationConfigResponse>
    ) {
        client.execute(
            DELETE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response -> recreateObject(response) { DeleteNotificationConfigResponse(it) } }
        )
    }

    /**
     * Get notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getNotificationConfig(
        client: NodeClient,
        request: GetNotificationConfigRequest,
        listener: ActionListener<GetNotificationConfigResponse>
    ) {
        client.execute(
            GET_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response -> recreateObject(response) { GetNotificationConfigResponse(it) } }
        )
    }

    /**
     * Get notification plugin features.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getPluginFeatures(
        client: NodeClient,
        request: GetPluginFeaturesRequest,
        listener: ActionListener<GetPluginFeaturesResponse>
    ) {
        client.execute(
            GET_PLUGIN_FEATURES_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response -> recreateObject(response) { GetPluginFeaturesResponse(it) } }
        )
    }

    /**
     * Get notification channel configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getChannelList(
        client: NodeClient,
        request: GetChannelListRequest,
        listener: ActionListener<GetChannelListResponse>
    ) {
        client.execute(
            GET_CHANNEL_LIST_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response -> recreateObject(response) { GetChannelListResponse(it) } }
        )
    }

    /**
     * Send notification API enabled for a feature. No REST API. Internal API only for Inter plugin communication.
     * @param client Node client for making transport action
     * @param eventSource The notification event information
     * @param channelMessage The notification message
     * @param channelIds The list of channel ids to send message to.
     * @param listener The listener for getting response
     */
    fun sendNotification(
        client: NodeClient,
        eventSource: EventSource,
        channelMessage: ChannelMessage,
        channelIds: List<String>,
        listener: ActionListener<SendNotificationResponse>
    ) {
        val threadContext: String? =
            client.threadPool().threadContext.getTransient<String>(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
        val wrapper = SecureClientWrapper(client) // Executing request in privileged mode
        wrapper.execute(
            SEND_NOTIFICATION_ACTION_TYPE,
            SendNotificationRequest(eventSource, channelMessage, channelIds, threadContext),
            wrapActionListener(listener) { response -> recreateObject(response) { SendNotificationResponse(it) } }
        )
    }

    /**
     * Publishes a notification API using the legacy notification implementation. No REST API.
     * Internal API only for the Alerting and Index Management plugin, other consumers should use [sendNotification].
     * @param client Node client for making transport action
     * @param request The legacy publish notification request
     * @param listener The listener for getting response
     */
    fun publishLegacyNotification(
        client: NodeClient,
        request: LegacyPublishNotificationRequest,
        listener: ActionListener<LegacyPublishNotificationResponse>
    ) {
        client.execute(
            LEGACY_PUBLISH_NOTIFICATION_ACTION_TYPE,
            request,
            wrapActionListener(listener) { response -> recreateObject(response) { LegacyPublishNotificationResponse(it) } }
        )
    }

    /**
     * Wrap action listener on concrete response class by a new created one on ActionResponse.
     * This is required because the response may be loaded by different classloader across plugins.
     * The onResponse(ActionResponse) avoids type cast exception and give a chance to recreate
     * the response object.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <Response : BaseResponse> wrapActionListener(
        listener: ActionListener<Response>,
        recreate: (Writeable) -> Response
    ): ActionListener<Response> {
        return object : ActionListener<ActionResponse> {
            override fun onResponse(response: ActionResponse) {
                val recreated = response as? Response ?: recreate(response)
                listener.onResponse(recreated)
            }

            override fun onFailure(exception: java.lang.Exception) {
                listener.onFailure(exception)
            }
        } as ActionListener<Response>
    }
}
