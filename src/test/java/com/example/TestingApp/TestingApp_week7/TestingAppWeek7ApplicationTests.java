package com.example.TestingApp.TestingApp_week7;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class TestingAppWeek7ApplicationTests {

	@BeforeEach
	void beforeEach(){
		log.info("beforeEach");
	}


	@AfterEach
	void afterEach(){
		log.info("afterEach");
	}

	@AfterAll
	static void afterAll(){
		log.info("after All");
	}


	@BeforeAll
	static void beforeAll(){
		log.info("beforeAll");
	}


	@Test
//	@Disabled
	void testOne() {
		int a = 5;
		int b = 3;
		int result=addNumbers(a,b);
//		Assertions.assertEquals(8,result); -- old
//
//		assertThat(result)
//				.isEqualTo(8)
//				.isCloseTo(9, Offset.offset(1));

       assertThat("apple")
			   .isEqualTo("apple")
			   .startsWith("app")
			   .endsWith("le")
			   .hasSize(5);
	}

	@Test
	//@DisplayName("saifcase")
	void testingDivideTwoNumbers_withZero_ArithemticException() {
		int a = 5;
		int b = 0;

		assertThatThrownBy(() ->divideTwoNumbers(a,b))
				.isInstanceOf(ArithmeticException.class)
				.hasMessage("Tried to divide by zero");
	}


	int addNumbers(int a, int b){
		return a+b;
	}

	double divideTwoNumbers(int a, int b){
		try{
			return a/b;
		}catch (ArithmeticException e){
			log.info("Arithmetic Exception Occurres" + e.getLocalizedMessage());
			throw new ArithmeticException("Tried to divide by zero");
		}
	}
}
