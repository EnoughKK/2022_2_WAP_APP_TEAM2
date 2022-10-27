package com.example.wapapp2.view.calculation

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wapapp2.R
import com.example.wapapp2.databinding.GroupFragmentBinding
import com.example.wapapp2.databinding.GroupItemBinding
import com.example.wapapp2.databinding.ListAddViewBinding
import com.example.wapapp2.dummy.CalcRoomDummyData
import com.example.wapapp2.view.main.MainHostFragment


class GrouplistFragment : Fragment() {
    private lateinit var binding: GroupFragmentBinding

    private lateinit var adapter: GroupAdapter

    /** Enter Group **/
    private val onClickedItemListener = object : OnClickedItemListener {
        override fun onClickedItem(position: Int) {
            val fragment = CalcMainFragment()
            val fragmentManager = parentFragment!!.parentFragmentManager

            val bundle = Bundle()
            val calcRoomDummyData = CalcRoomDummyData.getRoom()
            bundle.putString("roomId", calcRoomDummyData.roomId)

            fragment.arguments = bundle

            fragmentManager.beginTransaction()
                    .hide(fragmentManager.findFragmentByTag(MainHostFragment::class.java.name) as
                            Fragment)
                    .add(R.id.fragment_container_view, fragment, CalcMainFragment::class.java.name)
                    .addToBackStack(CalcMainFragment::class.java.name).commitAllowingStateLoss()
        }

    }

    
    /** Add Group **/
    private val addOnClickedItemListener = View.OnClickListener {
        val fragment = NewCalcFragment()
        val fragmentManager = requireParentFragment().parentFragmentManager

        fragmentManager.beginTransaction()
                .hide(fragmentManager.findFragmentByTag(MainHostFragment::class.java.name) as
                        Fragment)
                .add(R.id.fragment_container_view, fragment, NewCalcFragment::class.java.name)
                .addToBackStack(NewCalcFragment::class.java.name).commitAllowingStateLoss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = GroupFragmentBinding.inflate(layoutInflater)
        val groupItem = arrayListOf<GroupItem>(
                GroupItem("2022-09-23", "OOO, OOO, OOO 외 1명", "정산완료"),
                GroupItem("2022-09-27", "OOO, OOO, OOO 외 3명", "정산진행중..")
        )
        adapter = GroupAdapter(context, groupItem)
        binding.groupRV.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.groupRV.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        //binding.groupRV.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    private enum class ItemViewType {
        ADD, GROUP_ITEM
    }

    private inner class GroupItem(val date: String, val names: String, val state: String)

    private inner class GroupAdapter(private val context: Context?, private val items: ArrayList<GroupItem>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class GroupVH(val binding: GroupItemBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(groupItem: GroupItem) {
                binding.groupItemDate.text = groupItem.date
                binding.groupItemNames.text = groupItem.names
                binding.groupItemState.text = groupItem.state

                binding.root.setOnClickListener {
                    onClickedItemListener.onClickedItem(adapterPosition)
                }
            }

        }

        inner class AddVH(val binding: ListAddViewBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                binding.groupBtnAdd.setOnClickListener {
                    addOnClickedItemListener.onClick(binding.root)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            return
                if (position == itemCount - 1) (holder as AddVH).bind()
                else (holder as GroupVH).bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size + 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == ItemViewType.GROUP_ITEM.ordinal) GroupVH(GroupItemBinding.inflate(LayoutInflater.from(context)))
            else AddVH(ListAddViewBinding.inflate(LayoutInflater.from(context)))
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == itemCount - 1) ItemViewType.ADD.ordinal else ItemViewType.GROUP_ITEM.ordinal
        }

    }

    private interface OnClickedItemListener {
        fun onClickedItem(position: Int)
    }
}