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
            (it.getArgument(2)as ActionListener<CreateNotificationConfigResponse>)
                    .onResponse(response)
        }.`when`(nodeClient).execute(any(ActionType::class.java), any(), any())

        NotificationsPluginInterface.createNotificationConfig(nodeClient, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    @Test
    fun updateNotificationConfig() {
    }

    @Test
    fun deleteNotificationConfig() {
    }

    @Test
    fun getNotificationConfig() {
    }

    @Test
    fun getNotificationEvent() {
    }

    @Test
    fun getPluginFeatures() {
    }

    @Test
    fun getFeatureChannelList() {
    }

    @Test
    fun sendNotification() {
    }

}