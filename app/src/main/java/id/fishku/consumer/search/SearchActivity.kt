package id.fishku.consumer.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import dagger.hilt.android.AndroidEntryPoint
import id.fishku.consumer.R
import id.fishku.consumer.cart.CartActivity
import id.fishku.consumer.core.data.Resource
import id.fishku.consumer.core.domain.model.Fish
import id.fishku.consumer.core.ui.SearchAdapter
import id.fishku.consumer.core.utils.showMessage
import id.fishku.consumer.databinding.ActivitySearchBinding
import id.fishku.consumer.detailfish.DetailFishActivity

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val searchAdapter: SearchAdapter by lazy { SearchAdapter(::productItemClicked) }
    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSearch()
        setupData()
        searchFish()
        setupCart()

        setupToolbar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            setupToolbar()
        } else {
            val cartIntent = Intent(this, CartActivity::class.java)
            startActivity(cartIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSearch)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        //drawer
        binding.apply {
            toggle = ActionBarDrawerToggle(
                this@SearchActivity,
                drawerLayout,
                R.string.open,
                R.string.close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
        }
    }

    fun onButtonClick(view: View) {
        when (view.id) {
            R.id.btnReset -> resetFilterButtons()
            R.id.btnApply -> applyFilters()
        }
    }

    private fun resetFilterButtons() {
        binding.drawerContent.apply {
            btnNearest.resetState()
            btnBest.resetState()
            btnNewest.resetState()
            btnHighestPrice.resetState()
            btnLowestPrice.resetState()
        }
    }

    private fun applyFilters() {
        val locationFilter = null//TODO dapetin lokasi kota dari data SetLocationActivity
        val unfilteredResults = searchViewModel.result.value?.data ?: emptyList()

        val locationFilteredResults = if (locationFilter != null) {
            unfilteredResults.filter { fish ->
                fish.location == locationFilter
            }
        } else {
            unfilteredResults
        }

        val sortedResults = when {
            binding.drawerContent.btnNearest.isActivated -> {
                locationFilteredResults.sortedByDescending { it.location }
            }
            //TODO button lainnya
            binding.drawerContent.btnHighestPrice.isActivated -> {
                unfilteredResults.sortedByDescending { it.price }
            }
            binding.drawerContent.btnLowestPrice.isActivated -> {
                unfilteredResults.sortedBy { it.price }
            }
            else -> {
                unfilteredResults
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        searchAdapter.submitList(sortedResults)
    }
    //TAMBAHAN - sampai sini

    private fun initSearch() {
        val query = SearchActivityArgs.fromBundle(intent.extras as Bundle).query

        binding.searchViewFish.setQuery(query, false)
        if (query != null) {
            if (query.isNotEmpty()) {
                searchViewModel.searchFish(query)
            } else {
                searchViewModel.getAllFish()
            }
        }
    }

    private fun setupData() {
        searchViewModel.result.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    binding.apply {
                        loadingSearch.visibility = View.VISIBLE
                        rvSearchProduct.visibility = View.GONE
                        imgEmptySearch.visibility = View.GONE
                        tvEmptySearch.visibility = View.GONE
                        imgErrorSearch.visibility = View.GONE
                        tvErrorSearch.visibility = View.GONE
                    }
                }

                is Resource.Success -> {
                    binding.loadingSearch.visibility = View.GONE
                    if (!it.data.isNullOrEmpty()) {
                        searchAdapter.submitList(it.data)
                        setupAdapter()
                        isEmptyResult(false)
                    } else {
                        isEmptyResult(true)
                    }
                }

                is Resource.Error -> {
                    binding.apply {
                        loadingSearch.visibility = View.GONE
                        imgErrorSearch.visibility = View.VISIBLE
                        tvErrorSearch.visibility = View.VISIBLE
                    }
                    it.message?.showMessage(this)
                }
            }
        }
    }

    private fun searchFish() {
        binding.searchViewFish.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        searchViewModel.searchFish(query)
                    }
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        searchAdapter.submitList(null)
                        isEmptyResult(true)
                        clearFocus()
                    }
                    return true
                }
            })
        }
    }

    private fun setupCart() {
        val menuItem = binding.toolbarSearch.menu.getItem(0).actionView

        menuItem?.setOnClickListener {
            val cartIntent = Intent(this, CartActivity::class.java)
            startActivity(cartIntent)
        }

        val tvAmount = menuItem?.findViewById<TextView>(R.id.cart_badge)
        searchViewModel.cart.observe(this) {
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val amount = it.data?.size
                    if (amount == 0) {
                        tvAmount?.visibility = View.GONE
                    } else {
                        tvAmount?.visibility = View.VISIBLE
                        tvAmount?.text = amount?.toString()
                    }
                }

                is Resource.Error -> {}
            }
        }
    }

    private fun setupAdapter() {
        binding.rvSearchProduct.apply {
            setHasFixedSize(true)
            adapter = searchAdapter
        }
    }

    private fun productItemClicked(fish: Fish) {
        val detailIntent = Intent(this, DetailFishActivity::class.java)
        detailIntent.putExtra(EXTRA_FISH_ID, fish.fishID)
        startActivity(detailIntent)
    }

    private fun isEmptyResult(isEmpty: Boolean) {
        if (!isEmpty) {
            binding.apply {
                rvSearchProduct.visibility = View.VISIBLE
                imgEmptySearch.visibility = View.GONE
                tvEmptySearch.visibility = View.GONE
            }
        } else {
            binding.apply {
                rvSearchProduct.visibility = View.GONE
                imgEmptySearch.visibility = View.VISIBLE
                tvEmptySearch.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val EXTRA_FISH_ID = "id.fishku.consumer.search.fishId"
    }
}