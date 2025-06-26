package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.paging.PagingData
import com.yogadimas.simastekom.core.common.extensions.getBooleanData
import com.yogadimas.simastekom.core.common.model.IdNameResult
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.core.ui.adapter.paging.LoadingStateAdapter
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ActivitySmimportantContactCategoryBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.BaseListActivity
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.viewmodel.ext.android.viewModel

class SMImportantContactCategoryActivity :
    BaseListActivity<ActivitySmimportantContactCategoryBinding, ImportantContactCategoryData, SMImportantContactCategoryAdapter.ViewHolder, SMImportantContactCategoryAdapter>() {


    override fun inflateBinding(layoutInflater: LayoutInflater) =
        ActivitySmimportantContactCategoryBinding.inflate(layoutInflater)

    override val activityContext = this@SMImportantContactCategoryActivity
    override val viewmodel: SMImportantContactCategoryViewModel by viewModel()
    override lateinit var adapter: SMImportantContactCategoryAdapter
    override lateinit var loadingStateAdapter: LoadingStateAdapter
    override val main by lazy { binding.main }
    override val toolbar by lazy { binding.toolbar }
    override val toolbarSearchFilter by lazy { binding.toolbarSearchFilter }
    override val searchBar by lazy { binding.searchBar }
    override val searchView by lazy { binding.searchView }
    override val recyclerView by lazy { binding.rvImportantContactCategory }
    override val fabAdd by lazy { binding.fabAdd }
    override val loading by lazy { binding.loadingImportantContactCategory }
    override val emptyDataView by lazy { binding.viewHandle.viewEmptyData.root }
    override val notFoundDataView by lazy { binding.viewHandle.viewNotFoundData.root }
    override val errorLoadDataView by lazy { binding.viewHandle.viewErrorLoadData.root }
    override val errorBtnReload by lazy { binding.viewHandle.viewErrorLoadData.btnReload }
    override val notFoundBtnReload by lazy { binding.viewHandle.viewNotFoundData.btnReload }
    override val toManipulation: Class<*> =
        SMImportantContactCategoryManipulationActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        setupIntent()
        super.onCreate(savedInstanceState)
    }

    private var isFromImportantContact = false
    private fun setupIntent() {
        isFromImportantContact = intent.getBooleanData(KEY_IMPORTANT_CONTACT)
    }

    override fun setupAdapter() {
        adapter = SMImportantContactCategoryAdapter { data ->
            val data = IdNameResult(data.id, data.name)
            if (isFromImportantContact) {
                navigateToImportantContactActivity(data)
            } else {
                navigateToManipulationActivity(data.id)
            }
        }
    }


    private fun navigateToImportantContactActivity(data: IdNameResult) {
        val resultIntent = Intent()
        resultIntent.putExtra(KEY_IMPORTANT_CONTACT_RESULT_EXTRA, data)
        setResult(KEY_IMPORTANT_CONTACT_RESULT_CODE, resultIntent)
        finish()
    }


    override fun trigger(searchKeyword: String?): SharedFlow<PagingData<ImportantContactCategoryData>> =
        viewmodel.getImportantContactCategories(searchKeyword)


    companion object {
        const val KEY_IMPORTANT_CONTACT = "key_important_contact"
        const val KEY_IMPORTANT_CONTACT_RESULT_CODE = 1
        const val KEY_IMPORTANT_CONTACT_RESULT_EXTRA = "key_important_contact_result_extra"
    }
}