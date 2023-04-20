package woowacourse.movie.seatselection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import domain.movie.MovieName
import domain.reservation.Reservation
import domain.reservation.SeatReservation
import woowacourse.movie.R
import woowacourse.movie.getIntentData
import woowacourse.movie.model.SeatReservationInfo
import woowacourse.movie.model.toDomainModel
import woowacourse.movie.model.toUIModel
import woowacourse.movie.reservation.ReservationActivity.Companion.SEAT_RESERVATION_KEY
import woowacourse.movie.reservationresult.ReservationResultActivity

class ScreeningSeatSelectionActivity : AppCompatActivity() {

    private lateinit var seatReservationInfo: SeatReservationInfo
    private val seatReservation: SeatReservation by lazy { seatReservationInfo.toDomainModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screening_seat_selection)

        seatReservationInfo = getIntentData(SEAT_RESERVATION_KEY) ?: SeatReservationInfo.ofError()
        setMovieNameTextView()
        setSeatTableView()
        setOnCompleteButtonClickedListener()
    }

    private fun setMovieNameTextView() {
        val movieNamTextView = findViewById<TextView>(R.id.seat_selection_movie_name_text)

        movieNamTextView.text = seatReservationInfo.movieName
    }

    private fun setSeatTableView() {
        val seatTableLayout = findViewById<TableLayout>(R.id.seat_table_layout)

        ScreeningSeatViewSetter(seatTableLayout, seatReservation).apply {
            setSeatViewClickedListener(::setPaymentAmountTextView)
        }
    }

    private fun setPaymentAmountTextView(paymentAmount: Int) {
        val paymentAmountText: TextView = findViewById(R.id.seat_selection_payment_amount_text)

        paymentAmountText.text = paymentAmount.toString()
    }

    private fun setOnCompleteButtonClickedListener() {
        val seatSelectionCompleteButton = findViewById<Button>(R.id.seat_selection_complete_button)

        seatSelectionCompleteButton.setOnClickListener {
            onCompleteButtonClicked()
        }
    }

    private fun onCompleteButtonClicked() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reservation_check_text))
            .setMessage(getString(R.string.asking_reserve_text))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> onFinished() }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun onFinished() {
        runCatching {
            val intent = Intent(this, ReservationResultActivity::class.java)
            val reservation = Reservation.of(
                MovieName(seatReservationInfo.movieName),
                seatReservation.seatCount,
                seatReservation.screeningDateTime,
                seatReservation.getTotalPaymentAmount(),
                seatReservation.selectingComplete()
            ).toUIModel()

            intent.putExtra(RESERVATION_RESULT_KEY, reservation)
            startActivity(intent)
        }.onFailure {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val RESERVATION_RESULT_KEY = "reservation_result_key"
    }
}
