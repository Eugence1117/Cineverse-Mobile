package com.example.cineverseprototype.movie

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton

class MoviePosterRecycleAdapter(private val movieList:ArrayList<Movie>): RecyclerView.Adapter<MoviePosterRecycleAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_poster_item,parent,false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        //holder.itemView.findViewById<TextView>(R.id.movieName).text = movieList[position].movieName
        holder.itemView.findViewById<TextView>(R.id.censorship).text = movieList[position].censorship
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.itemView.findViewById<TextView>(R.id.censorship).backgroundTintList = ColorStateList.valueOf(Color.parseColor(movieList[position].getCensorshipColor()))
        }

        Singleton.getInstance(holder.itemView.context).picasso.load(movieList[position].picURL).placeholder(
            R.drawable.baseline_image_grey_400_48dp).into(holder.itemView.findViewById<ImageView>(R.id.movieImage))
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view){
    }
}