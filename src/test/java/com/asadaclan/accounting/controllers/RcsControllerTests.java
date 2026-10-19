package com.asadaclan.accounting.controllers;

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

import com.asadaclan.accounting.entities.Business;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.exceptions.UnregisteredPhoneNumberException;
import com.asadaclan.accounting.services.BusinessAuthorizationService;
import com.asadaclan.accounting.services.InboundMessageService;
import com.asadaclan.accounting.services.MessageDelegationService;
import com.asadaclan.accounting.services.ParserDelegationService;
import com.asadaclan.accounting.services.ReportService;

@ExtendWith(MockitoExtension.class)
class RcsControllerTests {
    private static final String PHONE_NUMBER = "+15551234567";
    private static final String RCS_ADDRESS = "rcs:" + PHONE_NUMBER;
    private static final Business BUSINESS = new Business(
            PHONE_NUMBER, "Example Studio", "owner@example.com");

    @Mock
    private MessageDelegationService messageDelegationService;

    @Mock
    private ParserDelegationService parserDelegationService;

    @Mock
    private BusinessAuthorizationService businessAuthorizationService;

    @Mock
    private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InboundMessageService inboundMessageService = new InboundMessageService(
                messageDelegationService, parserDelegationService, businessAuthorizationService, reportService);
        RcsController controller = new RcsController(inboundMessageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SmsExceptionHandler())
                .setMessageConverters(new MappingJackson2XmlHttpMessageConverter())
                .build();
    }

    @Test
    void receivesRcsCommandAndReturnsTheDelegateConfirmation() throws Exception {
        Transaction transaction = new Transaction(new BigDecimal("250.00"), new BigDecimal("50.00"), null,
                LocalDate.of(2026, 10, 18));
        when(businessAuthorizationService.requireBusiness(PHONE_NUMBER)).thenReturn(BUSINESS);
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Transaction), eq(PHONE_NUMBER)))
                .thenReturn(transaction);
        when(messageDelegationService.delegate(transaction)).thenReturn("transactionId: 3f1a8c2e");

        mockMvc.perform(post("/rcs")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", RCS_ADDRESS)
                        .param("Body", "tattoo, 250, 50"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/Response/Message").string("transactionId: 3f1a8c2e"));

        verify(businessAuthorizationService).requireBusiness(PHONE_NUMBER);
    }

    @Test
    void preservesTrailingEmptyFields() throws Exception {
        Transaction transaction = new Transaction(new BigDecimal("250.00"), new BigDecimal("50.00"), null,
                LocalDate.of(2026, 10, 18));
        when(businessAuthorizationService.requireBusiness(PHONE_NUMBER)).thenReturn(BUSINESS);
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Transaction), eq(PHONE_NUMBER)))
                .thenReturn(transaction);
        when(messageDelegationService.delegate(transaction)).thenReturn("transactionId: 3f1a8c2e");

        mockMvc.perform(post("/rcs")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", RCS_ADDRESS)
                        .param("Body", "tattoo,250,50,"))
                .andExpect(status().isOk());

        verify(parserDelegationService).delegate(
                eq(List.of("tattoo", "250", "50", "")), eq(MessageAction.Transaction), eq(PHONE_NUMBER));
    }

    @Test
    void blankMessageReturnsTwimlWithoutDispatching() throws Exception {
        when(businessAuthorizationService.requireBusiness(PHONE_NUMBER)).thenReturn(BUSINESS);

        mockMvc.perform(post("/rcs")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", RCS_ADDRESS)
                        .param("Body", "   "))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Message must not be blank"));

        verifyNoInteractions(parserDelegationService, messageDelegationService);
    }

    @Test
    void parserFailureReturnsTheParserMessage() throws Exception {
        when(businessAuthorizationService.requireBusiness(PHONE_NUMBER)).thenReturn(BUSINESS);
        when(parserDelegationService.delegate(anyList(), eq(MessageAction.Expense), eq(PHONE_NUMBER)))
                .thenThrow(new CommandParseException(
                        ParseErrorCode.MISSING_REQUIRED_FIELD, 1, "cost", "Required field is missing: cost"));

        mockMvc.perform(post("/rcs")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", RCS_ADDRESS)
                        .param("Body", "expense"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Required field is missing: cost"));

        verifyNoInteractions(messageDelegationService);
    }

    @Test
    void unregisteredSenderIsRejectedBeforeParsing() throws Exception {
        when(businessAuthorizationService.requireBusiness(PHONE_NUMBER))
                .thenThrow(new UnregisteredPhoneNumberException());

        mockMvc.perform(post("/rcs")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("From", RCS_ADDRESS)
                        .param("Body", "tattoo, 250, 50"))
                .andExpect(status().isOk())
                .andExpect(xpath("/Response/Message").string("Phone number is not registered"));

        verify(parserDelegationService, never()).delegate(anyList(), eq(MessageAction.Transaction), eq(PHONE_NUMBER));
        verifyNoInteractions(messageDelegationService);
    }
}
