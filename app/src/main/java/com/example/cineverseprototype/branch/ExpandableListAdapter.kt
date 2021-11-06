package com.example.cineverseprototype.branch

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.example.cineverseprototype.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class ExpandableListAdapter(
    val context: Context,
    val listTitle:List<StateLabel>,
    val listDetail:Map<StateLabel,List<Branch>>): BaseExpandableListAdapter(),Filterable {

    private var filteredData:MutableMap<StateLabel,MutableList<Branch>> = LinkedHashMap()
    private var filteredGroup:MutableList<StateLabel> = ArrayList()

    override fun getGroupCount(): Int {
        return filteredGroup.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return filteredData[filteredGroup[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return filteredGroup[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return filteredData[filteredGroup[groupPosition]]!![childPosition]
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

    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResult = FilterResults()
                if(constraint.isNullOrEmpty()){
                    filterResult.values = listDetail
                    filterResult.count = listDetail.size
                }
                else{
                    val filteredData:MutableMap<StateLabel,MutableList<Branch>> = LinkedHashMap()
                    Log.i("Debug","Filtering $constraint")
                    for(key in listTitle){
                        val listItem = listDetail[key]
                        if(key.stateName.contains(constraint,true)){
                            if(listItem != null){
                                filteredData[key] = listItem.toMutableList()
                            }
                        }
                        else{
                            if(listItem != null){
                                for(branch in listItem){
                                    if(branch.address.contains(constraint,true) || branch.branchName.contains(constraint,true) || branch.district.contains(constraint,true) || branch.state.contains(constraint,true)){
                                        if(filteredData.contains(key)){
                                            val branches = filteredData[key]
                                            if(branches != null){
                                                branches.add(branch)
                                                filteredData[key] = branches
                                            }
                                            else{
                                                val branches:ArrayList<Branch> = ArrayList()
                                                branches.add(branch)
                                                filteredData[key] = branches
                                            }
                                        }
                                        else{
                                            val branches:ArrayList<Branch> = ArrayList()
                                            branches.add(branch)
                                            filteredData[key] = branches
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Log.i("Debug","Total Count ${filteredData.size}")
                    filterResult.values = filteredData
                    filterResult.count = filteredData.size
                }
                return filterResult
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results != null){
                    if(results.values != null){
                        val dataList = results?.values as MutableMap<StateLabel,MutableList<Branch>>
                        val sortMap = dataList.toSortedMap(kotlin.Comparator { o1, o2 ->  o1.stateName.compareTo(o2.stateName)})

                        filteredData = sortMap
                        filteredGroup.clear()
                        for(key in filteredData.keys){
                            filteredGroup.add(key)
                        }
                    }
                    else{
                        filteredData = LinkedHashMap<StateLabel,MutableList<Branch>>()
                    }
                }
                notifyDataSetChanged()
            }

        }
    }

}