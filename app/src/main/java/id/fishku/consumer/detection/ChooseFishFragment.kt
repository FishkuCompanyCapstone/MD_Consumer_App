package id.fishku.consumer.detection

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.fishku.consumer.R
import id.fishku.consumer.core.data.Resource
import id.fishku.consumer.core.domain.model.FishType
import id.fishku.consumer.core.ui.FishTypeAdapter
import id.fishku.consumer.core.utils.showMessage
import id.fishku.consumer.databinding.FragmentChooseFishBinding
import id.fishku.consumer.fishinformation.FishNutritionActivity
import id.fishku.consumer.fishrecipe.FishRecipeActivity
import id.fishku.consumer.model.FishTypeDetection

@AndroidEntryPoint
class ChooseFishFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentChooseFishBinding? = null
    private val binding get() = _binding

//    private val detectionViewModel: DetectionViewModel by viewModels()
//    private val fishTypeAdapter: FishTypeAdapter by lazy { FishTypeAdapter(::productItemClicked) }

    private val fishTypeViewModel: FishTypeDetectionViewModel by viewModels()
    private val fishTypeAdapter = FishTypeDetectionAdapter { selectedFish ->
        productItemClicked(selectedFish)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentChooseFishBinding.inflate(inflater, container, false)
        return binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setupData()
        setUpAction()

        setupRecyclerView()

        // Observe the LiveData in the ViewModel
        fishTypeViewModel.fishList.observe(viewLifecycleOwner, Observer { fishList ->
            fishTypeAdapter.submitList(fishList)
        })

        // Fetch fish data when the fragment is created
        fishTypeViewModel.fetchFishData()

    }

    fun setPartOfFish(fishName: String?, fishPart: String?) {
        val toDetectionFish =
            ChooseFishFragmentDirections.actionNavigationChooseFishToDetectionFishActivity(
                fishName, fishPart
            )
        view?.findNavController()?.navigate(toDetectionFish)
    }

    private fun setupRecyclerView() {
        binding?.rvFishType?.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // Set the number of columns as needed
            adapter = fishTypeAdapter
        }
    }

    private fun setUpAction() {
        binding?.apply {
            ivFishRecipe.setOnClickListener(this@ChooseFishFragment)
            ivFishInformation.setOnClickListener(this@ChooseFishFragment)
        }
    }

//    private fun setupAdapter() {
//        binding?.rvFishType?.apply {
//            setHasFixedSize(true)
//            adapter = fishTypeAdapter
//        }
//    }
//
//    private fun isEmptyResult(isEmpty: Boolean) {
//        if (!isEmpty) {
//            binding?.apply {
//                rvFishType.visibility = View.VISIBLE
//                imgEmptySearch.visibility = View.GONE
//                tvEmptySearch.visibility = View.GONE
//            }
//        } else {
//            binding?.apply {
//                rvFishType.visibility = View.GONE
//                imgEmptySearch.visibility = View.VISIBLE
//                tvEmptySearch.visibility = View.VISIBLE
//            }
//        }
//    }

    private fun showBottomSheet(fishType: FishTypeDetection) {
        val bundle = Bundle()
        bundle.apply {
            putString(FISH_NAME, fishType.fishName)
            putString(FISH_PHOTO, fishType.photoUrl)
        }

        val modalBottomSheet = FishTypeDialogFragment()
        modalBottomSheet.arguments = bundle
        modalBottomSheet.show(childFragmentManager, modalBottomSheet.tag)
    }

    private fun productItemClicked(fishType: FishTypeDetection) {
        showBottomSheet(fishType)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivFishRecipe -> {
                val intentToFishRecipe = Intent(requireContext(), FishRecipeActivity::class.java)
                startActivity(intentToFishRecipe)

            }

            R.id.ivFishInformation -> {
                val intentToFishInformation =
                    Intent(requireContext(), FishNutritionActivity::class.java)
                startActivity(intentToFishInformation)
            }
        }
    }

    companion object {
        const val FISH_NAME = "fish_name"
        const val FISH_PHOTO = "fish_photo"
    }


}

//private fun setupData() {
//        detectionViewModel.getListFishDetection().observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Loading -> {
//                    binding?.apply {
//                        loadingSearch.visibility = View.VISIBLE
//                        rvFishType.visibility = View.GONE
//                        imgEmptySearch.visibility = View.GONE
//                        tvEmptySearch.visibility = View.GONE
//                        imgErrorSearch.visibility = View.GONE
//                        tvErrorSearch.visibility = View.GONE
//                    }
//                }
//
//                is Resource.Success -> {
//                    binding?.loadingSearch?.visibility = View.GONE
//                    if (!it.data.isNullOrEmpty()) {
//                        fishTypeAdapter.submitList(it.data)
//                        setupAdapter()
//                        isEmptyResult(false)
//                    } else {
//                        isEmptyResult(true)
//                    }
//                }
//
//                is Resource.Error -> {
//                    binding?.apply {
//                        loadingSearch.visibility = View.GONE
//                        imgErrorSearch.visibility = View.VISIBLE
//                        tvErrorSearch.visibility = View.VISIBLE
//                    }
//                    it.message?.showMessage(requireContext())
//                }
//
//                else -> {}
//            }
//        }
//    }