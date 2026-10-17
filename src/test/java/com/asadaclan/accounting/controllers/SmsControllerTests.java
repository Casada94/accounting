package com.asadaclan.accounting.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.services.MessageDelegationService;
import com.asadaclan.accounting.services.ParserDelegationService;

@ExtendWith(MockitoExtension.class)
class SmsControllerTests {

    @Mock
    private MessageDelegationService messageDelegationService;

    @Mock
    private ParserDelegationService parserDelegationService;

    private SmsController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new SmsController(messageDelegationService, parserDelegationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SmsExceptionHandler())
                .setMessageConverters(new MappingJackson2XmlHttpMessageConverter())
                .build();
    }

    @Test
    void receivesTattooAndReturnsTheDelegateConfirmation() throws Exception {
        Transaction transaction = new Transaction(new BigDecimal("250.00"), new BigDecimal("50.00"), null,
                LocalDate.of(2026, 10, 14));
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Transaction)))
                .thenReturn(transaction);
        when(messageDelegationService.delegate(transaction)).thenReturn("transactionId: 3f1a8c2e");

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "tattoo, 250, 50"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/Response/Message").string("transactionId: 3f1a8c2e"));
    }

    @Test
    void keepsTrailingEmptyFields() throws Exception {
        Transaction transaction = new Transaction(new BigDecimal("250.00"), new BigDecimal("50.00"), null,
                LocalDate.of(2026, 10, 14));
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Transaction)))
                .thenReturn(transaction);
        when(messageDelegationService.delegate(transaction)).thenReturn("transactionId: 3f1a8c2e");

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "tattoo,250,50,"))
                .andExpect(status().isOk());

        verify(parserDelegationService).delegate(
                eq(List.of("tattoo", "250", "50", "")), eq(MessageAction.Transaction));
    }

    @Test
    void blankAndMissingActionReturnErrorWithoutDispatching() throws Exception {
        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "   "))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Message must not be blank"));

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", ","))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Message must not be blank"));

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", ",,"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Message must not be blank"));

        assertEquals("Message must not be blank", controller.receiveSms(null).message());
        verifyNoInteractions(parserDelegationService, messageDelegationService);
    }

    @Test
    void unknownActionReturnsError() throws Exception {
        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "nope, 1"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Unknown action: nope"));

        verify(parserDelegationService, never()).delegate(anyList(), any(MessageAction.class));
        verifyNoInteractions(messageDelegationService);
    }

    @Test
    void parseFailureReturnsTheParserMessage() throws Exception {
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Expense)))
                .thenThrow(new CommandParseException(
                        ParseErrorCode.MISSING_REQUIRED_FIELD, 1, "cost", "Required field is missing: cost"));

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "expense"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Required field is missing: cost"));

        verifyNoInteractions(messageDelegationService);
    }

    @Test
    void helpGoesThroughParserAndMessageDelegation() throws Exception {
        Help help = new Help();
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Help))).thenReturn(help);
        when(messageDelegationService.delegate(help)).thenReturn("Available command syntax");

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("Body", "help, expense"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Available command syntax"));

        verify(parserDelegationService).delegate(
                eq(List.of("help", " expense")), eq(MessageAction.Help));
        verify(messageDelegationService).delegate(help);
    }
}
