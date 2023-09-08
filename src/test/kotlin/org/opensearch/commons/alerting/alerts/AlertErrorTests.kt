package org.opensearch.commons.alerting.alerts

import org.junit.Assert
import org.junit.jupiter.api.Test
import java.time.Instant

class AlertErrorTests {

    @Test
    fun `test alertError obfuscates IP addresses in message`() {
        val message =
            "AlertingException[[5f32db4e2a4fa94f6778cb895dae7a24][10.212.77.91:9300][indices:admin/create]]; " +
                "nested: Exception[org.opensearch.transport.RemoteTransportException: [5f32db4e2a4fa94f6778cb895dae7a24][10.212.77.91:9300]" +
                "[indices:admin/create]];; java.lang.Exception: org.opensearch.transport.RemoteTransportException: [5f32db4e2a4fa94f6778cb895" +
                "dae7a24][10.212.77.91:9300][indices:admin/create]"
        val alertError = AlertError(Instant.now(), message = message)
        Assert.assertEquals(
            alertError.message,
            "AlertingException[[5f32db4e2a4fa94f6778cb895dae7a24][x.x.x.x:9300][indices:admin/create]]; " +
                "nested: Exception[org.opensearch.transport.RemoteTransportException: [5f32db4e2a4fa94f6778cb895dae7a24][x.x.x.x:9300]" +
                "[indices:admin/create]];; java.lang.Exception: org.opensearch.transport.RemoteTransportException: " +
                "[5f32db4e2a4fa94f6778cb895dae7a24][x.x.x.x:9300][indices:admin/create]"
        )
    }
}
