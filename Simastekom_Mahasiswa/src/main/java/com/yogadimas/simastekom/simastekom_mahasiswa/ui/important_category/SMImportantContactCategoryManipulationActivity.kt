package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewStub
import androidx.core.view.isVisible
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactCategoryRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.ui.UiState
import com.yogadimas.simastekom.simastekom_mahasiswa.R
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ActivitySmimportantContactCategoryManipulationBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.LayoutFormImportantContactCategoryBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.BaseManipulationActivity
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.viewmodel.ext.android.viewModel

private typealias VStubImportantContactCategory = LayoutFormImportantContactCategoryBinding

class SMImportantContactCategoryManipulationActivity : BaseManipulationActivity<
        ActivitySmimportantContactCategoryManipulationBinding,
        ImportantContactCategoryData,
        ImportantContactCategoryRequest
        >() {

    private lateinit var vStub: ViewStub
    private var vStubBinding: VStubImportantContactCategory? = null

    override fun inflateBinding(layoutInflater: LayoutInflater): ActivitySmimportantContactCategoryManipulationBinding {
        return ActivitySmimportantContactCategoryManipulationBinding.inflate(layoutInflater)
    }

    override val activityContext = this@SMImportantContactCategoryManipulationActivity
    override val viewmodel: SMImportantContactCategoryViewModel by viewModel()
    override var dataBundle = ImportantContactCategoryRequest()

    override val main by lazy { binding.main }
    override val toolbar by lazy { binding.viewAppBar.toolbar }
    override val loading by lazy { binding.loadingImportantContactCategory }
    override val emptyDataView by lazy { binding.viewHandle.viewEmptyData.root }
    override val errorLoadDataView by lazy { binding.viewHandle.viewErrorLoadData.root }
    override val errorBtnReload by lazy { binding.viewHandle.viewErrorLoadData.btnReload }
    override val labelDialogSuccess: String get() = getString(R.string.text_important_contact_category)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupIntent()
        setupBundle(savedInstanceState)
        setupView { vStub = binding.vsImportantContactCategory }
        setupListener()
        setupCollector()
    }

    override fun state(): SharedFlow<UiState<BaseResponse<ImportantContactCategoryData>>> {
        return viewmodel.importantContactCategoryState
    }

    override fun getById() {
        viewmodel.getImportantContactCategoryById(dataBundle.id)
    }

    override fun setCreate(data: ImportantContactCategoryRequest) {
        viewmodel.createImportantContactCategory(data)
    }

    override fun setUpdate(data: ImportantContactCategoryRequest) {
        viewmodel.updateImportantContactCategory(dataBundle.id, data)
    }

    override fun setDelete() {
        viewmodel.deleteImportantContactCategory(dataBundle.id)
    }

    override fun setupMainData(data: ImportantContactCategoryData?) {
        vStubBinding?.run {
            setupDataSource(data)
            setupDataViewOnStub()
            setupListenerOnStub()
        }
    }

    override fun setupDataSource(data: ImportantContactCategoryData?) {
        val dataRemote = data ?: ImportantContactCategoryData()
        val name = dataBundle.name ?: dataRemote.name
        dataBundle.name = name
    }

    override fun setupBtnSaveState() {
        vStubBinding?.btnSave?.run { isEnabled = isFormValid() }
    }

    override fun isFormValid(): Boolean = vStubBinding?.run {
        listOf(
            edtName,
        ).all { it.text.toString().isNotEmpty() }
    } == true

    override fun initStubBindingIfNull() {
        if (vStubBinding == null) {
            vStubBinding = LayoutFormImportantContactCategoryBinding.bind(vStub.inflate())
        }
    }

    override fun setupDataViewOnStub() {
        vStubBinding?.run {
            edtName.setText(dataBundle.name)
        }
    }

    override fun setupListenerOnStub() {
        vStubBinding?.run {
            edtName.setTextWatchers { dataBundle.name = it }
            setupBtnSaveState()
            btnSave.setOnClickListener { saveOnStub() }
            btnDelete.setOnClickListener { deleteOnStub() }
        }
    }

    override fun showDefaultView(isShow: Boolean) {
        super.showDefaultView(isShow)
        showViewStub(isShow)
        vStubBinding?.run {
            setupMode(
                createdAction = { btnDelete.isVisible = !isShow },
                detailAction = { btnDelete.isVisible = isShow })
        }
    }

    private fun showViewStub(value: Boolean) = vStubBinding?.root?.run { isVisible = value }


}