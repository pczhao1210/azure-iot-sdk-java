// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;


/** Sends a number of event messages to an IoT Hub. */
public class SendEvent
{
    private static String connectionString = "";

    public static void main(String[] args)
        throws IOException, URISyntaxException, IotHubException, InterruptedException
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            System.out.println("Set the hub level connection string before running the sample");
            return;
        }

        RegistryManager rm = new RegistryManager(connectionString);
        Device x509Device = Device.createDevice(UUID.randomUUID().toString(), AuthenticationType.SELF_SIGNED);
        X509CertificateGenerator x509CertificateGenerator = new X509CertificateGenerator();

        String thumbprint = x509CertificateGenerator.getX509Thumbprint();
        String pub = x509CertificateGenerator.getPublicCertificate();
        String priv = x509CertificateGenerator.getPrivateKey();

        x509Device.setThumbprintFinal(thumbprint, thumbprint);
        rm.addDevice(x509Device);

        System.out.println("Created device " + x509Device.getDeviceId() + " to repro the issue");
        Thread.sleep(2000);

        // Issue only repros with x509 auth + AMQPS_WS. These certs are self-signed.
        DeviceClient client = new DeviceClient(rm.getDeviceConnectionString(x509Device), AMQPS_WS, pub, false, priv, false);
        client.open(); // Service unexpectedly closes the connection here sometimes
        client.closeNow();
    }
}
