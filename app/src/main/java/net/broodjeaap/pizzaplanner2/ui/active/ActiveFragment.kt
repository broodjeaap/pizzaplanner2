package net.broodjeaap.pizzaplanner2.ui.active

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.broodjeaap.pizzaplanner2.R
import net.broodjeaap.pizzaplanner2.data.repository.PlannedRecipeRepository
import net.broodjeaap.pizzaplanner2.data.repository.PlannedRecipeRepository.ActiveRecipeData
import net.broodjeaap.pizzaplanner2.databinding.FragmentActiveListBinding

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
        adapter = ActiveRecipesAdapter(
            onRecipeClick = { activeRecipeData ->
                // Navigate to detailed view for this specific recipe
                navigateToRecipeDetail(activeRecipeData.recipe.id)
            },
            onRecipeLongClick = { activeRecipeData ->
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(activeRecipeData)
                true // Return true to indicate the long press was handled
            }
        )
        
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
    
    private fun showDeleteConfirmationDialog(activeRecipeData: ActiveRecipeData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete '${activeRecipeData.recipe.recipeName}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the recipe
                repository.removeRecipe(activeRecipeData.recipe.id)
                // Refresh the list
                loadActiveRecipes()
                Toast.makeText(requireContext(), "Recipe deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
