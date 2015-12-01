package com.wang.web.unit;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;

@DirtiesContext
@EnableTransactionManagement
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionDbUnitTestExecutionListener.class })
public abstract class DbUnitTestBase {}
