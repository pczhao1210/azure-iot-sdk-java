/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * IotHub specific message container
 */
public class Message
{
    private final static String deliveryAcknowledgementPropertyName = "iothub-ack";

    /**
    * [Required for two way requests] Used to correlate two-way communication.
    * Format: A case-sensitive string (up to 128 char long) of ASCII 7-bit alphanumeric chars
    * plus {'-', ':', '/', '\', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
    * Non-alphanumeric characters are from URN RFC.
    **/
    @Getter
    @Setter
    private String messageId;

    /**
    * Destination of the message
    **/
    @Getter
    private String to;

    public void setTo(String deviceId)
    {
        this.to = "/devices/" + deviceId + "/messages/devicebound";
    }

    /**
    * Expiry time in UTC Interpreted by hub on C2D messages. Ignored in other cases.
    **/
    @Getter
    private Date expiryTimeUtc;

    /**
    * Used by receiver to Abandon, Reject or Complete the message
    **/
    @SuppressWarnings("unused") // Used in a getter, may be used for expansion
    @Getter
    private String lockToken;

    /**
    * Used in message responses and feedback
    **/
    @Getter
    @Setter
    private String correlationId;

    /**
    * [Required in feedback messages] Used to specify the origin of messages generated by device hub.
    * Possible value: "{hub name}/"
    **/
    @Getter
    @Setter
    private String userId;

    /**
    * [Required in feedback messages] Specifies the different acknowledgement levels for message delivery.
    **/
    @Getter
    private DeliveryAcknowledgement deliveryAcknowledgement;

    /**
    * A bag of user-defined properties. Value can only be strings. These do not contain system properties.
    **/
    private final Map<String,String> properties;

    /**
    * The message body
    **/
    private byte[] body;

    /**
    * Basic constructor
    **/
    public Message()
    {
        this.properties = new HashMap<>(1);
        this.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);
        this.correlationId = UUID.randomUUID().toString();
    }

    /**
    * stream: a stream containing the body of the message
     * @param stream The stream containing the message body
    **/
    public Message(ByteArrayInputStream stream)
    {
        this();
        if (stream != null)
        {
            this.body = stream.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
    * byteArray: a byte array containing the body of the message
     * @param byteArray The byte array containing the message body
    **/
    public Message(byte[] byteArray)
    {
        this();
        this.body = byteArray;
    }

    /**
     *
     * @param string - a string containing the body of the message.
     * Important: If a string is passed, the HttpBatch.SerializeAsString is set to true,
     * and the internal byte representation is serialized as UTF-8,
     * with HttpBatch.Encoding set to UTF-8.
     */
    public Message(String string)
    {
        this();
        this.body = string.getBytes(StandardCharsets.UTF_8);
    }

    /**
    * The stream content of the body.
     * @return The ByteArrayOutputStream object containing the message body
    **/
    public ByteArrayOutputStream getBodyStream()
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(this.body.length);
        byteArrayOutputStream.write(this.body, 0, this.body.length);
        return byteArrayOutputStream;
    }

    /**
    * The byte content of the body.
     * @return The byte array of the message body
    **/
    public byte[] getBytes()
    {
        return this.body;
    }

    /**
     * @param deliveryAcknowledgement the delivery acknowledgement to set
     */
    public final void setDeliveryAcknowledgement(DeliveryAcknowledgement deliveryAcknowledgement)
    {
        this.deliveryAcknowledgement = deliveryAcknowledgement;
        this.properties.put(deliveryAcknowledgementPropertyName, deliveryAcknowledgement.name().toLowerCase());
    }

    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            this.properties.put(entry.getKey(), entry.getValue());
        }
    }

    public void clearCustomProperties()
    {
        this.properties.clear();
        setDeliveryAcknowledgement(this.deliveryAcknowledgement);
    }
}
