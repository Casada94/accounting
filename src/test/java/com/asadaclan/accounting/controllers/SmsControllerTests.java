package com.asadaclan.accounting.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.asadaclan.accounting.models.ReceivedMessage;
import com.asadaclan.accounting.reporting.FinancialReportGenerator;
import com.asadaclan.accounting.service.ProcessExecutionService;
import com.asadaclan.accounting.service.QueryService;

@ExtendWith(MockitoExtension.class)
class SmsControllerTests {

    @Mock
    private FinancialReportGenerator financialReportGenerator;

    @Mock
    private QueryService queryService;

    @Mock
    private ProcessExecutionService processExecutionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SmsController controller = new SmsController(financialReportGenerator, queryService, processExecutionService,
                List.of("MM/dd/yyyy", "yyyy-MM-dd"));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void receivesTwilioFormPostAtSmsEndpointAndReturnsTwiml() throws Exception {
        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "Transaction,Coffee,4.50"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/Response/Message").string("transactionId: tempId"));
    }

    @Test
    void escapesDynamicResponseTextInTwiml() throws Exception {
        when(queryService.answer(any(ReceivedMessage.class))).thenReturn("Balance < $5 & pending");

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "Query,balance,0"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/Response/Message").string("Balance < $5 & pending"));
    }
}
