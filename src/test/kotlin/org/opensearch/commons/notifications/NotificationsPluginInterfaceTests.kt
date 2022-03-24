/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionListener
import org.opensearch.action.ActionType
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.destination.response.LegacyDestinationResponse
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetChannelListRequest
import org.opensearch.commons.notifications.action.GetChannelListResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetNotificationEventRequest
import org.opensearch.commons.notifications.action.GetNotificationEventResponse
import org.opensearch.commons.notifications.action.GetPluginFeaturesRequest
import org.opensearch.commons.notifications.action.GetPluginFeaturesResponse
import org.opensearch.commons.notifications.action.LegacyPublishNotificationRequest
import org.opensearch.commons.notifications.action.LegacyPublishNotificationResponse
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.Channel
import org.opensearch.commons.notifications.model.ChannelList
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.NotificationEventInfo
import org.opensearch.commons.notifications.model.NotificationEventSearchResult
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.rest.RestStatus
import java.time.Instant

@Suppress("UNCHECKED_CAST")
@ExtendWith(MockitoExtension::class)
internal class NotificationsPluginInterfaceTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: NodeClient

    @Test
    fun createNotificationConfig() {
        val request = mock(CreateNotificationConfigRequest::class.java)
        val response = CreateNotificationConfigResponse("configId")
        val listener: ActionListener<CreateNotificationConfigResponse> =
            mock(ActionListener::class.java) as ActionListener<CreateNotificationConfigResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<CreateNotificationConfigResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.createNotificationConfig(client, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun updateNotificationConfig() {
        val request = mock(UpdateNotificationConfigRequest::class.java)
        val response = UpdateNotificationConfigResponse("configId")
        val listener: ActionListener<UpdateNotificationConfigResponse> =
            mock(ActionListener::class.java) as ActionListener<UpdateNotificationConfigResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<UpdateNotificationConfigResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.updateNotificationConfig(client, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun deleteNotificationConfig() {
        val request = mock(DeleteNotificationConfigRequest::class.java)
        val response = DeleteNotificationConfigResponse(mapOf(Pair("sample_config_id", RestStatus.OK)))
        val listener: ActionListener<DeleteNotificationConfigResponse> =
            mock(ActionListener::class.java) as ActionListener<DeleteNotificationConfigResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<DeleteNotificationConfigResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.deleteNotificationConfig(client, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun getNotificationConfig() {
        val request = mock(GetNotificationConfigRequest::class.java)
        val response = mockGetNotificationConfigResponse()
        val listener: ActionListener<GetNotificationConfigResponse> =
            mock(ActionListener::class.java) as ActionListener<GetNotificationConfigResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetNotificationConfigResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getNotificationConfig(client, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun getNotificationEvent() {
        val request = mock(GetNotificationEventRequest::class.java)
        val response = mockGetNotificationEventResponse()
        val listener: ActionListener<GetNotificationEventResponse> =
            mock(ActionListener::class.java) as ActionListener<GetNotificationEventResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetNotificationEventResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getNotificationEvent(client, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun getPluginFeatures() {
        val request = mock(GetPluginFeaturesRequest::class.java)
        val response = GetPluginFeaturesResponse(
            listOf("config_type_1", "config_type_2", "config_type_3"),
            mapOf(
                Pair("FeatureKey1", "FeatureValue1"),
                Pair("FeatureKey2", "FeatureValue2"),
                Pair("FeatureKey3", "FeatureValue3")
            )
        )
        val listener: ActionListener<GetPluginFeaturesResponse> =
            mock(ActionListener::class.java) as ActionListener<GetPluginFeaturesResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetPluginFeaturesResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getPluginFeatures(client, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun getChannelList() {
        val sampleConfig = Channel(
            "config_id",
            "name",
            "description",
            ConfigType.SLACK
        )

        val request = mock(GetChannelListRequest::class.java)
        val response = GetChannelListResponse(ChannelList(sampleConfig))
        val listener: ActionListener<GetChannelListResponse> =
            mock(ActionListener::class.java) as ActionListener<GetChannelListResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetChannelListResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getChannelList(client, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun sendNotification() {
        val notificationInfo = EventSource(
            "title",
            "reference_id",
            SeverityType.HIGH,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
            "<b>htmlDescription</b>",
            null
        )

        val response = SendNotificationResponse("configId")
        val listener: ActionListener<SendNotificationResponse> =
            mock(ActionListener::class.java) as ActionListener<SendNotificationResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<SendNotificationResponse>)
                .onResponse(response)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.sendNotification(
            client, notificationInfo, channelMessage, listOf("channelId1", "channelId2"), listener
        )
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun publishLegacyNotification() {
        val request = mock(LegacyPublishNotificationRequest::class.java)
        val res = LegacyPublishNotificationResponse(LegacyDestinationResponse.Builder().withStatusCode(200).withResponseContent("Nice!").build())
        val l: ActionListener<LegacyPublishNotificationResponse> =
            mock(ActionListener::class.java) as ActionListener<LegacyPublishNotificationResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<LegacyPublishNotificationResponse>)
                .onResponse(res)
        }.whenever(client).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.publishLegacyNotification(client, request, l)
        verify(l, times(1)).onResponse(eq(res))
    }

    private fun mockGetNotificationConfigResponse(): GetNotificationConfigResponse {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
            Instant.now(),
            Instant.now(),
            sampleConfig
        )
        return GetNotificationConfigResponse(NotificationConfigSearchResult(configInfo))
    }

    private fun mockGetNotificationEventResponse(): GetNotificationEventResponse {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val eventInfo = NotificationEventInfo(
            "event_id",
            Instant.now(),
            Instant.now(),
            sampleEvent
        )
        return GetNotificationEventResponse(NotificationEventSearchResult(eventInfo))
    }
}
