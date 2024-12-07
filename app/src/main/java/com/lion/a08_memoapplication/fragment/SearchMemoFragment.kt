package com.lion.a08_memoapplication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.lion.a08_memoapplication.MainActivity
import com.lion.a08_memoapplication.R
import com.lion.a08_memoapplication.databinding.FragmentModifyMemoBinding
import com.lion.a08_memoapplication.databinding.FragmentSearchMemoBinding
import com.lion.a08_memoapplication.databinding.RowText1Binding
import com.lion.a08_memoapplication.model.MemoModel
import com.lion.a08_memoapplication.repository.MemoRepository
import com.lion.a08_memoapplication.util.FragmentName
import com.lion.a08_memoapplication.util.MemoListName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class SearchMemoFragment : Fragment() {

    lateinit var fragmentSearchMemoBinding: FragmentSearchMemoBinding
    lateinit var mainActivity: MainActivity

    var memoList = mutableListOf<MemoModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSearchMemoBinding =
            FragmentSearchMemoBinding.inflate(layoutInflater, container, false)
        mainActivity = activity as MainActivity

        // setToolbar
        settingToolbar()

        // setRecyclerView
        settingRecyclerView()

        settingTextField()

        return fragmentSearchMemoBinding.root
    }


    // Toolbar를 구성하는 메서드
    fun settingToolbar() {
        fragmentSearchMemoBinding.apply {
            if (arguments != null) {
                if (arguments?.getString("MemoName") != MemoListName.MEMO_NAME_ADDED.str) {
                    toolbarSearchMemo.title = arguments?.getString("MemoName")
                } else {
                    toolbarSearchMemo.title = arguments?.getString("categoryName")
                }
            } else {
                toolbarSearchMemo.title = MemoListName.MEMO_NAME_SEARCH.str
            }

            // 네비게이션 아이콘을 설정하고 누를 경우 NavigationView가 나타나도록 한다.
            toolbarSearchMemo.setNavigationIcon(R.drawable.menu_24px)
            toolbarSearchMemo.setNavigationOnClickListener {
                mainActivity.activityMainBinding.drawerLayoutMain.open()
            }
        }
    }

    // Set RecyclerView
    fun settingRecyclerView() {
        fragmentSearchMemoBinding.apply {
            recyclerViewSearchMemo.adapter = SearchListAdapter()
            val deco =
                MaterialDividerItemDecoration(mainActivity, MaterialDividerItemDecoration.VERTICAL)
            recyclerViewSearchMemo.addItemDecoration(deco)
            recyclerViewSearchMemo.layoutManager = GridLayoutManager(mainActivity, 3)
        }
    }

    // Set TextField
    fun settingTextField() {
        fragmentSearchMemoBinding.apply {
            // 검색창에 포커스를 준다.
            mainActivity.showSoftInput(textFieldSearchMemoName.editText!!)

            // 키보드 입력할 때마다 데이터 검색 및 갱신
            textFieldSearchMemoName.editText?.addTextChangedListener { text ->
                CoroutineScope(Dispatchers.Main).launch {
                    val keyword = text?.toString()?.trim() ?: ""

                    if (text?.isEmpty() == true) {
                        memoList.clear()
                        textViewWrongSearch.visibility = View.VISIBLE
                        fragmentSearchMemoBinding.recyclerViewSearchMemo.adapter?.notifyDataSetChanged()
                    } else {
                        val work1 = async(Dispatchers.IO) {
                            MemoRepository.selectShopDataAllByMemoTitle(mainActivity, keyword)
                        }
                        memoList = work1.await()
                        if (memoList.isEmpty()) {
                            textViewWrongSearch.visibility = View.VISIBLE
                        } else {
                            textViewWrongSearch.visibility = View.GONE
                        }
                        fragmentSearchMemoBinding.recyclerViewSearchMemo.adapter?.notifyDataSetChanged()
                    }
                }


            }
        }
    }

    inner class SearchListAdapter() :
        RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder>() {
        inner class SearchListViewHolder(val rowText1Binding: RowText1Binding) :
            RecyclerView.ViewHolder(rowText1Binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
            val rowText1Binding = RowText1Binding.inflate(layoutInflater)
            val searchListViewHolder = SearchListViewHolder(rowText1Binding)

            rowText1Binding.root.setOnClickListener {
                val dataBundle = Bundle()
                dataBundle.putInt("memoIdx", memoList[searchListViewHolder.adapterPosition].memoIdx)
                mainActivity.replaceFragment(
                    FragmentName.READ_MEMO_FRAGMENT,
                    true,
                    true,
                    dataBundle
                )
            }

            return searchListViewHolder
        }

        override fun getItemCount(): Int {
            return memoList.size
        }

        override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
            holder.rowText1Binding.textViewRow.text = memoList[position].memoTitle
        }
    }
}