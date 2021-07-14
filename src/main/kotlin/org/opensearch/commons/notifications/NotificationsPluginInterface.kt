/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package org.opensearch.commons.notifications

import org.opensearch.action.ActionListener
import org.opensearch.action.ActionResponse
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetFeatureChannelListRequest
import org.opensearch.commons.notifications.action.GetFeatureChannelListResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetNotificationEventRequest
import org.opensearch.commons.notifications.action.GetNotificationEventResponse
import org.opensearch.commons.notifications.action.GetPluginFeaturesRequest
import org.opensearch.commons.notifications.action.GetPluginFeaturesResponse
import org.opensearch.commons.notifications.action.NotificationsActions.CREATE_NOTIFICATION_CONFIG_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.DELETE_NOTIFICATION_CONFIG_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.GET_FEATURE_CHANNEL_LIST_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.GET_NOTIFICATION_CONFIG_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.GET_NOTIFICATION_EVENT_ACTION_TYPE
import org.opensearch.commons.notifications.action.NotificationsActions.GET_PLUGIN_FEATURES_ACTION_TYPE
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
    @Suppress("UNCHECKED_CAST")
    fun createNotificationConfig(
        client: NodeClient,
        request: CreateNotificationConfigRequest,
        listener: ActionListener<CreateNotificationConfigResponse>
    ) {
        client.execute(
            CREATE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? CreateNotificationConfigResponse ?:
                    recreateObject(response) { CreateNotificationConfigResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<CreateNotificationConfigResponse>
        )
    }

    /**
     * Update notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    @Suppress("UNCHECKED_CAST")
    fun updateNotificationConfig(
        client: NodeClient,
        request: UpdateNotificationConfigRequest,
        listener: ActionListener<UpdateNotificationConfigResponse>
    ) {
        client.execute(
            UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? UpdateNotificationConfigResponse ?:
                    recreateObject(response) { UpdateNotificationConfigResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<UpdateNotificationConfigResponse>
        )
    }

    /**
     * Delete notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    @Suppress("UNCHECKED_CAST")
    fun deleteNotificationConfig(
        client: NodeClient,
        request: DeleteNotificationConfigRequest,
        listener: ActionListener<DeleteNotificationConfigResponse>
    ) {
        client.execute(
            DELETE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? DeleteNotificationConfigResponse ?:
                    recreateObject(response) { DeleteNotificationConfigResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<DeleteNotificationConfigResponse>
        )
    }

    /**
     * Get notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    @Suppress("UNCHECKED_CAST")
    fun getNotificationConfig(
        client: NodeClient,
        request: GetNotificationConfigRequest,
        listener: ActionListener<GetNotificationConfigResponse>
    ) {
        client.execute(
            GET_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? GetNotificationConfigResponse ?:
                    recreateObject(response) { GetNotificationConfigResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<GetNotificationConfigResponse>
        )
    }

    /**
     * Get notification events.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    @Suppress("UNCHECKED_CAST")
    fun getNotificationEvent(
        client: NodeClient,
        request: GetNotificationEventRequest,
        listener: ActionListener<GetNotificationEventResponse>
    ) {
        client.execute(
            GET_NOTIFICATION_EVENT_ACTION_TYPE,
            request,
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? GetNotificationEventResponse ?:
                    recreateObject(response) { GetNotificationEventResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<GetNotificationEventResponse>
        )
    }

    /**
     * Get notification plugin features.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    @Suppress("UNCHECKED_CAST")
    fun getPluginFeatures(
        client: NodeClient,
        request: GetPluginFeaturesRequest,
        listener: ActionListener<GetPluginFeaturesResponse>
    ) {
        client.execute(
            GET_PLUGIN_FEATURES_ACTION_TYPE,
            request,
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? GetPluginFeaturesResponse ?:
                    recreateObject(response) { GetPluginFeaturesResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<GetPluginFeaturesResponse>
        )
    }

    /**
     * Get notification channel configuration enabled for a feature.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    @Suppress("UNCHECKED_CAST")
    fun getFeatureChannelList(
        client: NodeClient,
        request: GetFeatureChannelListRequest,
        listener: ActionListener<GetFeatureChannelListResponse>
    ) {
        client.execute(
            GET_FEATURE_CHANNEL_LIST_ACTION_TYPE,
            request,
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? GetFeatureChannelListResponse ?:
                    recreateObject(response) { GetFeatureChannelListResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<GetFeatureChannelListResponse>
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
    @Suppress("UNCHECKED_CAST")
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
            object : ActionListener<ActionResponse> {
                override fun onResponse(response: ActionResponse) {
                    val recreated = response as? SendNotificationResponse ?:
                    recreateObject(response) { SendNotificationResponse(it) }
                    listener.onResponse(recreated)
                }
                override fun onFailure(exception: java.lang.Exception) {
                    listener.onFailure(exception)
                }
            } as ActionListener<SendNotificationResponse>
        )
    }
}
