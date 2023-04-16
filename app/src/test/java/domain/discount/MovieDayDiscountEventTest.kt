package domain.discount

import domain.payment.PaymentAmount
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

internal class MovieDayDiscountEventTest {

    lateinit var movieDayDiscountEvent: MovieDayDiscountEvent

    @Before
    fun setUp() {
        movieDayDiscountEvent = MovieDayDiscountEvent()
    }

    @Test
    fun `10일인_경우_10%_할인이_적용된다`() {
        val paymentAmount = PaymentAmount(13000)
        val resultDiscountedPaymentAmount = movieDayDiscountEvent.discount(
            paymentAmount,
            LocalDateTime.of(2023, 4, 10, 13, 0)
        )
        val expectedDiscountedPaymentAmount = PaymentAmount(11700)

        Assert.assertEquals(resultDiscountedPaymentAmount, expectedDiscountedPaymentAmount)
    }

    @Test
    fun `20일인_경우_10%_할인이_적용된다`() {
        val paymentAmount = PaymentAmount(13000)
        val resultDiscountedPaymentAmount = movieDayDiscountEvent.discount(
            paymentAmount,
            LocalDateTime.of(2023, 4, 20, 13, 0)
        )
        val expectedDiscountedPaymentAmount = PaymentAmount(11700)

        Assert.assertEquals(resultDiscountedPaymentAmount, expectedDiscountedPaymentAmount)
    }

    @Test
    fun `30일_경우_10%_할인이_적용된다`() {
        val paymentAmount = PaymentAmount(13000)
        val resultDiscountedPaymentAmount = movieDayDiscountEvent.discount(
            paymentAmount,
            LocalDateTime.of(2023, 4, 30, 13, 0)
        )
        val expectedDiscountedPaymentAmount = PaymentAmount(11700)

        Assert.assertEquals(resultDiscountedPaymentAmount, expectedDiscountedPaymentAmount)
    }

    @Test
    fun `10일_20일_30일이_아닌_경우_할인이_적용되지_않는다`() {
        val paymentAmount = PaymentAmount(13000)
        val resultDiscountedPaymentAmount = movieDayDiscountEvent.discount(
            paymentAmount,
            LocalDateTime.of(2023, 4, 13, 15, 0)
        )
        val expectedDiscountedPaymentAmount = PaymentAmount(13000)

        Assert.assertEquals(resultDiscountedPaymentAmount, expectedDiscountedPaymentAmount)
    }
}