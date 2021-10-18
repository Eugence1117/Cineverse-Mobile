package com.example.cineverseprototype.branch

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.cineverseprototype.R

class ExpandableListAdapter(
    val context: Context,
    val listTitle:List<StateLabel>,
    val listDetail:Map<StateLabel,List<Branch>>): BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return listTitle.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return listDetail[listTitle[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return listTitle[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listDetail[listTitle[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        val label:StateLabel = getGroup(groupPosition) as StateLabel
        var view = convertView
        if(view == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.state_group_item,null)
        }
        view!!.findViewById<TextView>(R.id.listTitle).text = label.stateName
        view!!.findViewById<TextView>(R.id.branchSum).text = label.showSum()
        return view!!
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        val branch:Branch = getChild(groupPosition,childPosition) as Branch
        var view = convertView
        if(convertView == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.branch_item,null)
        }
        view!!.findViewById<TextView>(R.id.branchName).text = branch.branchName
        view!!.findViewById<TextView>(R.id.operatingHour).text = branch.toBusinessHour()
        view!!.findViewById<TextView>(R.id.address).text = branch.toFullAddress()
        view!!.tag = branch.branchId

        return view!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}