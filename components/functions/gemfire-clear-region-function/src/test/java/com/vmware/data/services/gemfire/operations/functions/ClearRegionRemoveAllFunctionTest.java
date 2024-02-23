package com.vmware.data.services.gemfire.operations.functions;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.execute.ResultSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearRegionRemoveAllFunctionTest {

    private ClearRegionRemoveAllFunction subject;

    @Mock
    private RegionFunctionContext<String> context;

    @Mock
    private ResultSender resultSender;

    @Mock
    private Region<Object, Object> region;


    @BeforeEach
    void setUp() {
        subject = new ClearRegionRemoveAllFunction();
    }

    @Test
    void apply() {
        when(context.getDataSet()).thenReturn(region);
        when(context.getResultSender()).thenReturn(resultSender);

        subject.execute((FunctionContext) context);

        verify(region,atLeastOnce()).removeAll(any());
        verify(resultSender).lastResult(any());
    }

    @Test
    void getId() {
        assertThat(subject.getId()).isEqualTo("ClearRegionRemoveAllFunction");
    }
}