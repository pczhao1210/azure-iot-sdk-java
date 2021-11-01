// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.ReportedPropertiesParameters;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.DeviceClientLonghaulTestAddOn;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@NoArgsConstructor
public class DeviceClientTwinAddOn implements DeviceClientLonghaulTestAddOn
{
    @Override
    public void setupClientBeforeOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {

    }

    @Override
    public void setupClientAfterOpen(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        final CountDownLatch twinStartedLatch = new CountDownLatch(1);
        clientUnderTest.startDeviceTwin(
            (responseStatus, callbackContext) ->
            {
                log.info("EventCallback for twin: {}", responseStatus);
                twinStartedLatch.countDown();
            },
            null,
            (TwinPropertyCallBack) (property, context) ->
            {
                log.info("Property callback for property: {} {}", property.getKey(), property.getValue());
            },
            null);
    }

    @Override
    public void performPeriodicTestableAction(DeviceClientManager clientUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception
    {
        Set<Property> reportedProperties = new HashSet<>();
        String value = new Date().toString();
        reportedProperties.add(new Property("key", value));
        clientUnderTest.sendReportedProperties(reportedProperties);

        DeviceTwin twinClient = new DeviceTwin(Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME));
        DeviceTwinDevice deviceTwinDevice = new DeviceTwinDevice(clientUnderTest.getConfig().getDeviceId());
        twinClient.getTwin(deviceTwinDevice);
        Set<Pair> desiredProperties = deviceTwinDevice.getDesiredProperties();
        desiredProperties.add(new Pair(UUID.randomUUID().toString(), new Date().toString()));
    }

    @Override
    public void performPeriodicStatusReport(TestParameters testParameters)
    {
    }

    @Override
    public boolean validateExpectations(TestParameters testParameters)
    {
        return true;
    }
}
