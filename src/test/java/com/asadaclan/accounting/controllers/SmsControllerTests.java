package com.asadaclan.accounting.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
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

import com.asadaclan.accounting.entities.Business;
import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.entities.ReportRequest;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.exceptions.UnregisteredPhoneNumberException;
import com.asadaclan.accounting.models.TwilioInboundMessage;
import com.asadaclan.accounting.services.BusinessAuthorizationService;
import com.asadaclan.accounting.services.InboundMessageService;
import com.asadaclan.accounting.services.MessageDelegationService;
import com.asadaclan.accounting.services.ParserDelegationService;
import com.asadaclan.accounting.services.ReportService;

@ExtendWith(MockitoExtension.class)
class SmsControllerTests {
    private static final String AUTHORIZED_PHONE_NUMBER = "+15551234567";
    private static final String BUSINESS_ID = AUTHORIZED_PHONE_NUMBER;
    private static final Business BUSINESS = new Business(
            AUTHORIZED_PHONE_NUMBER, "Example Studio", "owner@example.com");

    @Mock
    private MessageDelegationService messageDelegationService;

    @Mock
    private ParserDelegationService parserDelegationService;

    @Mock
    private BusinessAuthorizationService businessAuthorizationService;

    @Mock
    private ReportService reportService;

    private SmsController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InboundMessageService inboundMessageService = new InboundMessageService(
                messageDelegationService, parserDelegationService, businessAuthorizationService, reportService);
        controller = new SmsController(inboundMessageService);
        lenient().when(businessAuthorizationService.requireBusiness(AUTHORIZED_PHONE_NUMBER))
                .thenReturn(BUSINESS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SmsExceptionHandler())
                .setMessageConverters(new MappingJackson2XmlHttpMessageConverter())
                .build();
    }

    @Test
    void receivesTattooAndReturnsTheDelegateConfirmation() throws Exception {
        Transaction transaction = new Transaction(new BigDecimal("250.00"), new BigDecimal("50.00"), null,
                LocalDate.of(2026, 10, 14));
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Transaction), eq(BUSINESS_ID)))
                .thenReturn(transaction);
        when(messageDelegationService.delegate(transaction)).thenReturn("transactionId: 3f1a8c2e");

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "tattoo, 250, 50"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/Response/Message").string("transactionId: 3f1a8c2e"));

        verify(businessAuthorizationService).requireBusiness(AUTHORIZED_PHONE_NUMBER);
    }

    @Test
    void keepsTrailingEmptyFields() throws Exception {
        Transaction transaction = new Transaction(new BigDecimal("250.00"), new BigDecimal("50.00"), null,
                LocalDate.of(2026, 10, 14));
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Transaction), eq(BUSINESS_ID)))
                .thenReturn(transaction);
        when(messageDelegationService.delegate(transaction)).thenReturn("transactionId: 3f1a8c2e");

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "tattoo,250,50,"))
                .andExpect(status().isOk());

        verify(parserDelegationService).delegate(
                eq(List.of("tattoo", "250", "50", "")), eq(MessageAction.Transaction), eq(BUSINESS_ID));
    }

    @Test
    void blankAndMissingActionReturnErrorWithoutDispatching() throws Exception {
        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "   "))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Message must not be blank"));

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", ","))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Message must not be blank"));

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", ",,"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Message must not be blank"));

        assertEquals("Message must not be blank", controller.receiveSms(inboundMessage(null)).message());
        verifyNoInteractions(parserDelegationService, messageDelegationService);
    }

    @Test
    void unknownActionReturnsError() throws Exception {
        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "nope, 1"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Unknown action: nope"));

        verify(parserDelegationService, never()).delegate(anyList(), any(MessageAction.class), any());
        verifyNoInteractions(messageDelegationService);
    }

    @Test
    void parseFailureReturnsTheParserMessage() throws Exception {
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Expense), eq(BUSINESS_ID)))
                .thenThrow(new CommandParseException(
                        ParseErrorCode.MISSING_REQUIRED_FIELD, 1, "cost", "Required field is missing: cost"));

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "expense"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Required field is missing: cost"));

        verifyNoInteractions(messageDelegationService);
    }

    @Test
    void helpGoesThroughParserAndMessageDelegation() throws Exception {
        Help help = new Help();
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Help), eq(BUSINESS_ID))).thenReturn(help);
        when(messageDelegationService.delegate(help)).thenReturn("Available command syntax");

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "help, expense"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Available command syntax"));

        verify(parserDelegationService).delegate(
                eq(List.of("help", " expense")), eq(MessageAction.Help), eq(BUSINESS_ID));
        verify(messageDelegationService).delegate(help);
    }

    @Test
    void reportsReceiveTheAuthorizedBusinessWhileParsingUsesOnlyItsId() throws Exception {
        ReportRequest report = new ReportRequest(
                null, BUSINESS_ID, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Report), eq(BUSINESS_ID)))
                .thenReturn(report);

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "report, month, 10, 2026"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message")
                        .string("Please check your email for your financial report."));

        verify(parserDelegationService).delegate(anyList(), eq(MessageAction.Report), eq(BUSINESS_ID));
        verify(reportService).generateReport(BUSINESS, report);
    }

    @Test
    void unregisteredSenderIsRejectedBeforeTheMessageIsParsed() throws Exception {
        when(businessAuthorizationService.requireBusiness(AUTHORIZED_PHONE_NUMBER))
                .thenThrow(new UnregisteredPhoneNumberException());

        mockMvc.perform(post("/sms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("MessageSid", "SM1234567890")
                        .param("From", AUTHORIZED_PHONE_NUMBER)
                        .param("Body", "tattoo, 250, 50"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Phone number is not registered"));

        verifyNoInteractions(parserDelegationService, messageDelegationService);
    }

    private static TwilioInboundMessage inboundMessage(String body) {
        return new TwilioInboundMessage(
                "SM1234567890",
                "AC1234567890",
                null,
                AUTHORIZED_PHONE_NUMBER,
                "+15557654321",
                body,
                0,
                1,
                null,
                null);
    }
}
