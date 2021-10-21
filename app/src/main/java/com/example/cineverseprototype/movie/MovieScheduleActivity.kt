package com.example.cineverseprototype.movie

import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.Log
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.databinding.ActivityMovieScheduleBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.*


class MovieScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMovieScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movie = intent.getSerializableExtra("movie") as Movie

        setSupportActionBar(binding.toolbar)

        Singleton.getInstance(this).picasso.load(movie.picURL).placeholder(R.drawable.baseline_image_grey_400_48dp).into(binding.movieImage)

        val format = SimpleDateFormat("dd-MMM-yyyy")

        binding.movieDuration.text = SpannableStringBuilder().bold {append("Duration : ")}.append(movie.getTotalTime())
        binding.moviePlot.text = movie.synopsis
        binding.movieCast.text = SpannableStringBuilder().bold {append("Cast By : ")}.append(movie.cast)
        binding.movieCensorship.text =  SpannableStringBuilder().bold {append("Censorship : ")}.append(movie.censorship)
        binding.movieDirector.text =  SpannableStringBuilder().bold {append("Director : ")}.append(movie.director)
        binding.movieDistributor.text =  SpannableStringBuilder().bold {append("Distributor : ")}.append(movie.distributor)
        binding.movieReleaseDate.text =  SpannableStringBuilder().bold {append("Release Date : ")}.append(format.format(Date(movie.releaseDate)))
        binding.movieType.text =  SpannableStringBuilder().bold {append("Type : ")}.append(movie.movieType)

        val sheetBehavior = BottomSheetBehavior.from(binding.contentLayout);
        sheetBehavior.isFitToContents = true;
        sheetBehavior.isHideable = false;//prevents the boottom sheet from completely hiding off the screen
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;//initially state to fully expanded

        binding.bookButton.setOnClickListener {
            if(sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        binding.movieInfoBtn.setOnClickListener {
            if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            else{
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        binding.contentLayout.setOnClickListener {
            if(sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }
}