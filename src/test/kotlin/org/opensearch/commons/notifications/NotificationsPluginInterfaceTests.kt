package org.opensearch.commons.notifications

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionListener
import org.opensearch.action.ActionType
import org.opensearch.client.node.NodeClient
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
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.FeatureChannel
import org.opensearch.commons.notifications.model.FeatureChannelList
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
import java.util.EnumSet

@Suppress("UNCHECKED_CAST")
@ExtendWith(MockitoExtension::class)
internal class NotificationsPluginInterfaceTests {

    @Mock
    private lateinit var nodeClient: NodeClient

    @Test
    fun createNotificationConfig() {
        val request = mock(CreateNotificationConfigRequest::class.java)
        val response = CreateNotificationConfigResponse("configId")
        val listener: ActionListener<CreateNotificationConfigResponse> =
            mock(ActionListener::class.java) as ActionListener<CreateNotificationConfigResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<CreateNotificationConfigResponse>)
                .onResponse(response)
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.createNotificationConfig(nodeClient, request, listener)
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
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.updateNotificationConfig(nodeClient, request, listener)
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
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.deleteNotificationConfig(nodeClient, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun getNotificationConfig() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            EnumSet.of(Feature.REPORTS),
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
            Instant.now(),
            Instant.now(),
            "tenant",
            sampleConfig
        )

        val request = mock(GetNotificationConfigRequest::class.java)
        val response = GetNotificationConfigResponse(NotificationConfigSearchResult(configInfo))
        val listener: ActionListener<GetNotificationConfigResponse> =
            mock(ActionListener::class.java) as ActionListener<GetNotificationConfigResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetNotificationConfigResponse>)
                .onResponse(response)
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getNotificationConfig(nodeClient, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun getNotificationEvent() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.ALERTING,
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
            "tenant",
            sampleEvent
        )

        val request = mock(GetNotificationEventRequest::class.java)
        val response = GetNotificationEventResponse(NotificationEventSearchResult(eventInfo))
        val listener: ActionListener<GetNotificationEventResponse> =
            mock(ActionListener::class.java) as ActionListener<GetNotificationEventResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetNotificationEventResponse>)
                .onResponse(response)
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getNotificationEvent(nodeClient, request, listener)
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
            ))
        val listener: ActionListener<GetPluginFeaturesResponse> =
            mock(ActionListener::class.java) as ActionListener<GetPluginFeaturesResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetPluginFeaturesResponse>)
                .onResponse(response)
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getPluginFeatures(nodeClient, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun getFeatureChannelList() {
        val sampleConfig = FeatureChannel(
            "config_id",
            "name",
            "description",
            ConfigType.SLACK
        )

        val request = mock(GetFeatureChannelListRequest::class.java)
        val response = GetFeatureChannelListResponse(FeatureChannelList(sampleConfig))
        val listener: ActionListener<GetFeatureChannelListResponse> =
            mock(ActionListener::class.java) as ActionListener<GetFeatureChannelListResponse>

        doAnswer {
            (it.getArgument(2) as ActionListener<GetFeatureChannelListResponse>)
                .onResponse(response)
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.getFeatureChannelList(nodeClient, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun sendNotification() {
    }

}