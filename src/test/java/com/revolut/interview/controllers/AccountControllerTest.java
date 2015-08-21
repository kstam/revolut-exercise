package com.revolut.interview.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.interview.config.SpringRootConfig;
import com.revolut.interview.model.Account;
import com.revolut.interview.repos.AccountRepo;
import com.revolut.interview.repos.DataAccessException;
import com.revolut.interview.utils.TestConstants;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { SpringRootConfig.class })
public class AccountControllerTest extends AbstractTestNGSpringContextTests {

    public static final Account ACCOUNT1 = new Account(1, TestConstants.EUR_5);
    public static final Account ACCOUNT2 = new Account(2, TestConstants.EUR_100);

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeClass
    public void setup() throws DataAccessException {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        accountRepo.insert(ACCOUNT1);
        accountRepo.insert(ACCOUNT2);
    }

    @Test
    public void testCanGetAnExistingAccount() throws Exception {
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.balance.value", is(5)))
                .andExpect(jsonPath("$.balance.currency", is("EUR")));
    }

    @Test
    public void testReturnsErrorStatusWhenAccountDoesNotExist() throws Exception {
        mockMvc.perform(get("/accounts/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreatesANewAccountAccordingly() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String accountJson = om.writeValueAsString(new Account(TestConstants.USD_20));
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("accounts/3")))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.balance.value", is(20)))
                .andExpect(jsonPath("$.balance.currency", is("USD")));
    }

    @Test
    public void testCreateRetrurnsProperErrorOnFailure() throws Exception {
        String accountJson = "{\"balance\":{}}";
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateRetrurnsProperErrorWhenCurrencyIsWrong() throws Exception {
        String accountJson = "{\"balance\":{\"value\": 10, \"currency\":\"XXA\"}}";
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isBadRequest());
    }
}
