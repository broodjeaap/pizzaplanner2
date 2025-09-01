package com.pizzaplanner.ui.active

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pizzaplanner.R
import com.pizzaplanner.data.repository.PlannedRecipeRepository
import com.pizzaplanner.databinding.FragmentActiveListBinding

class ActiveFragment : Fragment() {

    private var _binding: FragmentActiveListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: PlannedRecipeRepository
    private lateinit var adapter: ActiveRecipesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = PlannedRecipeRepository(requireContext())
        setupRecyclerView()
        loadActiveRecipes()
    }
    
    private fun setupRecyclerView() {
        adapter = ActiveRecipesAdapter { activeRecipeData ->
            // Navigate to detailed view for this specific recipe
            navigateToRecipeDetail(activeRecipeData.recipe.id)
        }
        
        binding.recyclerViewActiveRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ActiveFragment.adapter
        }
    }
    
    private fun navigateToRecipeDetail(recipeId: String) {
        // Create a bundle with the recipe ID
        val bundle = Bundle().apply {
            putString("recipeId", recipeId)
        }
        
        // Navigate using Navigation Component
        findNavController().navigate(R.id.action_active_to_active_recipe_detail, bundle)
    }
    
    private fun loadActiveRecipes() {
        val allRecipes = repository.getAllRecipesList().sortedByDescending { it.recipe.startTime }
        
        if (allRecipes.isEmpty()) {
            showEmptyState()
        } else {
            showActiveRecipesList()
            adapter.submitList(allRecipes)
        }
    }
    
    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerViewActiveRecipes.visibility = View.GONE
    }
    
    private fun showActiveRecipesList() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.recyclerViewActiveRecipes.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh the list when coming back to this fragment
        loadActiveRecipes()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
