package com.raxim.myscoutee.common.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.raxim.myscoutee.common.config.MqttConfig.MqttGateway;
import com.raxim.myscoutee.common.config.properties.MqttProperties;
import com.raxim.myscoutee.profile.service.MessageService;

@Disabled
@DirtiesContext
@ExtendWith({ SpringExtension.class })
@EnableConfigurationProperties(value = MqttProperties.class)
@ContextConfiguration(classes = { JsonConfig.class, MqttProperties.class, MqttConfig.class })
@TestPropertySource("classpath:mqtt_test.properties")
@IntegrationComponentScan
@SpringIntegrationTest
public class MqttConfigTest {

    @MockBean
    private MessageService messageService;

    @Autowired
    private MqttGateway mqttGateway;

    @Test
    public void shouldTestMqttGateWay() {
        // mqttGateway.sendToMqtt("sent");
        System.out.println("testMqttGateWay with running mosquitto!");
    }
}
