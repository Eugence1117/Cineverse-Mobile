package com.example.cineverseprototype.movie

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton

class MovieRecycleAdapter(private val movieList:ArrayList<Movie>, private val branchId:String) : RecyclerView.Adapter<MovieRecycleAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_item,parent,false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.movieName).text = movieList[position].movieName
        holder.itemView.findViewById<TextView>(R.id.movieDuration).text = movieList[position].getTotalTime()
        holder.itemView.findViewById<TextView>(R.id.movieType).text = movieList[position].movieType
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,MovieScheduleActivity::class.java)
            intent.putExtra("movie",movieList[position])
            intent.putExtra("branchId",branchId)
            holder.itemView.context.startActivity(intent)
        }
        //holder.itemView.findViewById<TextView>(R.id.censorship).text = movieList[position].censorship

        Singleton.getInstance(holder.itemView.context).picasso.load(movieList[position].picURL).placeholder(R.drawable.baseline_image_grey_400_48dp).into(holder.itemView.findViewById<ImageView>(R.id.movieImg))
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }
}