package woowacourse.movie.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import domain.movie.ScreeningDate
import domain.reservation.Reservation
import domain.reservation.TicketCount
import woowacourse.movie.R
import woowacourse.movie.activity.MoviesActivity.Companion.MOVIE_KEY
import woowacourse.movie.model.ActivityMovieModel
import woowacourse.movie.model.ActivityReservationModel
import woowacourse.movie.model.toActivityModel
import woowacourse.movie.model.toDomainModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ReservationActivity : AppCompatActivity() {

    private val screeningDateSpinner: Spinner by lazy {
        findViewById(R.id.screening_date_spinner)
    }
    private val screeningTimeSpinner: Spinner by lazy {
        findViewById(R.id.screening_time_spinner)
    }
    private val ticketCountTextView: TextView by lazy {
        findViewById(R.id.reservation_ticket_count_text_view)
    }
    private val movie: ActivityMovieModel by lazy {
        intent.getSerializableExtra(MOVIE_KEY) as ActivityMovieModel?
            ?: throw IllegalArgumentException(getString(R.string.movie_data_error_message))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        initReservationView()
        initClickListener()
        initTicketCount(savedInstanceState)
        initSpinner(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val selectedDatePosition: Int = screeningDateSpinner.selectedItemPosition
        val selectedTimePosition: Int = screeningTimeSpinner.selectedItemPosition

        outState.putInt(TICKET_COUNT_KEY, ticketCountTextView.text.toString().toInt())
        outState.putInt(SCREENING_DATE_POSITION_KEY, selectedDatePosition)
        outState.putInt(SCREENING_TIME_POSITION_KEY, selectedTimePosition)
    }

    private fun initReservationView() {
        val descriptionTextView: TextView =
            findViewById(R.id.reservation_movie_description_text_view)
        val runningTimeTextView: TextView =
            findViewById(R.id.reservation_movie_running_time_text_view)
        val screeningDateTextView: TextView =
            findViewById(R.id.reservation_movie_screening_date_text_view)
        val movieMovieNameTextView: TextView =
            findViewById(R.id.reservation_movie_name_text_view)
        val posterImageView: ImageView =
            findViewById(R.id.reservation_movie_image_view)

        with(movie) {
            val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_DATE

            posterImage?.let { id -> posterImageView.setImageResource(id) }
            movieMovieNameTextView.text = movieName
            screeningDateTextView.text = getString(R.string.screening_period_form).format(
                startDate.format(dateFormat),
                endDate.format(dateFormat)
            )
            runningTimeTextView.text = getString(R.string.running_time_form).format(runningTime)
            descriptionTextView.text = description
        }
    }

    private fun initTicketCount(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            ticketCountTextView.text = TicketCount.MINIMUM.toString()
            return
        }
        val ticketCount: Int = savedInstanceState.getInt(TICKET_COUNT_KEY)
        ticketCountTextView.text = ticketCount.toString()
    }

    private fun initSpinner(savedInstanceState: Bundle?) {
        val dates = movie.screeningPeriod
        val defaultScreeningDatePosition = savedInstanceState?.getInt(SCREENING_DATE_POSITION_KEY) ?: 0
        val defaultScreeningTimePosition = savedInstanceState?.getInt(SCREENING_TIME_POSITION_KEY) ?: 0

        screeningDateSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            dates
        )
        screeningDateSpinner.onItemSelectedListener = SpinnerItemSelectedListener(
            screeningDateSpinner,
            defaultScreeningTimePosition,
            ::initTimeSpinner
        )
        screeningDateSpinner.setSelection(defaultScreeningDatePosition)
    }

    private fun initTimeSpinner(date: ScreeningDate?, defaultPosition: Int = 0) {
        val times = date?.screeningTimes ?: listOf()

        screeningTimeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            times
        )
        screeningTimeSpinner.setSelection(defaultPosition)
    }

    private fun initClickListener() {
        initMinusClickListener()
        initPlusClickListener()
        initCompleteButton()
    }

    private fun initMinusClickListener() {
        val minusButton = findViewById<Button>(R.id.reservation_ticket_count_minus_button)

        minusButton.setOnClickListener {
            runCatching {
                val ticketCount = TicketCount(ticketCountTextView.text.toString().toInt() - 1)
                ticketCountTextView.text = ticketCount.value.toString()
            }.onFailure {
                val ticketCountConditionMessage =
                    getString(R.string.ticket_count_condition_message_form).format(TicketCount.MINIMUM)
                Toast.makeText(this, ticketCountConditionMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initPlusClickListener() {
        val plusButton = findViewById<Button>(R.id.reservation_ticket_count_plus_button)

        plusButton.setOnClickListener {
            val ticketCount = TicketCount(ticketCountTextView.text.toString().toInt() + 1)
            ticketCountTextView.text = ticketCount.value.toString()
        }
    }

    private fun initCompleteButton() {
        val completeButton: Button = findViewById(R.id.reservation_complete_button)

        completeButton.setOnClickListener {
            val ticketCount = ticketCountTextView.text.toString().toInt()
            val screeningDate = screeningDateSpinner.selectedItem as LocalDate
            val screeningTime = screeningTimeSpinner.selectedItem as LocalTime
            val reservation: ActivityReservationModel =
                Reservation
                    .from(movie.toDomainModel(), ticketCount, LocalDateTime.of(screeningDate, screeningTime))
                    .toActivityModel()

            val intent = Intent(this, ReservationResultActivity::class.java)

            intent.putExtra(RESERVATION_KEY, reservation)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val TICKET_COUNT_KEY = "ticket_key"
        private const val SCREENING_DATE_POSITION_KEY = "screening_date_key"
        const val SCREENING_TIME_POSITION_KEY = "screening_time_key"
        const val RESERVATION_KEY = "reservation_key"
    }
}
