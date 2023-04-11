package com.vmware.data.solutions.gemfire.sink;

import com.vmware.data.services.gemfire.serialization.PDX;
import org.apache.geode.examples.security.ExampleSecurityManager;
import org.apache.geode.pdx.PdxInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GemFireSinkConsumerTest {

    @Mock
    private Map<String, PdxInstance> region;

    @Mock
    private PDX pdx;

    @Mock
    private PdxInstance pdxInstance;

    private String classType = Object.class.getName();

    private String valuePdxClassName = ExampleSecurityManager.User.class.getName();
    private String keyFieldExpression = "id";
    private GemFireSinkConsumer subject;


    @BeforeEach
    void setUp() {

        subject = new GemFireSinkConsumer(region,
                pdx,
                keyFieldExpression,
                valuePdxClassName
                );
    }

    @DisplayName("Given json WHEN: accept then: save to region")
    @Test
    void accept() {

        String json = """
                {}
                """;

        when(pdx.addTypeToJson(anyString(),anyString())).thenReturn(json);
        when(pdx.fromJSON(anyString())).thenReturn(pdxInstance);

        subject.accept(json);

        verify(region).put(anyString(),any(PdxInstance.class));
    }
}