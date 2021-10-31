package com.example.cineverseprototype

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation


class ExpandCollapseAnimation(view: View, duration: Int, type: Int) :
    Animation() {
    private val mAnimatedView: View
    private val mEndHeight: Int
    private val mType: Int
    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        if (interpolatedTime < 1.0f) {
            if (mType == 0) {
                mAnimatedView.layoutParams.height = (mEndHeight * interpolatedTime).toInt()
            } else {
                mAnimatedView.layoutParams.height =
                    mEndHeight - (mEndHeight * interpolatedTime).toInt()
            }
            mAnimatedView.requestLayout()
        } else {
            if (mType == 0) {
                mAnimatedView.layoutParams.height = mEndHeight
                mAnimatedView.requestLayout()
            } else {
                mAnimatedView.layoutParams.height = 0
                mAnimatedView.visibility = View.GONE
                mAnimatedView.requestLayout()
                mAnimatedView.layoutParams.height = mEndHeight
            }
        }
    }

    init {
        setDuration(duration.toLong())
        mAnimatedView = view
        mEndHeight = mAnimatedView.layoutParams.height
        mType = type
        if (mType == 0) {
            mAnimatedView.layoutParams.height = 0
            mAnimatedView.visibility = View.VISIBLE
        }
    }
}