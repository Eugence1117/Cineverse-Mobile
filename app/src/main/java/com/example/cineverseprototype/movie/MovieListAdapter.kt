package com.example.cineverseprototype.movie

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ethanhua.skeleton.Skeleton
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton
import com.squareup.picasso.Callback
import java.lang.Exception

class MovieListAdapter(context: Context, movieList:ArrayList<Movie>) : ArrayAdapter<Movie>(context, R.layout.movie_item,movieList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if(view == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.movie_item,null)
        }

        val movie = getItem(position)!!

        val imageView = view!!.findViewById<ImageView>(R.id.movieImg)
        val skeletonScreen = Skeleton.bind(imageView).load(R.layout.profile_picture_skeleton).show()

        Singleton.getInstance(context).picasso.load(movie.picURL).into(imageView,object:Callback{
            override fun onSuccess() {
                skeletonScreen.hide()
            }

            override fun onError(e: Exception?) {
                //Do not hide
            }

        })
        view!!.findViewById<TextView>(R.id.movieName).text = movie.movieName
        view!!.findViewById<TextView>(R.id.movieDuration).text = movie.getTotalTime()
        view!!.findViewById<TextView>(R.id.movieType).text = movie.movieType
        //view!!.findViewById<TextView>(R.id.censorship).text = movie.censorship

        return view!!
    }

}